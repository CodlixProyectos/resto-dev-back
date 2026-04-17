package resto_dev.modules.reservations.infrastructure.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import resto_dev.modules.layout.tables.infrastructure.persistence.mapper.TableJpaMapper;
import resto_dev.modules.reservations.domain.model.Reservation;
import resto_dev.modules.reservations.infrastructure.persistence.entity.ReservationJpaEntity;

@Component
@RequiredArgsConstructor
public class ReservationPersistenceMapper {

    private final TableJpaMapper tableMapper;

    public Reservation toDomain(ReservationJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Reservation.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .customerEmail(entity.getCustomerEmail())
                .reservationDate(entity.getReservationDate())
                .reservationTime(entity.getReservationTime())
                .numGuests(entity.getNumGuests())
                .status(entity.getStatus())
                .table(tableMapper.toDomain(entity.getTable()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ReservationJpaEntity toEntity(Reservation domain) {
        if (domain == null) {
            return null;
        }

        return ReservationJpaEntity.builder()
                .id(domain.getId())
                .customerName(domain.getCustomerName())
                .customerPhone(domain.getCustomerPhone())
                .customerEmail(domain.getCustomerEmail())
                .reservationDate(domain.getReservationDate())
                .reservationTime(domain.getReservationTime())
                .numGuests(domain.getNumGuests())
                .status(domain.getStatus())
                .table(tableMapper.toEntity(domain.getTable()))
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
