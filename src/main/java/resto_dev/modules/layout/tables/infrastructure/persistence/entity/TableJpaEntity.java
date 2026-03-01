package resto_dev.modules.layout.tables.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.layout.zones.infrastructure.persistence.entity.ZoneJpaEntity;
import resto_dev.shared.common.BaseEntity;

@Entity
@jakarta.persistence.Table(name = "restaurant_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableJpaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private ZoneJpaEntity zone;

    @Column(nullable = false, length = 50)
    private String tableNumber;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TableStatus status;

    @Column(nullable = false)
    private boolean active;
}
