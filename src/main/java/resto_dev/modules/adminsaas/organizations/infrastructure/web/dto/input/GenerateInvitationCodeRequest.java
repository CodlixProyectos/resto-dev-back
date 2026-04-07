package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateInvitationCodeRequest {
    
    @NotBlank(message = "El nombre del plan es obligatorio")
    private String planName; // "FREE", "BASIC", "PRO"

    @NotNull(message = "Los días de prueba son obligatorios")
    private Integer trialDays;
}
