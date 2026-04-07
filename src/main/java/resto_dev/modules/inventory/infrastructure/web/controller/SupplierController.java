package resto_dev.modules.inventory.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.inventory.application.service.SupplierService;
import resto_dev.modules.inventory.infrastructure.persistence.entity.SupplierJpaEntity;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/suppliers")
@RequiredArgsConstructor
@Tag(name = "Inventory Suppliers", description = "Gestión de Proveedores")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Obtener proveedores activos con paginación y búsqueda")
    public ResponseEntity<ApiResponse<PaginatedResponse<SupplierJpaEntity>>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("name").ascending());
        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(supplierService.getAllActive(pageable, search))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un proveedor por ID")
    public ResponseEntity<ApiResponse<SupplierJpaEntity>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(supplierService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear o actualizar un proveedor")
    public ResponseEntity<ApiResponse<SupplierJpaEntity>> save(@RequestBody SupplierJpaEntity supplier) {
        return ResponseEntity.ok(ApiResponse.ok(supplierService.save(supplier)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar (desactivar) un proveedor")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
