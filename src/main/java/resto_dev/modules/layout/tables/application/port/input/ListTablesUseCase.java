package resto_dev.modules.layout.tables.application.port.input;

import resto_dev.modules.layout.tables.application.query.SearchTablesQuery;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.shared.common.pagination.PageModel;

public interface ListTablesUseCase {
    PageModel<Table> execute(SearchTablesQuery query);
}
