package resto_dev.modules.adminsaas.users.infrastructure.web.dto.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration request using an invitation code.
 */
public record RegisterWithCodeRequest(
    @NotBlank(message = "Nombre completo es requerido")
    @Size(min = 3, max = 100)
    String fullName,

    @NotBlank(message = "Email es requerido")
    @Email(message = "Formato de email inválido")
    String email,

    @NotBlank(message = "Contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    @NotBlank(message = "Código de invitación es requerido")
    String invitationCode,

    @NotBlank(message = "Nombre del restaurante es requerido")
    @Size(min = 3, max = 60)
    String organizationName
) {
}
