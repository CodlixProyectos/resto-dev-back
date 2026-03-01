package resto_dev.modules.layout.tables.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Table {
    private UUID id;
    private UUID zoneId;
    private String tableNumber;
    private int capacity;
    private TableStatus status;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
