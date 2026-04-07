package resto_dev.shared.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.OrganizationSubscriptionJpaRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;


/**
 * Filter to resolve the tenant from the X-Organization-Id HTTP header.
 * Intercepts every request after JwtAuthFilter, validates organization
 * membership,
 * and sets the TenantContext schema.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Organization-Id";

    private final OrganizationJpaRepository organizationRepository;
    private final OrganizationMemberJpaRepository memberRepository;
    private final OrganizationSubscriptionJpaRepository subscriptionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String orgIdHeader = request.getHeader(TENANT_HEADER);

        // Check if organizationId was pre-resolved by JwtAuthFilter (via ticket)
        Object preResolvedOrgId = request.getAttribute("organizationId");
        if (preResolvedOrgId instanceof UUID orgId) {
            orgIdHeader = orgId.toString();
        }
        // Support for EventSource (SSE) which doesn't allow custom headers
        else if (!StringUtils.hasText(orgIdHeader)) {
            orgIdHeader = request.getParameter("organizationId");
        }
        
        // 🚨 ARCHITECTURAL FIX: Extract from URL path (e.g. /api/v1/organizations/{id}/pensioners)
        // This acts as a fallback for modules transitioning to the tenant scope.
        if (!StringUtils.hasText(orgIdHeader)) {
            String path = request.getRequestURI();
            if (path != null && path.contains("/organizations/")) {
                String[] parts = path.split("/");
                for (int i = 0; i < parts.length; i++) {
                    if ("organizations".equals(parts[i]) && i + 1 < parts.length) {
                        String possibleUuid = parts[i + 1];
                        if (possibleUuid.length() == 36) { // basic UUID length check
                            orgIdHeader = possibleUuid;
                        }
                        break;
                    }
                }
            }
        }

        if (StringUtils.hasText(orgIdHeader)) {
            try {
                UUID organizationId = UUID.fromString(orgIdHeader);
                TenantContext.setCurrentOrganizationId(organizationId);
                boolean accessGranted = setupTenantContext(request, organizationId);

                if (!accessGranted) {
                    String errorCode = (String) request.getAttribute("tenantError");
                    if (errorCode == null) errorCode = "ACCESS_DENIED";

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter()
                            .write(String.format("{\"success\":false,\"code\":\"%s\",\"message\":\"Access denied to this organization\"}", errorCode));
                    return; // Stop filter chain
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format for {}: {}", TENANT_HEADER, orgIdHeader);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Crucial: Clear context to avoid memory leaks and cross-request contamination
            // in ThreadLocals
            TenantContext.clear();
        }
    }

    /**
     * Resolves the organization, checks access, and sets the TenantContext.
     * 
     * @return true if access is granted, false otherwise.
     */
    private boolean setupTenantContext(HttpServletRequest request, UUID organizationId) {
        var orgOpt = organizationRepository.findById(organizationId);
        if (orgOpt.isEmpty()) {
            log.warn("Organization {} not found", organizationId);
            return false;
        }

        var org = orgOpt.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Unauthenticated (e.g. public endpoints like public menu)
            TenantContext.setCurrentTenant(org.getSchemaName());
            log.debug("Set tenant context to {} for unauthenticated request", org.getSchemaName());
            return true;
        }

        // Authenticated users must belong to the organization
        UUID userId = (UUID) auth.getPrincipal();

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin || memberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            
            // ARCHITECTURE SaaS: Bloqueo de Muro de Contención para SUSCRIPCIONES
            // Si no eres SUPER_ADMIN, verificamos que tu organización siga con plan activo.
            if (!isSuperAdmin) {
                var subOpt = subscriptionRepository.findFirstByOrganizationIdOrderByCreatedAtDesc(organizationId);
                if (subOpt.isPresent()) {
                    var sub = subOpt.get();
                    if ("SUSPENDED".equals(sub.getStatus())) {
                        log.warn("Organization {} is SUSPENDED. Access Denied.", organizationId);
                        request.setAttribute("tenantError", "SUBSCRIPTION_SUSPENDED");
                        return false;
                    }
                    if (sub.getEndDate() != null && sub.getEndDate().isBefore(LocalDate.now())) {
                        log.warn("Organization {} subscription/trial EXPIRED on {}. Access Denied.", organizationId, sub.getEndDate());
                        request.setAttribute("tenantError", "LICENSE_EXPIRED");
                        return false;
                    }
                } else {
                    log.warn("Organization {} has no active subscriptions. Access Denied.", organizationId);
                    request.setAttribute("tenantError", "NO_SUBSCRIPTION");
                    return false;
                }
            }

            TenantContext.setCurrentTenant(org.getSchemaName());
            log.debug("Set tenant context to {} for user {}", org.getSchemaName(), userId);
            return true;
        } else {
            log.warn("User {} attempted to access organization {} without membership", userId, organizationId);
            return false;
        }
    }
}
