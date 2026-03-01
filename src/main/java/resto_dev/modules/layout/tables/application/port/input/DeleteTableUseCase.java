package resto_dev.modules.layout.tables.application.port.input;

import java.util.UUID;

public interface DeleteTableUseCase {
    void execute(UUID id);
}
