package resto_dev.shared.security.permissions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.members.ports.out.MemberRepositoryPort;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Evaluates custom @PreAuthorize("hasPermission(#orgId, 'Organization',
 * 'CREATE_ORDER')") rules.
 * Checks the database (via MemberRepositoryPort) to see if the user has the
 * required permission
 * in the specific organization. SuperAdmins automatically pass.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final MemberRepositoryPort memberRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || !(permission instanceof String)) {
            return false;
        }

        // Super admins have omnipotent access
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return true;
        }

        // targetDomainObject should be the organizationId
        UUID organizationId = extractUuid(targetDomainObject);
        if (organizationId == null) {
            log.warn("Permission denied: targetDomainObject missing or not UUID in @PreAuthorize");
            return false;
        }

        UUID userId = (UUID) authentication.getPrincipal();
        String requiredPermission = (String) permission;

        List<String> userPermissions = memberRepository.findPermissionsByOrganizationAndUser(organizationId, userId);
        boolean hasPerm = userPermissions.contains(requiredPermission);

        log.debug("User {} requesting '{}' for Org {}: {}", userId, requiredPermission, organizationId,
                hasPerm ? "GRANTED" : "DENIED");

        return hasPerm;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        if ("Organization".equals(targetType)) {
            return hasPermission(authentication, targetId, permission);
        }
        log.warn("Unknown targetType in hasPermission: {}", targetType);
        return false;
    }

    private UUID extractUuid(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof UUID uuid)
            return uuid;
        if (obj instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
