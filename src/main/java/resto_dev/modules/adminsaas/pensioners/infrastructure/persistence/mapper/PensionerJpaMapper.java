package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.pensioners.domain.model.Pensioner;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.entity.PensionerJpaEntity;

@Component
public class PensionerJpaMapper {

    public Pensioner toDomain(PensionerJpaEntity entity) {
        if (entity == null) return null;
        return Pensioner.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .fullName(entity.getFullName())
                .dni(entity.getDni())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PensionerJpaEntity toEntity(Pensioner domain) {
        if (domain == null) return null;
        PensionerJpaEntity entity = PensionerJpaEntity.builder()
                .organizationId(domain.getOrganizationId())
                .fullName(domain.getFullName())
                .dni(domain.getDni())
                .email(domain.getEmail())
                .phoneNumber(domain.getPhoneNumber())
                .active(domain.isActive())
                .build();
        
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        
        return entity;
    }
}
