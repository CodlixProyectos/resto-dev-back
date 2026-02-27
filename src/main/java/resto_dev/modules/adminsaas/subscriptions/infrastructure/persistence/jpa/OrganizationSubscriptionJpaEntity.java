package resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.jpa.OrganizationJpaEntity;
import resto_dev.shared.common.BaseEntity;

import java.time.LocalDate;

/**
 * Organization subscription — links an organization to a plan.
 */
@Entity
@Table(name = "organization_subscriptions", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationSubscriptionJpaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationJpaEntity organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlanJpaEntity plan;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "TRIAL";

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
