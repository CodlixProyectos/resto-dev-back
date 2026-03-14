package resto_dev.modules.layout.tables.infrastructure.web.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import resto_dev.modules.layout.tables.domain.model.TableStatus;

import java.util.UUID;

@Schema(description = "Datos para crear una nueva mesa")
public record CreateTableRequest(
        @Schema(description = "ID de la zona a la que pertenece la mesa", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID de la zona es obligatorio.") 
        UUID zoneId,

        @Schema(description = "Número o código identificador de la mesa", example = "A1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El número de mesa es obligatorio.") 
        @Size(max = 50, message = "El número no debe superar los 50 caracteres.") 
        String tableNumber,

        @Schema(description = "Capacidad de comensales", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "La capacidad debe ser un número positivo.") 
        int capacity,

        @Schema(description = "Estado inicial de la mesa (OPCIONAL - por defecto FREE)", 
                example = "FREE", 
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"FREE", "OCCUPIED", "DIRTY", "OUT_OF_SERVICE"})
        TableStatus status) {
}
