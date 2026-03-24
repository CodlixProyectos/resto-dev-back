package resto_dev.modules.layout.tables.infrastructure.web.dto.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Datos para crear múltiples mesas de forma masiva")
public record BulkCreateTableRequest(
        @Schema(description = "ID de la zona a la que pertenecen las mesas", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID de la zona es obligatorio.") 
        UUID zoneId,

        @Schema(description = "Número base para comenzar la secuencia (ej: '1')", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El número base de mesa es obligatorio.") 
        @Size(max = 20, message = "El número no debe superar los 20 caracteres.") 
        String baseTableNumber,

        @Schema(description = "Capacidad de comensales para todas las mesas", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "La capacidad debe ser un número positivo.") 
        int capacity,

        @Schema(description = "Cantidad de mesas a crear", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 1, message = "Debes crear al menos una mesa.")
        @Positive(message = "La cantidad debe ser un número positivo.")
        int quantity) {
}
