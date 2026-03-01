package resto_dev.modules.sales.orders.infrastructure.persistence.adapter;

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
        Specification<OrderJpaEntity> spec = Specification.where((Specification<OrderJpaEntity>) null);

        if (query.tableId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("tableId"), query.tableId()));
        }

        if (query.statuses() != null && !query.statuses().isEmpty()) {
            spec = spec.and((root, cq, cb) -> root.get("status").in(query.statuses()));
        }

        // Para cocina, priorizamos el orden de llegada (las más viejas primero)
        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<OrderJpaEntity> pageResult = orderJpaRepository.findAll(spec, pageable);

        return new PageModel<>(
                pageResult.getContent().stream().map(orderJpaMapper::toDomain).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());
    }
}
