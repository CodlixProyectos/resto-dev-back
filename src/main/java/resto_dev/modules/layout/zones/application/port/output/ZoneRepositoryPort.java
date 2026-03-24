package resto_dev.modules.layout.zones.application.port.output;

import resto_dev.modules.layout.zones.application.query.SearchZonesQuery;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.shared.common.pagination.PageModel;

import java.util.Optional;
import java.util.UUID;

public interface ZoneRepositoryPort {
    Zone save(Zone zone);

    Optional<Zone> findById(UUID id);

    void deleteById(UUID id);

    boolean existsByName(String name);

    PageModel<Zone> searchZones(SearchZonesQuery query);

    long countActive();
}
