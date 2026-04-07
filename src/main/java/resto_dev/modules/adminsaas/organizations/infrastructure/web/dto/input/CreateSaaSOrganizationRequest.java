package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSaaSOrganizationRequest {
    
    @NotBlank(message = "El nombre de la organización es obligatorio")
    private String name;

    @Email(message = "El email del dueño debe ser válido")
    @NotBlank(message = "El email del dueño es obligatorio")
    private String ownerEmail;

    @NotBlank(message = "El plan inicial es obligatorio")
    private String initialPlan; // "TRIAL", "BASIC", "PREMIUM"
    
    private Integer trialDaysCount = 14; // Default

    private String initialPassword;

    private String ownerFullName;
}
