package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCodeResponse {
    private UUID id;
    private String code;
    private String planName;
    private Integer trialDays;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;
    private String usedByOrganizationName;
}
