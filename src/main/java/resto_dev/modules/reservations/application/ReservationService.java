package resto_dev.modules.reservations.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.reservations.application.port.output.ReservationRepositoryPort;
import resto_dev.modules.reservations.domain.model.Reservation;
import resto_dev.modules.reservations.domain.model.ReservationStatus;
import resto_dev.shared.tenancy.TenantContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepositoryPort repository;
    private final TableRepositoryPort tableRepository;
    private final resto_dev.modules.sales.orders.application.port.input.GetOrderByIdUseCase getOrderByIdUseCase; // Placeholder if needed
    private final resto_dev.modules.sales.orders.application.port.output.AdminEventPublisherPort adminEventPublisher;

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
        if (reservation.getTable() != null && reservation.getReservationDate().equals(java.time.LocalDate.now())) {
            tableRepository.findById(reservation.getTable().getId()).ifPresent(table -> {
                if (table.getStatus() == resto_dev.modules.layout.tables.domain.model.TableStatus.FREE) {
                    table.setStatus(resto_dev.modules.layout.tables.domain.model.TableStatus.RESERVED);
                    tableRepository.save(table);
                }
            });
        }
        Reservation savedReservation = repository.save(reservation);

        // Notify Admin of new reservation
        adminEventPublisher.notifyAdmin(
            TenantContext.getCurrentOrganizationId(),
            resto_dev.shared.domain.model.Notification.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .title("Nueva Reserva")
                    .message(savedReservation.getCustomerName() + " ha reservado una mesa para " + savedReservation.getNumGuests() + " personas.")
                    .type("message")
                    .status("new")
                    .timestamp(java.time.LocalDateTime.now())
                    .relatedId(savedReservation.getId().toString())
                    .relatedType("RESERVATION")
                    .actionUrl("/app/reservations")
                    .build(),
            "RESERVATION_CREATED"
        );

        return savedReservation;
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
