package resto_dev.shared.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Tells Hibernate what the current schema (tenant) is.
 * Reads from TenantContext. Defaults to "public".
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantName = TenantContext.getCurrentTenant();
        return (tenantName != null) ? tenantName : "public";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
