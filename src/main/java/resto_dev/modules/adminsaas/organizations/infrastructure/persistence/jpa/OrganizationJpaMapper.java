package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.jpa;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.organizations.domain.Organization;

@Component
public class OrganizationJpaMapper {

    public Organization toDomain(OrganizationJpaEntity entity) {
        return Organization.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .schemaName(entity.getSchemaName())
                .type(entity.getType())
                .ownerId(entity.getOwnerId())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public OrganizationJpaEntity toEntity(Organization org) {
        OrganizationJpaEntity entity = OrganizationJpaEntity.builder()
                .name(org.getName())
                .slug(org.getSlug())
                .schemaName(org.getSchemaName())
                .type(org.getType())
                .ownerId(org.getOwnerId())
                .active(org.isActive())
                .build();

        if (org.getId() != null) {
            entity.setId(org.getId());
        }
        return entity;
    }
}
