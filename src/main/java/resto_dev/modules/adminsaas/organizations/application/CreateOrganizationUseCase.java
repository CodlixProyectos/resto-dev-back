package resto_dev.modules.adminsaas.organizations.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.organizations.domain.Organization;
import resto_dev.modules.adminsaas.organizations.ports.in.CreateOrganizationPort;
import resto_dev.modules.adminsaas.organizations.ports.in.dto.CreateOrganizationCommand;
import resto_dev.modules.adminsaas.organizations.ports.out.OrganizationRepositoryPort;
import resto_dev.shared.errors.ApiException;
import resto_dev.shared.tenancy.SchemaService;

import java.util.UUID;

/**
 * Use case: Create a new organization (tenant).
 * 1. Validates slug uniqueness
 * 2. Generates schema name: client_{uuid12}
 * 3. Persists the organization record in public schema
 * 4. Creates the PostgreSQL schema automatically
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateOrganizationUseCase implements CreateOrganizationPort {

    private final OrganizationRepositoryPort organizationRepository;
    private final SchemaService schemaService;

    @Override
    public Organization execute(CreateOrganizationCommand command, UUID ownerId) {
        if (organizationRepository.existsBySlug(command.slug())) {
            throw ApiException.conflict("Slug already taken: " + command.slug());
        }

        String schemaName = "client_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        Organization organization = Organization.builder()
                .name(command.name())
                .slug(command.slug())
                .schemaName(schemaName)
                .type(command.type() != null ? command.type() : "restaurant")
                .ownerId(ownerId)
                .active(true)
                .build();

        Organization saved = organizationRepository.save(organization);

        schemaService.createSchema(schemaName);

        log.info("🏢 Organization '{}' ({}) created with schema '{}'",
                saved.getName(), saved.getType(), schemaName);
        return saved;
    }
}
