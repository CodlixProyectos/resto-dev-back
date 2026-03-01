package resto_dev.modules.layout.tables.application.query;

import lombok.Builder;
import resto_dev.modules.layout.tables.domain.model.TableStatus;

import java.util.UUID;

@Builder
public record SearchTablesQuery(
        String search, // by tableNumber
        UUID zoneId,
        TableStatus status,
        Boolean isActive,
        int page,
        int size) {
}
