package resto_dev.modules.sales.orders.application.port.output;

import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.shared.common.pagination.PageModel;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findById(UUID id);

    PageModel<Order> searchOrders(SearchOrdersQuery query);
}
