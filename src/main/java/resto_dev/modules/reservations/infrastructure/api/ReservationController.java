package resto_dev.modules.reservations.infrastructure.api;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.reservations.application.ReservationService;
import resto_dev.modules.reservations.domain.Reservation;
import resto_dev.modules.reservations.domain.ReservationStatus;
import resto_dev.modules.reservations.infrastructure.api.dto.output.ReservationResponse;
import resto_dev.modules.reservations.infrastructure.api.mapper.ReservationWebMapper;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import resto_dev.modules.reservations.infrastructure.api.dto.output.ReservationStatsResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;
    private final ReservationWebMapper mapper;

    @GetMapping
    public ApiResponse<PaginatedResponse<ReservationResponse>> getReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        String safeSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        Page<Reservation> reservations = service.searchReservations(date, safeSearch, pageable);
        Page<ReservationResponse> responsePage = reservations.map(mapper::toResponse);
        
        return ApiResponse.ok(PaginatedResponse.of(responsePage));
    }

    @GetMapping("/stats")
    public ApiResponse<ReservationStatsResponse> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ReservationStatsResponse stats = ReservationStatsResponse.builder()
                .totalReservations(service.countTotal(date))
                .pendingCount(service.countByStatus(date, ReservationStatus.PENDING))
                .confirmedCount(service.countByStatus(date, ReservationStatus.CONFIRMED))
                .seatedCount(service.countByStatus(date, ReservationStatus.SEATED))
                .build();
        return ApiResponse.ok(stats);
    }

    @PostMapping
    public ApiResponse<ReservationResponse> createReservation(@jakarta.validation.Valid @RequestBody Reservation reservation) {
        Reservation created = service.createReservation(reservation);
        return ApiResponse.ok(mapper.toResponse(created));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ReservationResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam ReservationStatus status) {
        Reservation updated = service.updateStatus(id, status);
        return ApiResponse.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable UUID id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
