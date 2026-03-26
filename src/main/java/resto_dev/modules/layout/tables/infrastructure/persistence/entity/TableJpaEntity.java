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
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TableStatus status;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "pos_x")
    private Double posX;

    @Column(name = "pos_y")
    private Double posY;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;

    @Column(name = "rotation")
    private Integer rotation;

    @Column(name = "shape", length = 20)
    private String shape;
}
