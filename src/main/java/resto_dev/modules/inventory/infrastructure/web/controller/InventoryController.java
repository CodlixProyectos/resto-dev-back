package resto_dev.modules.inventory.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.inventory.application.service.InventoryService;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.repository.InventoryItemJpaRepository;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Gestión de Insumos y Stock")
public class InventoryController {

    private final InventoryItemJpaRepository itemRepository;
    private final InventoryService inventoryService;

    @GetMapping("/items")
    @Operation(summary = "Obtener lista de insumos con paginación y búsqueda")
    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryItemJpaEntity>>> getItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<InventoryItemJpaEntity> result;
        
        if (search != null && !search.trim().isEmpty()) {
            result = itemRepository.findAllByActiveTrueAndNameContainingIgnoreCase(search, pageable);
        } else {
            result = itemRepository.findAllByActiveTrue(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(result)));
    }

    @PostMapping("/items")
    @Operation(summary = "Crear o actualizar un nuevo insumo")
    public ResponseEntity<ApiResponse<InventoryItemJpaEntity>> saveItem(@RequestBody InventoryItemJpaEntity item) {
        if (item.getId() == null) {
            item.setCreatedAt(LocalDateTime.now());
            item.setActive(true);
        } else {
            InventoryItemJpaEntity existing = itemRepository.findById(item.getId())
                    .orElseThrow(() -> new RuntimeException("Item no encontrado: " + item.getId()));
            item.setCreatedAt(existing.getCreatedAt());
            item.setUpdatedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(ApiResponse.ok(itemRepository.save(item)));
    }

    @GetMapping("/movements")
    @Operation(summary = "Obtener historial de movimientos (Kardex) con paginación")
    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryService.MovementDetailedDto>>> getMovementHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("date").descending());
        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(inventoryService.getMovementHistory(pageable))));
    }

    @PostMapping("/movements")
    @Operation(summary = "Registrar movimiento de stock (Compra, Consumo, etc.)")
    public ResponseEntity<ApiResponse<String>> recordMovement(@RequestBody MovementRequest request) {
        inventoryService.recordMovement(
                request.itemId(),
                request.type(),
                request.quantity(),
                request.reason(),
                request.unitPrice(),
                request.supplierId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Movimiento registrado correctamente"));
    }

    public record MovementRequest(
            UUID itemId,
            String type,
            BigDecimal quantity,
            String reason,
            BigDecimal unitPrice,
            UUID supplierId
    ) {}
}
