package resto_dev.modules.inventory.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryCategoryJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.repository.InventoryCategoryJpaRepository;
import resto_dev.shared.responses.ApiResponse;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/categories")
@RequiredArgsConstructor
@Tag(name = "Inventory Categories", description = "Gestión de las categorías organizativas de los insumos (Módulo V2)")
public class InventoryCategoryController {

    private final InventoryCategoryJpaRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Obtener lista de todas las categorías")
    public ResponseEntity<ApiResponse<List<InventoryCategoryJpaEntity>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryRepository.findAll()));
    }

    @PostMapping
    @Operation(summary = "Crear o actualizar una categoría de insumos")
    public ResponseEntity<ApiResponse<InventoryCategoryJpaEntity>> saveCategory(@RequestBody InventoryCategoryJpaEntity category) {
        if (category.getId() == null) {
            category.setCreatedAt(LocalDateTime.now());
        } else {
            category.setUpdatedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(ApiResponse.ok(categoryRepository.save(category)));
    }
}
