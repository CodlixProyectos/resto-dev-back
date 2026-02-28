package resto_dev.modules.menu.categories.adapters.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.menu.categories.domain.Category;
import resto_dev.modules.menu.categories.ports.in.CreateCategoryUseCase;
import resto_dev.modules.menu.categories.ports.in.DeleteCategoryUseCase;
import resto_dev.modules.menu.categories.ports.in.ListCategoriesUseCase;
import resto_dev.modules.menu.categories.ports.in.UpdateCategoryUseCase;
import resto_dev.modules.menu.categories.ports.in.dto.CreateCategoryCommand;
import resto_dev.modules.menu.categories.ports.in.dto.UpdateCategoryCommand;

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

    @PostMapping("/create")
    @Operation(summary = "Crear una nueva categoría", description = "Crea una nueva categoría para el menú del restaurante.", responses = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CreateCategoryCommand command) {
        Category createdCategory = createCategoryUseCase.execute(command);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @Operation(summary = "Listar todas las categorías", description = "Obtiene una lista de todas las categorías del menú para el inquilino (tenant) actual.")
    public ResponseEntity<List<Category>> listCategories() {
        List<Category> categories = listCategoriesUseCase.execute();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Actualizar una categoría", description = "Actualiza los detalles de una categoría existente mediante su ID.")
    public ResponseEntity<Category> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryCommand command) {
        Category updatedCategory = updateCategoryUseCase.execute(id, command);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Eliminar una categoría", description = "Elimina una categoría por completo mediante su ID.")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        deleteCategoryUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
