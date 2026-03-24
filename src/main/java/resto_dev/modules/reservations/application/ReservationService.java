package resto_dev.modules.reservations.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.reservations.domain.Reservation;
import resto_dev.modules.reservations.domain.ReservationStatus;
import resto_dev.modules.reservations.infrastructure.persistence.ReservationJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationJpaRepository repository;

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return repository.findByReservationDateOrderByReservationTimeAsc(date);
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        return repository.save(reservation);
    }

    @Transactional
    public Reservation updateStatus(UUID id, ReservationStatus status) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus(status);
        return repository.save(reservation);
    }

    @Transactional
    public void deleteReservation(UUID id) {
        repository.deleteById(id);
    }
}
