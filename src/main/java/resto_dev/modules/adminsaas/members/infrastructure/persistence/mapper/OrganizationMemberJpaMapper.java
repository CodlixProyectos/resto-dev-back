package resto_dev.modules.adminsaas.members.infrastructure.persistence.mapper;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.entity.UserJpaEntity;
import resto_dev.shared.security.permissions.RoleEntity;

/**
 * Mapper for OrganizationMember Entity to Domain.
 */
@Component
@RequiredArgsConstructor
public class OrganizationMemberJpaMapper {

    private final EntityManager entityManager;

    public OrganizationMember toDomain(OrganizationMemberJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationMember.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization().getId())
                .userId(entity.getUser().getId())
                .roleId(entity.getRole() != null ? entity.getRole().getId() : null)
                .pin(entity.getPin())
                .active(entity.isActive())
                .joinedAt(entity.getJoinedAt())
                .build();
    }

    public OrganizationMemberJpaEntity toEntity(OrganizationMember domain) {
        if (domain == null) {
            return null;
        }

        OrganizationMemberJpaEntity entity = OrganizationMemberJpaEntity.builder()
                .organization(entityManager.getReference(OrganizationJpaEntity.class, domain.getOrganizationId()))
                .user(entityManager.getReference(UserJpaEntity.class, domain.getUserId()))
                .role(domain.getRoleId() != null ? entityManager.getReference(RoleEntity.class, domain.getRoleId())
                        : null)
                .pin(domain.getPin())
                .active(domain.isActive())
                .build();

        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        return entity;
    }
}
