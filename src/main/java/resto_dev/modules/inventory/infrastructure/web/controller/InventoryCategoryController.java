package resto_dev.modules.inventory.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryCategoryJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.repository.InventoryCategoryJpaRepository;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/categories")
@RequiredArgsConstructor
@Tag(name = "Inventory Categories", description = "Gestión de las categorías organizativas de los insumos (Módulo V2)")
public class InventoryCategoryController {

    private final InventoryCategoryJpaRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Obtener lista de categorías con paginación y búsqueda")
    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryCategoryJpaEntity>>> getCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<InventoryCategoryJpaEntity> result;
        
        if (search != null && !search.trim().isEmpty()) {
            result = categoryRepository.findAllByNameContainingIgnoreCase(search, pageable);
        } else {
            result = categoryRepository.findAllByActiveTrue(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(result)));
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
