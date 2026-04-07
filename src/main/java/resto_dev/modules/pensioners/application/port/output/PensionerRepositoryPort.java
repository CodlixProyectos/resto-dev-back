package resto_dev.modules.pensioners.application.port.output;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import resto_dev.modules.pensioners.domain.model.Pensioner;

import java.util.Optional;
import java.util.UUID;

public interface PensionerRepositoryPort {
    Pensioner save(Pensioner pensioner);
    Optional<Pensioner> findById(UUID id);
    Page<Pensioner> findAllByOrganizationId(UUID organizationId, Pageable pageable, String search);
    boolean existsByOrganizationIdAndDni(UUID organizationId, String dni);
    boolean existsByOrganizationIdAndEmail(UUID organizationId, String email);
    void deleteById(UUID id);
}
