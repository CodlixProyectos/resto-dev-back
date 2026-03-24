package resto_dev.modules.reservations.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.reservations.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByReservationDateOrderByReservationTimeAsc(LocalDate date);
}
