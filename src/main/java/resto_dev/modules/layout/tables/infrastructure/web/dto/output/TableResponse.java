package resto_dev.modules.layout.tables.infrastructure.web.dto.output;

import resto_dev.modules.layout.tables.domain.model.TableStatus;

import java.util.UUID;

public record TableResponse(
        UUID id,
        UUID zoneId,
        String tableNumber,
        int capacity,
        TableStatus status,
        boolean active) {
}
