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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Gestión de Insumos y Stock")
public class InventoryController {

    private final InventoryItemJpaRepository itemRepository;
    private final InventoryService inventoryService;
    private final resto_dev.modules.inventory.infrastructure.excel.InventoryExcelParser excelParser;

    @GetMapping("/items")
    @Operation(summary = "Obtener lista de insumos con paginación y búsqueda")
    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryItemJpaEntity>>> getItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("name").ascending());
        
        Specification<InventoryItemJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase().trim() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (status != null && !status.isEmpty()) {
                switch (status.toLowerCase()) {
                    case "in-stock" -> {
                        predicates.add(cb.greaterThan(root.get("currentStock"), root.get("minStock")));
                        predicates.add(cb.greaterThan(root.get("currentStock"), BigDecimal.ZERO));
                    }
                    case "low-stock" -> {
                        predicates.add(cb.lessThanOrEqualTo(root.get("currentStock"), root.get("minStock")));
                        predicates.add(cb.greaterThan(root.get("currentStock"), BigDecimal.ZERO));
                    }
                    case "out-of-stock" -> predicates.add(cb.lessThanOrEqualTo(root.get("currentStock"), BigDecimal.ZERO));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InventoryItemJpaEntity> result = itemRepository.findAll(spec, pageable);
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

    @PostMapping("/bulk-upload")
    @Operation(summary = "Cargar insumos masivamente desde un archivo Excel")
    public ResponseEntity<ApiResponse<String>> bulkUpload(@RequestParam("file") MultipartFile file) {
        try {
            var rows = excelParser.parse(file);
            int processed = inventoryService.bulkUploadItems(rows);
            return ResponseEntity.ok(ApiResponse.ok("Se han procesado " + processed + " productos correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error al procesar el archivo Excel: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-save")
    @Operation(summary = "Guardar insumos masivamente desde datos JSON (Previsualización editable)")
    public ResponseEntity<ApiResponse<String>> bulkSave(@RequestBody List<resto_dev.modules.inventory.infrastructure.excel.InventoryExcelParser.InventoryExcelRow> rows) {
        try {
            int processed = inventoryService.bulkUploadItems(rows);
            return ResponseEntity.ok(ApiResponse.ok("Se han guardado " + processed + " productos correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error al guardar los productos: " + e.getMessage()));
        }
    }

    @GetMapping("/movements")
    @Operation(summary = "Obtener historial de movimientos (Kardex) con paginación")
    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryService.MovementDetailedDto>>> getMovementHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("date").descending());
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
