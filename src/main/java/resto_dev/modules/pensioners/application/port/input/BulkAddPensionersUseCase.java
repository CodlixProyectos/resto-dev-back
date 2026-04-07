package resto_dev.modules.pensioners.application.port.input;

import java.util.List;
import java.util.UUID;

public interface BulkAddPensionersUseCase {
    
    record BulkPensionerRequest(
        String fullName,
        String dni,
        String email,
        String phoneNumber
    ) {}

    void execute(UUID organizationId, List<BulkPensionerRequest> pensioners);
}
