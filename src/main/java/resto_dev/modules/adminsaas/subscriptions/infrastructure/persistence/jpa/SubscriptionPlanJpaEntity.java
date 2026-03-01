package resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.shared.common.BaseEntity;

import java.math.BigDecimal;

/**
 * Subscription plan entity — defines SaaS tiers.
 */
@Entity
@Table(name = "subscription_plans", schema = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanJpaEntity extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "max_tables", nullable = false)
    private int maxTables;

    @Column(name = "max_users", nullable = false)
    private int maxUsers;

    @Column(name = "price_monthly", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}
