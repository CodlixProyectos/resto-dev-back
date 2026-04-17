package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import resto_dev.shared.messaging.RabbitMqConfig;
import resto_dev.shared.messaging.dto.TenantProvisioningMessage;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
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
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Organization execute(CreateOrganizationCommand command, UUID superAdminId) {
        String slug = command.slug();
        if (slug == null || slug.isBlank()) {
            slug = generateSlug(command.name());
        }

        if (organizationRepository.existsBySlug(slug)) {
            throw ApiException.conflict("Slug already taken: " + slug);
        }

        String schemaName = "client_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // 1. Process Owner (Create if not exists)
        String initialPassword = null;
        UserJpaEntity owner = userRepository.findByEmail(command.ownerEmail()).orElse(null);

        if (owner == null) {
            log.info("👤 Creating new owner user for email: {}", command.ownerEmail());
            initialPassword = UUID.randomUUID().toString().substring(0, 8);
            
            owner = UserJpaEntity.builder()
                    .fullName(command.ownerName())
                    .email(command.ownerEmail())
                    .passwordHash(passwordEncoder.encode(initialPassword))
                    .active(true)
                    .superAdmin(false)
                    .build();
            
            owner = userRepository.save(owner);
        }

        // 2. Create Organization
        Organization organization = Organization.builder()
                .name(command.name())
                .slug(slug)
                .schemaName(schemaName)
                .type(command.type() != null ? command.type() : "restaurant")
                .ownerId(owner.getId())
                .email(command.ownerEmail())
                .active(false) // Deactivated until schema is ready
                .registrationStatus("PENDING_SETUP")
                .invitationCode(generateInitialCode())
                .initialPassword(initialPassword) // Ephemeral
                .build();

        Organization saved = organizationRepository.save(organization);

        log.info("Attempting to send provisioning message for schema: {}", schemaName);
        try {
            TenantProvisioningMessage message = new TenantProvisioningMessage(saved.getId(), schemaName);
            rabbitTemplate.convertAndSend(
                RabbitMqConfig.TENANT_EXCHANGE, 
                RabbitMqConfig.TENANT_PROVISIONING_ROUTING_KEY, 
                message
            );
            log.info("Message sent to RabbitMQ successfully (Async).");
        } catch (Exception e) {
            log.warn("⚠️ RabbitMQ is DOWN. Falling back to Synchronous provisioning: {}", e.getMessage());
            // Fallback: execute synchronously if RabbitMQ is not available
            schemaService.createSchema(schemaName);
            
            // Mark as active immediately since it's already done
            saved.setActive(true);
            saved.setRegistrationStatus("ACTIVE");
            organizationRepository.save(saved);
        }

        log.info("🏢 Organization '{}' created with owner '{}' and schema '{}'",
                saved.getName(), owner.getEmail(), schemaName);

        // 3. Automatically add owner as member with OWNER role
        addOwnerAsMember(saved.getId(), owner.getId());

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

    private String generateInitialCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("RT-");
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                + "-" + (System.currentTimeMillis() % 1000);
    }
}
