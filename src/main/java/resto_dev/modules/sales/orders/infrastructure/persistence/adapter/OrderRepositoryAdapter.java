package resto_dev.modules.sales.orders.infrastructure.persistence.adapter;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import resto_dev.modules.sales.orders.application.port.output.OrderRepositoryPort;
import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.infrastructure.persistence.entity.OrderJpaEntity;
import resto_dev.modules.sales.orders.infrastructure.persistence.mapper.OrderJpaMapper;
import resto_dev.modules.sales.orders.infrastructure.persistence.repository.OrderJpaRepository;
import resto_dev.shared.common.pagination.PageModel;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderJpaMapper orderJpaMapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderJpaMapper.toEntity(order);
        OrderJpaEntity savedEntity = orderJpaRepository.save(entity);
        return orderJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderJpaRepository.findById(id).map(orderJpaMapper::toDomain);
    }

    @Override
    public PageModel<Order> searchOrders(SearchOrdersQuery query) {
        Specification<OrderJpaEntity> spec = buildSpecification(query);

        // Sort: ASC for KDS (oldest first), DESC for history (newest first)
        Sort sort;
        if (query.statuses() != null
                && query.statuses().contains(resto_dev.modules.sales.orders.domain.model.OrderStatus.PENDING_KITCHEN)) {
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        } else {
            // Historial General: Queremos los que faltan cobrar (No PAID, No CANCELLED) primero, 
            // y luego agrupados por fecha descendente.
            sort = org.springframework.data.jpa.domain.JpaSort.unsafe(Sort.Direction.ASC, 
                "(CASE WHEN status = 'PAID' THEN 1 WHEN status = 'CANCELLED' THEN 1 ELSE 0 END)")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);
        Page<OrderJpaEntity> pageResult = orderJpaRepository.findAll(spec, pageable);

        return new PageModel<>(
                pageResult.getContent().stream().map(orderJpaMapper::toDomain).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());
    }

    @Override
    public java.math.BigDecimal sumTotalByQuery(SearchOrdersQuery query) {
        Specification<OrderJpaEntity> spec = buildSpecification(query);

        // We use the repository to find all with the spec, but we only want the sum of
        // the "total" column.
        // For simplicity and to avoid complex Criteria API manually, we'll use a hack or
        // implement it properly.
        // Proper way:
        return orderJpaRepository.findAll(spec).stream()
                .map(OrderJpaEntity::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private Specification<OrderJpaEntity> buildSpecification(SearchOrdersQuery query) {
        Specification<OrderJpaEntity> spec = Specification.where((root, query1, criteriaBuilder) -> criteriaBuilder.conjunction());

        if (query.tableId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("tableId"), query.tableId()));
        }

        if (query.statuses() != null && !query.statuses().isEmpty()) {
            spec = spec.and((root, cq, cb) -> root.get("status").in(query.statuses()));
        }

        if (query.waiterId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("waiterId"), query.waiterId()));
        }

        if (query.searchTerm() != null && !query.searchTerm().isBlank()) {
            String likePattern = "%" + query.searchTerm().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("customerName")), likePattern),
                    cb.like(cb.lower(root.get("notes")), likePattern),
                    cb.like(cb.lower(root.get("tableNumber")), likePattern),
                    cb.like(cb.function("CONCAT", String.class, root.get("id"), cb.literal("")), likePattern)
            ));
        }

        if (query.startDate() != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), query.startDate()));
        }

        if (query.endDate() != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), query.endDate()));
        }

        // Optimization: Fetch Join items to avoid N+1 and empty item lists in history/billing
        spec = spec.and((root, cq, cb) -> {
            if (Long.class != cq.getResultType() && long.class != cq.getResultType()) {
                root.fetch("items", JoinType.LEFT);
            }
            return null;
        });

        return spec;
    }

    @Override
    public long countByWaiterAndDate(UUID waiterId, java.time.LocalDate date) {
        return orderJpaRepository.countByWaiterIdAndDate(waiterId, date);
    }

    @Override
    public long countByStatus(resto_dev.modules.sales.orders.domain.model.OrderStatus status) {
        return orderJpaRepository.countByStatus(status);
    }

    @Override
    public long countByStatusAndDate(resto_dev.modules.sales.orders.domain.model.OrderStatus status, java.time.LocalDate date) {
        return orderJpaRepository.countByStatusAndDate(status, date);
    }
}
