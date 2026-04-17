package resto_dev.modules.reservations.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import resto_dev.modules.reservations.application.port.output.ReservationRepositoryPort;
import resto_dev.modules.reservations.domain.model.Reservation;
import resto_dev.modules.reservations.domain.model.ReservationStatus;
import resto_dev.modules.reservations.infrastructure.persistence.mapper.ReservationPersistenceMapper;
import resto_dev.modules.reservations.infrastructure.persistence.repository.ReservationJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final ReservationJpaRepository repository;
    private final ReservationPersistenceMapper mapper;

    @Override
    public List<Reservation> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Reservation save(Reservation reservation) {
        var entity = mapper.toEntity(reservation);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<Reservation> findByReservationDateOrderByReservationTimeAsc(LocalDate date) {
        return repository.findByReservationDateOrderByReservationTimeAsc(date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Reservation> searchReservations(LocalDate date, String search, Pageable pageable) {
        return repository.searchReservations(date, search, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public long countByStatus(ReservationStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    public long countByReservationDateAndStatus(LocalDate date, ReservationStatus status) {
        return repository.countByReservationDateAndStatus(date, status);
    }

    @Override
    public long countByReservationDate(LocalDate date) {
        return repository.countByReservationDate(date);
    }

    @Override
    public long count() {
        return repository.count();
    }
}
