package resto_dev.modules.adminsaas.organizations.application.port.output;

import resto_dev.modules.adminsaas.organizations.domain.model.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for organization persistence.
 */
public interface OrganizationRepositoryPort {

    Organization save(Organization organization);

    Optional<Organization> findById(UUID id);

    Optional<Organization> findBySlug(String slug);

    List<Organization> findByOwnerId(UUID ownerId);

    boolean existsBySlug(String slug);
}
