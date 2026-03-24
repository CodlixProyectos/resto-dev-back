package resto_dev.modules.layout.tables.application.port.input;

import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.infrastructure.web.dto.input.BulkCreateTableRequest;

import java.util.List;

public interface BulkCreateTableUseCase {
    List<Table> execute(BulkCreateTableRequest request);
}
