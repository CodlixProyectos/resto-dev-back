package resto_dev.modules.layout.tables.application.port.input;

import resto_dev.modules.layout.tables.application.command.UpdateTableCommand;
import resto_dev.modules.layout.tables.domain.model.Table;

import java.util.UUID;

public interface UpdateTableUseCase {
    Table execute(UUID id, UpdateTableCommand command);
}
