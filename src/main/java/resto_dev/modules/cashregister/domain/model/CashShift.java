package resto_dev.modules.cashregister.domain.model;

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
public class CashShift {
    private UUID id;
    private UUID openedBy;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal startingCash;
    private BigDecimal expectedCash;
    private BigDecimal actualCash;
    private BigDecimal difference;
    private ShiftStatus status;

    public void close(BigDecimal reportedActualCash, BigDecimal totalSystemSalesEnEfectivo) {
        this.status = ShiftStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.expectedCash = this.startingCash.add(totalSystemSalesEnEfectivo);
        this.actualCash = reportedActualCash;
        this.difference = reportedActualCash.subtract(this.expectedCash);
    }
}
