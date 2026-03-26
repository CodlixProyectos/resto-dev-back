package resto_dev.modules.reservations.infrastructure.api.dto.output;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;
import resto_dev.modules.reservations.domain.ReservationStatus;

public record ReservationResponse(
    UUID id,
    String customerName,
    String customerPhone,
    String customerEmail,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer numGuests,
    ReservationStatus status,
    TableInfo table,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record TableInfo(
        UUID id,
        String tableNumber
    ) {}
}
