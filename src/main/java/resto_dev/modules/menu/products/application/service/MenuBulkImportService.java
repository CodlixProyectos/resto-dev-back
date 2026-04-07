package resto_dev.modules.menu.products.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import resto_dev.modules.menu.categories.application.port.input.BulkAddCategoriesUseCase;
import resto_dev.modules.menu.categories.application.port.output.CategoryRepositoryPort;
import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.products.application.port.input.BulkAddProductsUseCase;
import resto_dev.modules.menu.products.application.port.output.ProductRepositoryPort;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.products.infrastructure.excel.MenuExcelParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuBulkImportService implements BulkAddCategoriesUseCase, BulkAddProductsUseCase {

    private final MenuExcelParser excelParser;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    @Override
    @Transactional
    public List<Category> importCategories(MultipartFile file) {
        log.info("Iniciando importación masiva de categorías");
        try {
            List<MenuExcelParser.CategoryExcelRow> rows = excelParser.parseCategories(file);
            List<Category> savedCategories = new ArrayList<>();

            for (MenuExcelParser.CategoryExcelRow row : rows) {
                // Verificar si ya existe por nombre para evitar duplicados en el mismo esquema
                Optional<Category> existing = categoryRepositoryPort.findByName(row.name());
                if (existing.isPresent()) {
                    continue; 
                }

                Category category = Category.builder()
                        .name(row.name())
                        .description(row.description())
                        .isActive(true)
                        .build();

                savedCategories.add(categoryRepositoryPort.save(category));
            }

            log.info("Importadas {} categorías", savedCategories.size());
            return savedCategories;
        } catch (Exception e) {
            log.error("Error en importación masiva de categorías", e);
            throw new RuntimeException("Error al procesar el archivo de categorías: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<Product> importProducts(MultipartFile file) {
        log.info("Iniciando importación masiva de productos");
        try {
            List<MenuExcelParser.ProductExcelRow> rows = excelParser.parseProducts(file);
            List<Product> savedProducts = new ArrayList<>();

            for (MenuExcelParser.ProductExcelRow row : rows) {
                // 1. Obtener o crear categoría
                Category category = getOrCreateCategory(row.categoryName());

                // 2. Verificar duplicado de producto en esa categoría
                if (productRepositoryPort.existsByNameAndCategoryId(row.name(), category.getId())) {
                    continue;
                }

                Product product = Product.builder()
                        .name(row.name())
                        .description(row.description())
                        .price(row.price())
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .active(true)
                        .build();

                savedProducts.add(productRepositoryPort.save(product));
            }

            log.info("Importados {} productos", savedProducts.size());
            return savedProducts;
        } catch (Exception e) {
            log.error("Error en importación masiva de productos", e);
            throw new RuntimeException("Error al procesar el archivo de productos: " + e.getMessage());
        }
    }

    private Category getOrCreateCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            categoryName = "General";
        }
        
        String finalCategoryName = categoryName;
        return categoryRepositoryPort.findByName(finalCategoryName)
                .orElseGet(() -> {
                    log.info("Creando categoría automática: {}", finalCategoryName);
                    Category newCat = Category.builder()
                            .name(finalCategoryName)
                            .description("Creado automáticamente vía importación")
                            .isActive(true)
                            .build();
                    return categoryRepositoryPort.save(newCat);
                });
    }
}
