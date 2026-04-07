package resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateOrganizationRequest(
    @NotBlank(message = "El código de invitación es obligatorio")
    String invitationCode,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    String email,

    @NotBlank(message = "El nombre es obligatorio")
    String fullName,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al min 8 caracteres")
    String password
) {}
