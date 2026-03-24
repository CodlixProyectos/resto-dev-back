package resto_dev.modules.layout.tables.infrastructure.web.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;
import resto_dev.modules.layout.tables.domain.model.TableStatus;

import java.util.UUID;

@Schema(description = "Información completa de una mesa")
public record TableResponse(
        @Schema(description = "Identificador único de la mesa (generado automáticamente)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        
        @Schema(description = "ID de la zona a la que pertenece", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID zoneId,
        
        @Schema(description = "Número o código de la mesa", example = "A1")
        String tableNumber,
        
        @Schema(description = "Capacidad de comensales", example = "4")
        int capacity,
        
        @Schema(description = "Estado actual de la mesa", example = "FREE", allowableValues = {"FREE", "OCCUPIED", "DIRTY", "OUT_OF_SERVICE"})
        TableStatus status,
        
        @Schema(description = "Indica si la mesa está activa (siempre true al crear)", example = "true")
        boolean active,

        @Schema(description = "Posición X porcentual", example = "10.5")
        Double posX,

        @Schema(description = "Posición Y porcentual", example = "20.0")
        Double posY,

        @Schema(description = "Ancho de la mesa", example = "60.0")
        Double width,

        @Schema(description = "Alto de la mesa", example = "60.0")
        Double height,

        @Schema(description = "Rotación en grados", example = "0")
        Integer rotation,

        @Schema(description = "Forma de la mesa", example = "SQUARE")
        String shape) {
}
