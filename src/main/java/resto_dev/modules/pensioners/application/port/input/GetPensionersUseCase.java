package resto_dev.modules.pensioners.application.port.input;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.pensioners.domain.model.Pensioner;

import java.util.UUID;

public interface GetPensionersUseCase {
    Page<Pensioner> execute(UUID organizationId, Pageable pageable, String search);
}
