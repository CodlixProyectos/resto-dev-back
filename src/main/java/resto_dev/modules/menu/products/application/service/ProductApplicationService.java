package resto_dev.modules.menu.products.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.products.application.port.input.CreateProductUseCase;
import resto_dev.modules.menu.products.application.port.input.ListProductsUseCase;
import resto_dev.modules.menu.products.application.command.CreateProductCommand;
import resto_dev.modules.menu.products.application.query.SearchProductsQuery;
import resto_dev.modules.menu.products.application.port.output.ProductRepositoryPort;
import resto_dev.shared.common.pagination.PageModel;

import resto_dev.modules.menu.products.application.port.input.UpdateProductUseCase;
import resto_dev.modules.menu.products.application.port.input.DeleteProductUseCase;
import resto_dev.modules.menu.products.application.command.UpdateProductCommand;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductApplicationService
        implements CreateProductUseCase, ListProductsUseCase, UpdateProductUseCase, DeleteProductUseCase {

    private final ProductRepositoryPort productRepository;

    @Override
    public Product execute(CreateProductCommand command) {
        if (productRepository.existsByNameAndCategoryId(command.name(), command.categoryId())) {
            throw new IllegalArgumentException("Product with this name already exists in this category");
        }

        Product newProduct = Product.builder()
                .name(command.name())
                .description(command.description())
                .price(command.price())
                .imageUrl(command.imageUrl())
                .categoryId(command.categoryId())
                .active(true)
                .build();

        return productRepository.save(newProduct);
    }

    @Override
    public PageModel<Product> execute(SearchProductsQuery query) {
        return productRepository.searchProducts(query);
    }

    @Override
    public Product execute(UUID id, UpdateProductCommand command) {
        Optional<Product> existingProduct = productRepository.findById(id);

        if (existingProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }

        Product product = existingProduct.get();
        product.setName(command.name());
        product.setDescription(command.description());
        product.setPrice(command.price());
        product.setImageUrl(command.imageUrl());
        product.setCategoryId(command.categoryId());
        product.setActive(command.active());

        return productRepository.save(product);
    }

    @Override
    public void execute(UUID id) {
        if (productRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
