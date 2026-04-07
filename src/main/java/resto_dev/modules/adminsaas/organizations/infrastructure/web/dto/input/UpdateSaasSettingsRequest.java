package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateSaasSettingsRequest {
    private UUID planId;
    private String subscriptionStatus;
    private LocalDate endDate;
    private int userLimit;
    @com.fasterxml.jackson.annotation.JsonProperty("hasInventory")
    private boolean hasInventory;

    @com.fasterxml.jackson.annotation.JsonProperty("hasPensioners")
    private boolean hasPensioners;

    @com.fasterxml.jackson.annotation.JsonProperty("hasKds")
    private boolean hasKds;

}
