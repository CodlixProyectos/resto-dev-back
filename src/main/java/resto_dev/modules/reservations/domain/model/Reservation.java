package resto_dev.modules.reservations.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;
import resto_dev.modules.layout.tables.domain.model.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private UUID id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private Integer numGuests;
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;
    private Table table;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
