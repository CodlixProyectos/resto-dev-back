package resto_dev.modules.adminsaas.members.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.entity.UserJpaEntity;
import resto_dev.shared.common.BaseEntity;
import resto_dev.shared.security.permissions.RoleEntity;

import java.time.LocalDateTime;

/**
 * JPA entity for organization_members pivot table.
 * Links User + Organization + Role (generic, not restaurant-specific).
 */
@Entity
@Table(name = "organization_members", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = {
        "organization_id", "user_id", "role_id" }, name = "uk_member_org_user_role"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationMemberJpaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationJpaEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
}
