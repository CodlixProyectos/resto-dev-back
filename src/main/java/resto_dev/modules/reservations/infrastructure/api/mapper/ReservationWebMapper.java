package resto_dev.modules.reservations.infrastructure.api.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.reservations.domain.model.Reservation;
import resto_dev.modules.reservations.infrastructure.api.dto.output.ReservationResponse;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationWebMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationResponse.TableInfo tableInfo = null;
        if (reservation.getTable() != null) {
            tableInfo = new ReservationResponse.TableInfo(
                reservation.getTable().getId(),
                reservation.getTable().getTableNumber()
            );
        }

        return new ReservationResponse(
            reservation.getId(),
            reservation.getCustomerName(),
            reservation.getCustomerPhone(),
            reservation.getCustomerEmail(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getNumGuests(),
            reservation.getStatus(),
            tableInfo,
            reservation.getNotes(),
            reservation.getCreatedAt(),
            reservation.getUpdatedAt()
        );
    }

    public List<ReservationResponse> toResponseList(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
