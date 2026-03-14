package resto_dev.modules.adminsaas.users.infrastructure.web.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request object for changing user password")
public class UpdatePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @Schema(description = "Contraseña actual para validar", example = "MiPassword123")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "La nueva contraseña deseada", example = "NuevoPassword123!")
    private String newPassword;
}
