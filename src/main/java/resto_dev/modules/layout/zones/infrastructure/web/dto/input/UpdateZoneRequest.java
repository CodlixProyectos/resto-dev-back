package resto_dev.modules.layout.zones.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateZoneRequest(
        @NotBlank(message = "El nombre de la zona es obligatorio.") @Size(max = 100, message = "El nombre no debe superar los 100 caracteres.") String name,

        @Size(max = 255, message = "La descripción no debe superar los 255 caracteres.") String description,
        boolean active,
        Double entrancePosX,
        Double entrancePosY) {
}
