package resto_dev.modules.layout.zones.application.port.input;

import resto_dev.modules.layout.zones.application.query.SearchZonesQuery;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.shared.common.pagination.PageModel;

public interface ListZonesUseCase {
    PageModel<Zone> execute(SearchZonesQuery query);
}
