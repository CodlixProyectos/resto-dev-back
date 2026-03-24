package resto_dev.modules.layout.tables.application.port.input;

import resto_dev.modules.layout.tables.application.command.CreateTableCommand;
import resto_dev.modules.layout.tables.domain.model.Table;

public interface CreateTableUseCase {
    Table execute(CreateTableCommand command);
}
