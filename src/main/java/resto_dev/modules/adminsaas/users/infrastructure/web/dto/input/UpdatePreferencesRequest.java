package resto_dev.modules.adminsaas.users.infrastructure.web.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for updating user preferences")
public class UpdatePreferencesRequest {

    @NotNull(message = "El estado de notificaciones es obligatorio")
    @Schema(description = "Habilitar notificaciones", example = "true")
    private Boolean notificationsEnabled;

    @NotNull(message = "El estado de sonido es obligatorio")
    @Schema(description = "Habilitar sonidos", example = "true")
    private Boolean soundEnabled;

    @NotNull(message = "El estado de modo oscuro es obligatorio")
    @Schema(description = "Habilitar modo oscuro", example = "false")
    private Boolean darkModeEnabled;
}
