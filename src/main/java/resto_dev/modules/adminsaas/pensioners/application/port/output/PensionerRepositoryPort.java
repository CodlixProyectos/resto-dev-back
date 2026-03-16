package resto_dev.modules.adminsaas.pensioners.application.port.output;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.adminsaas.pensioners.domain.model.Pensioner;

import java.util.Optional;
import java.util.UUID;

public interface PensionerRepositoryPort {
    Pensioner save(Pensioner pensioner);
    Optional<Pensioner> findById(UUID id);
    Page<Pensioner> findAllByOrganizationId(UUID organizationId, Pageable pageable);
    void deleteById(UUID id);
}
