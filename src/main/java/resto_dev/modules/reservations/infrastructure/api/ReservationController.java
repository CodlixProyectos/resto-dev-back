package resto_dev.modules.reservations.infrastructure.api;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.reservations.application.ReservationService;
import resto_dev.modules.reservations.domain.Reservation;
import resto_dev.modules.reservations.domain.ReservationStatus;
import resto_dev.shared.responses.ApiResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @GetMapping
    public ApiResponse<List<Reservation>> getReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate searchDate = (date != null) ? date : LocalDate.now();
        return ApiResponse.ok(service.getReservationsByDate(searchDate));
    }

    @PostMapping
    public ApiResponse<Reservation> createReservation(@RequestBody Reservation reservation) {
        return ApiResponse.ok(service.createReservation(reservation));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Reservation> updateStatus(
            @PathVariable UUID id,
            @RequestParam ReservationStatus status) {
        return ApiResponse.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable UUID id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
