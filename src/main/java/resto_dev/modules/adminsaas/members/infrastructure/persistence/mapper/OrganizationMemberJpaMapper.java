package resto_dev.modules.adminsaas.members.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;

/**
 * Mapper for OrganizationMember Entity to Domain.
 */
@Component
public class OrganizationMemberJpaMapper {

    public OrganizationMember toDomain(OrganizationMemberJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationMember.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization().getId())
                .userId(entity.getUser().getId())
                .roleId(entity.getRole() != null ? entity.getRole().getId() : null)
                .active(entity.isActive())
                .joinedAt(entity.getJoinedAt())
                .build();
    }
}
