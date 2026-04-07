package resto_dev.modules.pensioners.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.pensioners.domain.model.PensionerPayment;
import resto_dev.modules.pensioners.infrastructure.persistence.entity.PensionerPaymentJpaEntity;

@Component
public class PensionerPaymentJpaMapper {

    public PensionerPayment toDomain(PensionerPaymentJpaEntity entity) {
        return PensionerPayment.builder()
                .id(entity.getId())
                .pensionerId(entity.getPensionerId())
                .organizationId(entity.getOrganizationId())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .paymentMethod(entity.getPaymentMethod())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PensionerPaymentJpaEntity toEntity(PensionerPayment domain) {
        return PensionerPaymentJpaEntity.builder()
                .id(domain.getId())
                .pensionerId(domain.getPensionerId())
                .organizationId(domain.getOrganizationId())
                .amount(domain.getAmount())
                .date(domain.getDate())
                .paymentMethod(domain.getPaymentMethod())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
