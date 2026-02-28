package resto_dev.shared.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;

import java.io.IOException;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String orgIdHeader = request.getHeader(TENANT_HEADER);

        if (StringUtils.hasText(orgIdHeader)) {
            try {
                UUID organizationId = UUID.fromString(orgIdHeader);
                boolean accessGranted = setupTenantContext(organizationId);

                if (!accessGranted) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter()
                            .write("{\"success\":false,\"message\":\"Access denied to this organization\"}");
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
    private boolean setupTenantContext(UUID organizationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Unauthenticated (e.g. public endpoints like public menu)
            // Allow setting schema blindly. Security rules will protect endpoints anyway.
            organizationRepository.findById(organizationId).ifPresent(org -> {
                TenantContext.setCurrentTenant(org.getSchemaName());
                log.debug("Set tenant context to {} for unauthenticated request", org.getSchemaName());
            });
            return true;
        }

        // Authenticated users must belong to the organization
        UUID userId = (UUID) auth.getPrincipal();

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        boolean isMember = memberRepository.findByOrganizationIdAndUserId(organizationId, userId).size() > 0;

        if (isMember || isSuperAdmin) {
            organizationRepository.findById(organizationId).ifPresent(org -> {
                TenantContext.setCurrentTenant(org.getSchemaName());
                log.debug("Set tenant context to {} for user {}", org.getSchemaName(), userId);
            });
            return true;
        } else {
            log.warn("User {} attempted to access organization {} without membership", userId, organizationId);
            return false;
        }
    }
}
