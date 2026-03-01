package resto_dev.shared.tenancy;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Thread-local holder for the current tenant schema.
 * Used by multi-tenant infrastructure to route queries to the correct schema.
 *
 * Flow: JwtFilter authenticates → TenantFilter resolves tenant → sets schema
 * here.
 */
@Slf4j
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_ORGANIZATION_ID = new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    public static void setCurrentTenant(String tenantSchema) {
        log.debug("Setting tenant context to: {}", tenantSchema);
        CURRENT_TENANT.set(tenantSchema);
    }

    public static void setCurrentOrganizationId(UUID organizationId) {
        log.debug("Setting organization id context to: {}", organizationId);
        CURRENT_ORGANIZATION_ID.set(organizationId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static UUID getCurrentOrganizationId() {
        return CURRENT_ORGANIZATION_ID.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_ORGANIZATION_ID.remove();
    }
}
