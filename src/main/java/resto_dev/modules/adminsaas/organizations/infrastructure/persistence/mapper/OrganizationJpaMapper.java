package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.mapper;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;


import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;

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
                .legalName(entity.getLegalName())
                .businessId(entity.getBusinessId())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .logoUrl(entity.getLogoUrl())
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
                .legalName(org.getLegalName())
                .businessId(org.getBusinessId())
                .email(org.getEmail())
                .phone(org.getPhone())
                .address(org.getAddress())
                .logoUrl(org.getLogoUrl())
                .build();

        if (org.getId() != null) {
            entity.setId(org.getId());
        }
        return entity;
    }
}
