package resto_dev.modules.menu.publicmenu.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.menu.categories.application.port.output.CategoryRepositoryPort;
import resto_dev.modules.menu.categories.application.query.ListCategoriesQuery;
import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.products.application.port.output.ProductRepositoryPort;
import resto_dev.modules.menu.products.application.query.SearchProductsQuery;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.publicmenu.domain.model.PublicMenuCategory;
import resto_dev.modules.menu.publicmenu.domain.model.PublicMenuProduct;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicMenuApplicationService {

    private final CategoryRepositoryPort categoryRepository;
    private final ProductRepositoryPort productRepository;

    public List<PublicMenuCategory> getPublicMenu() {
        // Fetch active categories (up to 1000)
        ListCategoriesQuery catQuery = ListCategoriesQuery.builder()
                .page(0)
                .size(1000)
                .isActive(true)
                .build();
        List<Category> categories = categoryRepository.findAll(catQuery).content();

        // Fetch active products (up to 2000)
        SearchProductsQuery prodQuery = SearchProductsQuery.builder()
                .page(0)
                .size(2000)
                .isActive(true)
                .build();
        List<Product> products = productRepository.searchProducts(prodQuery).content();

        // Group products by category ID
        Map<java.util.UUID, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategoryId));

        // Assemble Nested Menu using Domain Models
        return categories.stream().map(cat -> {
            List<Product> catProducts = productsByCategory.getOrDefault(cat.getId(), List.of());
            List<PublicMenuProduct> productModels = catProducts.stream()
                    .map(p -> PublicMenuProduct.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .price(p.getPrice())
                            .imageUrl(p.getImageUrl())
                                    .build())
                    .toList();

            return PublicMenuCategory.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .description(cat.getDescription())
                    .products(productModels)
                    .build();
        }).toList();
    }
}
