package resto_dev.modules.adminsaas.users.infrastructure.web.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request object for updating a user profile")
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    @Schema(description = "Nombre completo del usuario", example = "Jorge Castro")
    private String fullName;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    @Schema(description = "Número telefónico", example = "+51 987 654 321")
    private String phoneNumber;

    @Size(max = 500, message = "La URL del avatar es demasiado larga")
    @Schema(description = "URL de la foto de perfil del usuario", example = "https://example.com/images/avatar.png")
    private String avatarUrl;
}
