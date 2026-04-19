package resto_dev.modules.inventory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.inventory.infrastructure.persistence.entity.SupplierJpaEntity;
import resto_dev.modules.inventory.infrastructure.persistence.repository.SupplierJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierJpaRepository supplierRepository;

    @Transactional(readOnly = true)
    public Page<SupplierJpaEntity> getAllActive(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return supplierRepository.findAllByActiveTrueAndNameContainingIgnoreCase(search, pageable);
        }
        return supplierRepository.findAllByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public List<SupplierJpaEntity> getAllActiveList() {
        return supplierRepository.findAllByActiveTrue();
    }

    @Transactional(readOnly = true)
    public SupplierJpaEntity getById(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id));
    }

    @Transactional
    public SupplierJpaEntity save(SupplierJpaEntity supplier) {
        if (supplier.getId() == null) {
            supplier.setCreatedAt(LocalDateTime.now());
            supplier.setActive(true);
        } else {
            SupplierJpaEntity existing = getById(supplier.getId());
            supplier.setCreatedAt(existing.getCreatedAt());
            supplier.setUpdatedAt(LocalDateTime.now());
        }
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void delete(UUID id) {
        SupplierJpaEntity supplier = getById(id);
        supplier.setActive(false);
        supplier.setUpdatedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
        log.info("Supplier soft-deleted: {}", id);
    }

    public record ImportReport(
            int totalProcessed,
            int duplicatesCount,
            List<String> duplicateNames
    ) {}

    @Transactional
    public ImportReport bulkUploadSuppliers(List<resto_dev.modules.inventory.infrastructure.excel.SupplierExcelParser.SupplierExcelRow> rows) {
        int processed = 0;
        int duplicates = 0;
        List<String> duplicateNames = new java.util.ArrayList<>();

        for (var row : rows) {
            try {
                String trimmedName = row.name().trim();
                
                // Check duplicate
                if (supplierRepository.existsByNameIgnoreCaseAndActiveTrue(trimmedName)) {
                    duplicates++;
                    duplicateNames.add(trimmedName);
                    continue;
                }

                SupplierJpaEntity supplier = SupplierJpaEntity.builder()
                        .name(trimmedName)
                        .contactName(row.contactName())
                        .phone(row.phone())
                        .email(row.email())
                        .address(row.address())
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                supplierRepository.save(supplier);
                processed++;
            } catch (Exception e) {
                log.error("Failed to upload supplier row: {}. Reason: {}", row.name(), e.getMessage());
            }
        }
        return new ImportReport(processed, duplicates, duplicateNames);
    }
}
