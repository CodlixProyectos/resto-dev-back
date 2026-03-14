package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.adminsaas.organizations.application.port.input.CreateOrganizationUseCase;
import resto_dev.modules.adminsaas.organizations.application.command.CreateOrganizationCommand;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.entity.UserJpaEntity;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.repository.UserJpaRepository;
import resto_dev.shared.errors.ApiException;
import resto_dev.shared.security.permissions.PermissionEntity;
import resto_dev.shared.security.permissions.PermissionRepository;
import resto_dev.shared.security.permissions.RoleEntity;
import resto_dev.shared.security.permissions.RoleRepository;
import resto_dev.shared.tenancy.SchemaService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Use case: Create a new organization (tenant).
 * 1. Validates slug uniqueness
 * 2. Generates schema name: client_{uuid12}
 * 3. Persists the organization record in public schema
 * 4. Creates the PostgreSQL schema automatically
 * 5. Automatically adds owner as member with OWNER role
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateOrganizationApplicationService implements CreateOrganizationUseCase {

    private final OrganizationRepositoryPort organizationRepository;
    private final SchemaService schemaService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final OrganizationMemberJpaRepository memberRepository;
    private final UserJpaRepository userRepository;
    private final OrganizationJpaRepository organizationJpaRepository;

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

        // ✅ FIX: Automatically add owner as member with OWNER role
        addOwnerAsMember(saved.getId(), ownerId);

        return saved;
    }

    /**
     * Automatically adds the owner as a member of the organization with OWNER role.
     * Creates the OWNER role with all permissions if it doesn't exist.
     */
    private void addOwnerAsMember(UUID organizationId, UUID ownerId) {
        // Get or create OWNER role with full permissions
        RoleEntity ownerRole = roleRepository.findByName("OWNER")
                .orElseGet(() -> {
                    log.info("🔑 Creating OWNER role with full permissions");
                    
                    // Get all available permissions
                    Set<PermissionEntity> allPermissions = new HashSet<>(permissionRepository.findAll());
                    
                    RoleEntity newRole = RoleEntity.builder()
                            .name("OWNER")
                            .description("Organization Owner - Full Access")
                            .system(true)
                            .permissions(allPermissions)
                            .build();
                    
                    return roleRepository.save(newRole);
                });

        // Get user and organization entities
        UserJpaEntity user = userRepository.findById(ownerId)
                .orElseThrow(() -> ApiException.notFound("User not found: " + ownerId));
        
        OrganizationJpaEntity org = organizationJpaRepository.findById(organizationId)
                .orElseThrow(() -> ApiException.notFound("Organization not found: " + organizationId));

        // Check if membership already exists
        boolean alreadyMember = memberRepository.existsByOrganizationIdAndUserIdAndRoleId(
                organizationId, ownerId, ownerRole.getId());
        
        if (!alreadyMember) {
            OrganizationMemberJpaEntity member = OrganizationMemberJpaEntity.builder()
                    .organization(org)
                    .user(user)
                    .role(ownerRole)
                    .active(true)
                    .joinedAt(LocalDateTime.now())
                    .build();
            
            memberRepository.save(member);
            log.info("✅ Owner {} automatically added as member of organization {}", ownerId, organizationId);
        } else {
            log.debug("Owner {} already a member of organization {}", ownerId, organizationId);
        }
    }
}
