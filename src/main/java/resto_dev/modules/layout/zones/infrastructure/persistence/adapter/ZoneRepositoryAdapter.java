package resto_dev.modules.layout.zones.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import resto_dev.modules.layout.zones.application.port.output.ZoneRepositoryPort;
import resto_dev.modules.layout.zones.application.query.SearchZonesQuery;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.modules.layout.zones.infrastructure.persistence.entity.ZoneJpaEntity;
import resto_dev.modules.layout.zones.infrastructure.persistence.mapper.ZoneJpaMapper;
import resto_dev.modules.layout.zones.infrastructure.persistence.repository.ZoneJpaRepository;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.search.GenericSpecificationBuilder;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ZoneRepositoryAdapter implements ZoneRepositoryPort {

    private final ZoneJpaRepository zoneJpaRepository;
    private final ZoneJpaMapper zoneJpaMapper;

    @Override
    public Zone save(Zone zone) {
        ZoneJpaEntity entity = zoneJpaMapper.toEntity(zone);
        ZoneJpaEntity savedEntity = zoneJpaRepository.save(entity);
        return zoneJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Zone> findById(UUID id) {
        return zoneJpaRepository.findById(id)
                .map(zoneJpaMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        zoneJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return zoneJpaRepository.existsByName(name);
    }

    @Override
    public PageModel<Zone> searchZones(SearchZonesQuery query) {
        Specification<ZoneJpaEntity> spec = Specification.where((Specification<ZoneJpaEntity>) null);

        if (query.search() != null && !query.search().trim().isEmpty()) {
            spec = spec.and(GenericSpecificationBuilder.searchInFields(query.search(), "name", "description"));
        }

        if (query.isActive() != null) {
            spec = spec.and(GenericSpecificationBuilder.isEntityActive(query.isActive()));
        }

        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ZoneJpaEntity> pageResult = zoneJpaRepository.findAll(spec, pageable);

        return new PageModel<>(
                pageResult.getContent().stream().map(zoneJpaMapper::toDomain).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages());
    }
}
