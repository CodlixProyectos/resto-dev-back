package resto_dev.modules.layout.tables.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.layout.tables.application.query.SearchTablesQuery;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.infrastructure.persistence.entity.TableJpaEntity;
import resto_dev.modules.layout.tables.infrastructure.persistence.mapper.TableJpaMapper;
import resto_dev.modules.layout.tables.infrastructure.persistence.repository.TableJpaRepository;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.search.GenericSpecificationBuilder;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TableRepositoryAdapter implements TableRepositoryPort {

    private final TableJpaRepository tableJpaRepository;
    private final TableJpaMapper tableJpaMapper;

    @Override
    public Table save(Table table) {
        TableJpaEntity entity = tableJpaMapper.toEntity(table);
        TableJpaEntity savedEntity = tableJpaRepository.save(entity);
        return tableJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Table> findById(UUID id) {
        return tableJpaRepository.findById(id)
                .map(tableJpaMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        tableJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByTableNumberAndZoneId(String tableNumber, UUID zoneId) {
        return tableJpaRepository.existsByTableNumberAndZoneId(tableNumber, zoneId);
    }

    @Override
    public PageModel<Table> searchTables(SearchTablesQuery query) {
        Specification<TableJpaEntity> spec = Specification.where((Specification<TableJpaEntity>) null);

        if (query.search() != null && !query.search().trim().isEmpty()) {
            spec = spec.and(GenericSpecificationBuilder.searchInFields(query.search(), "tableNumber"));
        }

        if (query.isActive() != null) {
            spec = spec.and(GenericSpecificationBuilder.isEntityActive(query.isActive()));
        }

        if (query.zoneId() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("zone").get("id"), query.zoneId()));
        }

        if (query.status() != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), query.status()));
        }

        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.ASC, "tableNumber"));
        Page<TableJpaEntity> pageResult = tableJpaRepository.findAll(spec, pageable);

        return new PageModel<>(
                pageResult.getContent().stream().map(tableJpaMapper::toDomain).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());
    }
}
