package resto_dev.shared.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JWT authentication filter — runs once per request.
 * Sets SecurityContext with userId as principal.
 * Adds ROLE_SUPER_ADMIN authority if applicable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final resto_dev.shared.security.sse.SseTicketService sseTicketService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String ticketIdStr = request.getParameter("ticket");
        if (StringUtils.hasText(ticketIdStr)) {
            try {
                UUID ticketId = UUID.fromString(ticketIdStr);
                var ticketDataOpt = sseTicketService.validateAndConsumeTicket(ticketId);
                
                if (ticketDataOpt.isPresent()) {
                    var data = ticketDataOpt.get();
                    log.debug("Authenticating SSE connection via ticket for user: {}", data.userId());
                    
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    // Default authorities for SSE authenticated users
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    
                    var authentication = new UsernamePasswordAuthenticationToken(
                            data.userId(), null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // Also store organization ID in request for TenantFilter
                    request.setAttribute("organizationId", data.organizationId());
                    
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid SSE ticket format: {}", ticketIdStr);
            }
        }

        String token = extractToken(request);
        log.debug("Extracted token from request: {}, URI: {}", token != null ? "PRESENT" : "NULL", request.getRequestURI());

        if (token != null && jwtProvider.validateToken(token)) {
            UUID userId = jwtProvider.extractUserId(token);
            log.debug("JWT validated for user: {}", userId);
            boolean superAdmin = jwtProvider.extractSuperAdmin(token);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (superAdmin) {
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            }
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            var authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            if (token != null) {
                log.warn("JWT validation failed for token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        // Support for EventSource (SSE) which doesn't allow custom headers
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }

        return null;
    }
}
