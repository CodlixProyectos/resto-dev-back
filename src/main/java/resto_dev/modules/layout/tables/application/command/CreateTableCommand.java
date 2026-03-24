package resto_dev.modules.layout.tables.application.command;

import lombok.Builder;
import resto_dev.modules.layout.tables.domain.model.TableStatus;

import java.util.UUID;

@Builder
public record CreateTableCommand(
        UUID zoneId,
        String tableNumber,
        int capacity,
        TableStatus status,
        Double posX,
        Double posY,
        Double width,
        Double height,
        Integer rotation,
        String shape) {
}
