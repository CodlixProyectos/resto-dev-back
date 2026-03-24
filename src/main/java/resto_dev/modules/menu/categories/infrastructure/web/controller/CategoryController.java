package resto_dev.modules.menu.categories.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.categories.application.port.input.CreateCategoryUseCase;
import resto_dev.modules.menu.categories.application.port.input.DeleteCategoryUseCase;
import resto_dev.modules.menu.categories.application.port.input.ListCategoriesUseCase;
import resto_dev.modules.menu.categories.application.port.input.UpdateCategoryUseCase;
import resto_dev.modules.menu.categories.application.command.CreateCategoryCommand;
import resto_dev.modules.menu.categories.application.command.UpdateCategoryCommand;
import resto_dev.modules.menu.categories.infrastructure.web.dto.input.CreateCategoryRequest;
import resto_dev.modules.menu.categories.infrastructure.web.dto.input.UpdateCategoryRequest;
import resto_dev.modules.menu.categories.infrastructure.web.dto.output.CategoryResponse;
import resto_dev.modules.menu.categories.infrastructure.web.mapper.CategoryWebMapper;

import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías del Menú", description = "Endpoints para gestionar las categorías del menú del restaurante")
public class CategoryController {

        private final CreateCategoryUseCase createCategoryUseCase;
        private final ListCategoriesUseCase listCategoriesUseCase;
        private final UpdateCategoryUseCase updateCategoryUseCase;
        private final DeleteCategoryUseCase deleteCategoryUseCase;
        private final CategoryWebMapper categoryWebMapper;

        @PostMapping("/create")
        @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_MENU')")
        @Operation(summary = "Crear una nueva categoría", description = "Crea una nueva categoría para el menú del restaurante.", responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
        })
        public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @Valid @RequestBody CreateCategoryRequest request) {
                CreateCategoryCommand command = categoryWebMapper.toCommand(request);
                Category createdCategory = createCategoryUseCase.execute(command);
                return new ResponseEntity<>(ApiResponse.created(categoryWebMapper.toResponse(createdCategory),
                                "Categoría creada exitosamente"), HttpStatus.CREATED);
        }

        @GetMapping("/list")
        @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_MENU')")
        @Operation(summary = "Listar categorías (paginado y filtrado)", description = "Obtiene una lista paginada de categorías. Permite filtrar dinámicamente por nombre o estado.")
        public ResponseEntity<ApiResponse<PaginatedResponse<CategoryResponse>>> listCategories(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Boolean isActive,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                resto_dev.modules.menu.categories.application.query.ListCategoriesQuery query = resto_dev.modules.menu.categories.application.query.ListCategoriesQuery
                                .builder()
                                .name(name)
                                .isActive(isActive)
                                .page(page)
                                .size(size)
                                .build();

                resto_dev.shared.common.pagination.PageModel<Category> pageModel = listCategoriesUseCase.execute(query);

                List<CategoryResponse> responses = pageModel.content().stream()
                                .map(categoryWebMapper::toResponse)
                                .toList();

                PaginatedResponse<CategoryResponse> pageResponse = PaginatedResponse.<CategoryResponse>builder()
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
        @Operation(summary = "Actualizar una categoría", description = "Actualiza los detalles de una categoría existente mediante su ID.")
        public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateCategoryRequest request) {
                UpdateCategoryCommand command = categoryWebMapper.toCommand(request);
                Category updatedCategory = updateCategoryUseCase.execute(id, command);
                return ResponseEntity.ok(ApiResponse.ok(categoryWebMapper.toResponse(updatedCategory)));
        }

        @DeleteMapping("/delete/{id}")
        @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_MENU')")
        @Operation(summary = "Eliminar una categoría", description = "Elimina una categoría por completo mediante su ID.")
        public ResponseEntity<ApiResponse<Void>> deleteCategory(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @PathVariable UUID id) {
                deleteCategoryUseCase.execute(id);
                return ResponseEntity.ok(ApiResponse.ok(null, "Categoría eliminada exitosamente"));
        }
}
