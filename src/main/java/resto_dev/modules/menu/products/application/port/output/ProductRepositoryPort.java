package resto_dev.modules.menu.products.application.port.output;

import resto_dev.shared.common.pagination.PageModel;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.products.application.query.SearchProductsQuery;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {
    Product save(Product product);

    Optional<Product> findById(UUID id);

    boolean existsByNameAndCategoryId(String name, UUID categoryId);

    PageModel<Product> searchProducts(SearchProductsQuery query);

    void deleteById(UUID id);
}
