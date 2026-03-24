package resto_dev.modules.menu.products.application.port.input;

import resto_dev.modules.menu.products.application.query.SearchProductsQuery;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.shared.common.pagination.PageModel;

public interface ListProductsUseCase {
    PageModel<Product> execute(SearchProductsQuery query);
}
