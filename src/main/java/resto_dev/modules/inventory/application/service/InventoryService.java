package resto_dev.modules.inventory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.inventory.infrastructure.persistence.entity.InventoryItemJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.entity.StockMovementJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.repository.InventoryItemJpaRepository;
import resto_dev.modules.inventory.infrastructure.persistence.repository.StockMovementJpaRepository;
import resto_dev.modules.inventory.infrastructure.persistence.repository.SupplierJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemJpaRepository itemRepository;
    private final StockMovementJpaRepository movementRepository;
    private final SupplierJpaRepository supplierRepository;

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
        // positive for COMPRA/AJUSTE_IN, negative for CONSUMO/MERMA/AJUSTE_OUT
        BigDecimal multiplier = getMultiplierForType(type);
        BigDecimal change = quantity.multiply(multiplier);
        
        item.setCurrentStock(item.getCurrentStock().add(change));
        item.setUpdatedAt(LocalDateTime.now());

        // 3. Update cost price if it's a purchase
        if ("COMPRA".equalsIgnoreCase(type) && unitPrice != null) {
            item.setCostPrice(unitPrice);
        }

        itemRepository.save(item);
        log.info("Stock movement recorded for {}: {} {}", item.getName(), change, item.getUnit());
    }

    private BigDecimal getMultiplierForType(String type) {
        return switch (type.toUpperCase()) {
            case "COMPRA", "AJUSTE_POS" -> BigDecimal.ONE;
            case "CONSUMO", "MERMA", "AJUSTE_NEG" -> new BigDecimal("-1");
            default -> BigDecimal.ZERO;
        };
    }

    @Transactional(readOnly = true)
    public List<MovementDetailedDto> getMovementHistory() {
        // Obtenemos los últimos 100 movimientos ordenados por fecha de creación desc
        return movementRepository.findAll().stream()
                // Idealmente usaríamos Pageable y un Query en Repository, pero esto es funcional para Phase28
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(100)
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
                }).toList();
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
