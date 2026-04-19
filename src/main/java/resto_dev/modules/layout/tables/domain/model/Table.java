package resto_dev.modules.layout.tables.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private UUID id;
    private UUID zoneId;
    private String tableNumber;
    private Integer capacity;
    private TableStatus status;
    private Boolean active;
    private Double posX;
    private Double posY;
    private Double width;
    private Double height;
    private Integer rotation;
    private String shape;
    private String customerName;
    private String currentOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Boolean isActive() {
        return active;
    }
}
