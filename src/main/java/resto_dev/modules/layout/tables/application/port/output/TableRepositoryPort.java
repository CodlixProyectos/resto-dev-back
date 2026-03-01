package resto_dev.modules.layout.tables.application.port.output;

import resto_dev.modules.layout.tables.application.query.SearchTablesQuery;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.shared.common.pagination.PageModel;

import java.util.Optional;
import java.util.UUID;

public interface TableRepositoryPort {
    Table save(Table table);

    Optional<Table> findById(UUID id);

    void deleteById(UUID id);

    boolean existsByTableNumberAndZoneId(String tableNumber, UUID zoneId);

    PageModel<Table> searchTables(SearchTablesQuery query);
}
