package resto_dev.modules.sales.orders.application.port.input;

import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.shared.common.pagination.PageModel;

public interface GetActiveKitchenOrdersUseCase {
    PageModel<Order> execute(SearchOrdersQuery query);

    java.math.BigDecimal calculateRevenue(SearchOrdersQuery query);
}
