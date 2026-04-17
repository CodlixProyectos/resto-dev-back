package resto_dev.modules.reservations.application.port.output;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.reservations.domain.model.Reservation;
import resto_dev.modules.reservations.domain.model.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {
    List<Reservation> findAll();
    Optional<Reservation> findById(UUID id);
    Reservation save(Reservation reservation);
    void deleteById(UUID id);
    List<Reservation> findByReservationDateOrderByReservationTimeAsc(LocalDate date);
    Page<Reservation> searchReservations(LocalDate date, String search, Pageable pageable);
    long countByStatus(ReservationStatus status);
    long countByReservationDateAndStatus(LocalDate date, ReservationStatus status);
    long countByReservationDate(LocalDate date);
    long count();
}
