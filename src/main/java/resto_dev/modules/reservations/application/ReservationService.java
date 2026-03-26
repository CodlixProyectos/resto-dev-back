package resto_dev.modules.reservations.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.layout.tables.infrastructure.persistence.repository.TableJpaRepository;
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
    private final TableJpaRepository tableRepository;

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public long countByStatus(LocalDate date, ReservationStatus status) {
        return (date != null) 
                ? repository.countByReservationDateAndStatus(date, status)
                : repository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countTotal(LocalDate date) {
        return (date != null) 
                ? repository.countByReservationDate(date)
                : repository.count();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return repository.findByReservationDateOrderByReservationTimeAsc(date);
    }

    @Transactional(readOnly = true)
    public Page<Reservation> searchReservations(LocalDate date, String search, Pageable pageable) {
        return repository.searchReservations(date, search, pageable);
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // Si tiene mesa asignada, podríamos ponerla en RESERVED de una vez si es para hoy
        if (reservation.getTable() != null && reservation.getReservationDate().equals(LocalDate.now())) {
            tableRepository.findById(reservation.getTable().getId()).ifPresent(table -> {
                if (table.getStatus() == TableStatus.FREE) {
                    table.setStatus(TableStatus.RESERVED);
                    tableRepository.save(table);
                }
            });
        }
        return repository.save(reservation);
    }

    @Transactional
    public Reservation updateStatus(UUID id, ReservationStatus status) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        reservation.setStatus(status);

        // Lógica de transición de Mesa
        if (status == ReservationStatus.SEATED && reservation.getTable() != null) {
            tableRepository.findById(reservation.getTable().getId()).ifPresent(table -> {
                table.setStatus(TableStatus.OCCUPIED);
                tableRepository.save(table);
            });
        } else if (status == ReservationStatus.CANCELLED && reservation.getTable() != null) {
            tableRepository.findById(reservation.getTable().getId()).ifPresent(table -> {
                if (table.getStatus() == TableStatus.RESERVED) {
                    table.setStatus(TableStatus.FREE);
                    tableRepository.save(table);
                }
            });
        }

        return repository.save(reservation);
    }

    @Transactional
    public void deleteReservation(UUID id) {
        repository.deleteById(id);
    }
}
