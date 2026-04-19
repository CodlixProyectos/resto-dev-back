package resto_dev.modules.inventory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.entity.StockMovementJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.repository.InventoryCategoryJpaRepository;
import resto_dev.modules.inventory.infrastructure.persistence.repository.InventoryItemJpaRepository;
import resto_dev.modules.inventory.infrastructure.persistence.repository.StockMovementJpaRepository;
import resto_dev.modules.inventory.infrastructure.persistence.repository.SupplierJpaRepository;
import resto_dev.modules.inventory.infrastructure.excel.InventoryExcelParser;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryCategoryJpaEntity;
import resto_dev.modules.sales.orders.application.port.output.AdminEventPublisherPort;
import resto_dev.shared.domain.model.Notification;
import resto_dev.shared.tenancy.TenantContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemJpaRepository itemRepository;
    private final InventoryCategoryJpaRepository categoryRepository;
    private final StockMovementJpaRepository movementRepository;
    private final SupplierJpaRepository supplierRepository;
    private final AdminEventPublisherPort adminEventPublisher;

    @Transactional
    public void recordMovement(UUID itemId, String type, BigDecimal quantity, String reason, BigDecimal unitPrice, UUID supplierId) {
        InventoryItemJpaEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item de inventario no encontrado: " + itemId));

        // 1. Create movement record
        StockMovementJpaEntity movement = StockMovementJpaEntity.builder()
                .itemId(itemId)
                .supplierId(supplierId)
                .type(type)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .reason(reason)
                .date(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        movementRepository.save(movement);

        // 2. Update stock levels
        BigDecimal multiplier = getMultiplierForType(type);
        BigDecimal change = quantity.multiply(multiplier);
        
        item.setCurrentStock(item.getCurrentStock().add(change));
        item.setUpdatedAt(LocalDateTime.now());

        // 3. Update cost price if it's a purchase
        if ("COMPRA".equalsIgnoreCase(type) && unitPrice != null) {
            item.setCostPrice(unitPrice);
        }

        InventoryItemJpaEntity savedItem = itemRepository.save(item);
        log.info("Stock movement recorded for {}: {} {}", item.getName(), change, item.getUnit());

        // Notify Admin if stock is low
        if (savedItem.getCurrentStock().compareTo(savedItem.getMinStock()) <= 0) {
            adminEventPublisher.notifyAdmin(
                TenantContext.getCurrentOrganizationId(),
                Notification.builder()
                        .id(java.util.UUID.randomUUID().toString())
                        .title("Stock Bajo")
                        .message("El producto \"" + savedItem.getName() + "\" está por debajo del nivel mínimo.")
                        .type("warning")
                        .status("new")
                        .timestamp(LocalDateTime.now())
                        .relatedId(savedItem.getId().toString())
                        .relatedType("INVENTORY")
                        .actionUrl("/app/inventory")
                        .build(),
                "LOW_STOCK"
            );
        }
    }

    private BigDecimal getMultiplierForType(String type) {
        return switch (type.toUpperCase()) {
            case "COMPRA", "AJUSTE_POS" -> BigDecimal.ONE;
            case "CONSUMO", "MERMA", "AJUSTE_NEG" -> new BigDecimal("-1");
            default -> BigDecimal.ZERO;
        };
    }

    @Transactional
    public record ImportReport(
            int totalProcessed,
            int duplicatesCount,
            List<String> duplicateNames
    ) {}

    @Transactional
    public ImportReport bulkUploadItems(List<InventoryExcelParser.InventoryExcelRow> rows) {
        int processed = 0;
        int duplicates = 0;
        java.util.List<String> duplicateNames = new java.util.ArrayList<>();

        for (InventoryExcelParser.InventoryExcelRow row : rows) {
            try {
                // 1. Check for duplicates
                String trimmedName = row.name().trim();
                if (itemRepository.existsByNameIgnoreCaseAndActiveTrue(trimmedName)) {
                    duplicates++;
                    duplicateNames.add(trimmedName);
                    continue;
                }

                // 2. Get or create category
                InventoryCategoryJpaEntity category = null;
                if (row.categoryName() != null && !row.categoryName().isBlank()) {
                    category = categoryRepository.findByNameIgnoreCase(row.categoryName().trim())
                            .orElseGet(() -> {
                                InventoryCategoryJpaEntity newCat = new InventoryCategoryJpaEntity();
                                newCat.setName(row.categoryName().trim());
                                newCat.setCreatedAt(LocalDateTime.now());
                                newCat.setActive(true);
                                return categoryRepository.save(newCat);
                            });
                }

                // 3. Build and save item
                InventoryItemJpaEntity item = InventoryItemJpaEntity.builder()
                        .name(trimmedName)
                        .description(row.description())
                        .category(category)
                        .unit(row.unit() != null ? row.unit().toLowerCase() : "un")
                        .currentStock(row.initialStock() != null ? row.initialStock() : BigDecimal.ZERO)
                        .minStock(row.minStock() != null ? row.minStock() : BigDecimal.ZERO)
                        .maxStock(row.maxStock() != null ? row.maxStock() : BigDecimal.ZERO)
                        .costPrice(row.costPrice() != null ? row.costPrice() : BigDecimal.ZERO)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                itemRepository.save(item);
                processed++;
            } catch (Exception e) {
                log.error("Failed to upload row: {}. Reason: {}", row.name(), e.getMessage());
            }
        }
        return new ImportReport(processed, duplicates, duplicateNames);
    }

    @Transactional(readOnly = true)
    public Page<MovementDetailedDto> getMovementHistory(Pageable pageable) {
        return movementRepository.findAll(pageable)
                .map(mov -> {
                    String itemName = itemRepository.findById(mov.getItemId())
                            .map(InventoryItemJpaEntity::getName)
                            .orElse("Insumo Desconocido");
                    String supplierName = null;
                    if (mov.getSupplierId() != null) {
                        supplierName = supplierRepository.findById(mov.getSupplierId())
                                .map(resto_dev.modules.inventory.infrastructure.persistence.entity.SupplierJpaEntity::getName)
                                .orElse("Proveedor Eliminado");
                    }
                    return new MovementDetailedDto(
                            mov.getId(),
                            mov.getItemId(),
                            itemName,
                            mov.getType(),
                            mov.getQuantity(),
                            mov.getUnitPrice(),
                            mov.getDate(),
                            mov.getReason(),
                            supplierName
                    );
                });
    }

    @Transactional
    public void deleteItem(UUID id) {
        InventoryItemJpaEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de inventario no encontrado: " + id));
        item.setActive(false);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
        log.info("Inventory item deactivated: {}", item.getName());
    }

    public record MovementDetailedDto(
            UUID id,
            UUID itemId,
            String itemName,
            String type,
            BigDecimal quantity,
            BigDecimal unitPrice,
            LocalDateTime date,
            String reason,
            String supplierName
    ) {}
}
