package resto_dev.modules.cashregister.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import resto_dev.modules.cashregister.domain.model.ShiftStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashShiftJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "opened_by", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID openedBy;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "starting_cash", nullable = false, precision = 10, scale = 2)
    private BigDecimal startingCash;

    @Column(name = "expected_cash", precision = 10, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "actual_cash", precision = 10, scale = 2)
    private BigDecimal actualCash;

    @Column(name = "difference", precision = 10, scale = 2)
    private BigDecimal difference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShiftStatus status;
}
