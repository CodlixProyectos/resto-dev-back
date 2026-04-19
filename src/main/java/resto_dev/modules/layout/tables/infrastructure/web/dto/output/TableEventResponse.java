package resto_dev.modules.layout.tables.infrastructure.web.dto.output;

import resto_dev.modules.layout.tables.domain.model.TableStatus;
import java.util.UUID;

/**
 * DTO especializado para eventos SSE de mesas.
 * Separa la representación de eventos de la respuesta REST estándar.
 */
public record TableEventResponse(
        UUID id,
        UUID zoneId,
        String tableNumber,
        int capacity,
        TableStatus status,
        boolean active,
        Double posX,
        Double posY,
        Double width,
        Double height,
        Integer rotation,
        String shape,
        String customerName,
        String currentOrderId) {
}
