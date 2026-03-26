package resto_dev.modules.reservations.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.reservations.domain.Reservation;
import resto_dev.modules.reservations.domain.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {
    
    @EntityGraph(attributePaths = {"table", "table.zone"})
    List<Reservation> findByReservationDateOrderByReservationTimeAsc(LocalDate date);

    @Query("""
        SELECT r FROM Reservation r LEFT JOIN r.table t 
        WHERE (cast(:date as date) IS NULL OR r.reservationDate = :date)
        AND (cast(:search as text) IS NULL OR :search = '' 
            OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', cast(:search as text), '%')) 
            OR r.customerPhone LIKE CONCAT('%', cast(:search as text), '%')
            OR LOWER(t.tableNumber) LIKE LOWER(CONCAT('%', cast(:search as text), '%')))
        ORDER BY r.reservationDate ASC, r.reservationTime ASC
    """)
    @EntityGraph(attributePaths = {"table", "table.zone"})
    Page<Reservation> searchReservations(@Param("date") LocalDate date, @Param("search") String search, Pageable pageable);

    long countByReservationDate(LocalDate date);
    long countByReservationDateAndStatus(LocalDate date, ReservationStatus status);
    
    long countByStatus(ReservationStatus status);
    long count();

    @Override
    @EntityGraph(attributePaths = {"table", "table.zone"})
    List<Reservation> findAll();
}
