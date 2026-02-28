package resto_dev.modules.menu.categories.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import resto_dev.shared.common.BaseEntity;

@Entity
@Table(name = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryJpaEntity extends BaseEntity {

    private String name;

    private String description;

    private boolean isActive;
}
