package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerConsumption;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.entity.PensionerConsumptionJpaEntity;

@Component
public class PensionerConsumptionJpaMapper {

    public PensionerConsumption toDomain(PensionerConsumptionJpaEntity entity) {
        return PensionerConsumption.builder()
                .id(entity.getId())
                .pensionerId(entity.getPensionerId())
                .organizationId(entity.getOrganizationId())
                .date(entity.getDate())
                .description(entity.getDescription())
                .totalAmount(entity.getTotalAmount())
                .isExtra(entity.isExtra())
                .itemsSnapshot(entity.getItemsSnapshot())
                .notes(entity.getNotes())
                .paymentType(entity.getPaymentType())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PensionerConsumptionJpaEntity toEntity(PensionerConsumption domain) {
        return PensionerConsumptionJpaEntity.builder()
                .id(domain.getId())
                .pensionerId(domain.getPensionerId())
                .organizationId(domain.getOrganizationId())
                .date(domain.getDate())
                .description(domain.getDescription())
                .totalAmount(domain.getTotalAmount())
                .isExtra(domain.isExtra())
                .itemsSnapshot(domain.getItemsSnapshot())
                .notes(domain.getNotes())
                .paymentType(domain.getPaymentType())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
