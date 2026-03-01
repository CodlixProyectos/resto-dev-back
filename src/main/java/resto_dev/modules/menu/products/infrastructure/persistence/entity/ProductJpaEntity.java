package resto_dev.modules.menu.products.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.shared.common.BaseEntity;
import resto_dev.modules.menu.categories.infrastructure.persistence.entity.CategoryJpaEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductJpaEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity category;
}
