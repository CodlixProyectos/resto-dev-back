package resto_dev.modules.menu.products.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.menu.products.application.command.CreateProductCommand;
import resto_dev.modules.menu.products.application.port.input.CreateProductUseCase;
import resto_dev.modules.menu.products.application.port.input.ListProductsUseCase;
import resto_dev.modules.menu.products.application.query.SearchProductsQuery;
import resto_dev.modules.menu.products.domain.model.Product;
import resto_dev.modules.menu.products.infrastructure.web.dto.input.CreateProductRequest;
import resto_dev.modules.menu.products.infrastructure.web.dto.output.ProductResponse;
import resto_dev.modules.menu.products.infrastructure.web.mapper.ProductWebMapper;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Productos del Menú", description = "Endpoints para gestionar los productos y platillos del restaurante")
public class ProductController {

        private final CreateProductUseCase createProductUseCase;
        private final ListProductsUseCase listProductsUseCase;
        private final resto_dev.modules.menu.products.application.port.input.UpdateProductUseCase updateProductUseCase;
        private final resto_dev.modules.menu.products.application.port.input.DeleteProductUseCase deleteProductUseCase;
        private final ProductWebMapper webMapper;

        @PostMapping("/create")
        @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_MENU')")
        @Operation(summary = "Crear un nuevo producto", description = "Crea un producto asociado a una categoría.")
        public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @Valid @RequestBody CreateProductRequest request) {
                CreateProductCommand command = webMapper.toCommand(request);
                Product createdProduct = createProductUseCase.execute(command);
                return new ResponseEntity<>(ApiResponse.created(webMapper.toResponse(createdProduct),
                                "Producto creado exitosamente"), HttpStatus.CREATED);
        }

        @GetMapping("/list")
        @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_MENU')")
        @Operation(summary = "Listar productos", description = "Obtiene una lista paginada de productos con filtrado dinámico.")
        public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> listProducts(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) UUID categoryId,
                        @RequestParam(required = false) Boolean isActive,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                SearchProductsQuery query = SearchProductsQuery.builder()
                                .search(search)
                                .categoryId(categoryId)
                                .isActive(isActive)
                                .page(page)
                                .size(size)
                                .build();

                PageModel<Product> pageModel = listProductsUseCase.execute(query);

                List<ProductResponse> responses = pageModel.content().stream()
                                .map(webMapper::toResponse)
                                .toList();

                PaginatedResponse<ProductResponse> pageResponse = PaginatedResponse.<ProductResponse>builder()
                                .data(responses)
                                .page(pageModel.page())
                                .size(pageModel.size())
                                .totalElements(pageModel.totalElements())
                                .totalPages(pageModel.totalPages())
                                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                                .hasPrevious(pageModel.page() > 0)
                                .build();

                return ResponseEntity.ok(ApiResponse.ok(pageResponse));
        }

        @PutMapping("/update/{id}")
        @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_MENU')")
        @Operation(summary = "Actualizar un producto", description = "Actualiza toda la información de un producto.")
        public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @PathVariable UUID id,
                        @Valid @RequestBody resto_dev.modules.menu.products.infrastructure.web.dto.input.UpdateProductRequest request) {
                resto_dev.modules.menu.products.application.command.UpdateProductCommand command = webMapper
                                .toCommand(request);
                Product updatedProduct = updateProductUseCase.execute(id, command);
                return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(updatedProduct)));
        }

        @DeleteMapping("/delete/{id}")
        @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_MENU')")
        @Operation(summary = "Eliminar un producto", description = "Elimina físicamente un producto del sistema.")
        public ResponseEntity<ApiResponse<Void>> deleteProduct(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @PathVariable UUID id) {
                deleteProductUseCase.execute(id);
                return ResponseEntity.ok(ApiResponse.ok(null, "Producto eliminado exitosamente"));
        }
}
