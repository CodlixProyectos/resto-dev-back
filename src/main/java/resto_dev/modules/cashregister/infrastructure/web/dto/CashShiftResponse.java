package resto_dev.modules.cashregister.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashShiftResponse {
    private UUID id;
    private UUID openedBy;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal startingCash;
    private BigDecimal expectedCash;
    private BigDecimal actualCash;
    private BigDecimal difference;
    private String status;
}
