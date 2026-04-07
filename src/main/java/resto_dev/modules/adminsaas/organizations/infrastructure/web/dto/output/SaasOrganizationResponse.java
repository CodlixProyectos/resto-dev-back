package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Super Admin (SaaS) response containing both Organization and Subscription data.
 */
public record SaasOrganizationResponse(
        UUID id,
        String name,
        String slug,
        String email,
        String phone,
        boolean active,
        
        // Subscription details
        UUID planId,
        String planName,
        String subscriptionStatus,
        LocalDate subscriptionEndDate,
        LocalDate subscriptionStartDate,
        @com.fasterxml.jackson.annotation.JsonProperty("userLimit") int userLimit,
        @com.fasterxml.jackson.annotation.JsonProperty("hasInventory") boolean hasInventory,
        @com.fasterxml.jackson.annotation.JsonProperty("hasPensioners") boolean hasPensioners,
        @com.fasterxml.jackson.annotation.JsonProperty("hasKds") boolean hasKds,
        String invitationCode,
        String yapeQrUrl,
        String plinQrUrl
) {
}
