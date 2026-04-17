package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.OrganizationJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.CreateSaaSOrganizationRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.CreateSaaSOrganizationResponse;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.shared.security.permissions.RoleEntity;
import resto_dev.shared.security.permissions.RoleRepository;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.*;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.entity.UserJpaEntity;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.repository.UserJpaRepository;
import resto_dev.shared.tenancy.FlywayConfig;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import resto_dev.shared.messaging.RabbitMqConfig;
import resto_dev.shared.messaging.dto.TenantProvisioningMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaasOrganizationCreatorService {

    private final UserJpaRepository userRepository;
    private final OrganizationJpaRepository organizationRepository;
    private final OrganizationSubscriptionJpaRepository subscriptionRepository;
    private final SubscriptionPlanJpaRepository planRepository;
    private final OrganizationMemberJpaRepository memberRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate; // Inyectamos RabbitTemplate para enviar mensajes
    private final DataSource dataSource;

    @Transactional
    public CreateSaaSOrganizationResponse create(CreateSaaSOrganizationRequest request) {
        log.info("Creating new SaaS organization: {}", request.getName());

        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // 1. Generate temp password
        String tempPassword = generateRandomPassword();
        
        // 2. Create Owner
        UserJpaEntity owner = UserJpaEntity.builder()
                .email(request.getOwnerEmail())
                .passwordHash(request.getInitialPassword() != null ? 
                    passwordEncoder.encode(request.getInitialPassword()) : 
                    passwordEncoder.encode(tempPassword))
                .fullName(request.getOwnerFullName() != null ? request.getOwnerFullName() : "Adm. " + request.getName())
                .active(true)
                .superAdmin(false)
                .build();
        owner = userRepository.save(owner);

        // 3. Generate Slug and Schema Name
        String slug = generateSlug(request.getName());
        String schemaName = "client_" + slug.replace("-", "_");

        // 4. Create Organization
        OrganizationJpaEntity organization = OrganizationJpaEntity.builder()
                .name(request.getName())
                .slug(slug)
                .schemaName(schemaName)
                .ownerId(owner.getId())
                .email(request.getOwnerEmail())
                .active(false) // Desactivado hasta que el esquema esté listo
                .registrationStatus("PENDING_SETUP") // Marcamos como pendiente de configuración
                .type("restaurant")
                .build();
        organization = organizationRepository.save(organization);

        // 5. Create Subscription
        SubscriptionPlanJpaEntity plan = planRepository.findByName(request.getInitialPlan())
                .orElseGet(() -> planRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No hay planes de suscripción configurados")));

        OrganizationSubscriptionJpaEntity subscription = OrganizationSubscriptionJpaEntity.builder()
                .organization(organization)
                .plan(plan)
                .status("TRIAL".equals(request.getInitialPlan()) ? "TRIAL" : "ACTIVE")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(request.getTrialDaysCount()))
                .build();
        subscriptionRepository.save(subscription);

        // 6. Add owner as ADMIN member of the organization
        RoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado en la base de datos"));

        OrganizationMemberJpaEntity member = OrganizationMemberJpaEntity.builder()
                .organization(organization)
                .user(owner)
                .role(adminRole)
                .status("ACTIVE")
                .active(true)
                .build();
        memberRepository.save(member);
        log.info("✅ Owner added as ADMIN member of org: {}", organization.getName());

        // 7. PUBLICACIÓN EN RABBITMQ (¡La magia asíncrona!)
        // En lugar de llamar a Flyway aquí, enviamos un mensaje al exchange.
        log.info("Sending provisioning message for schema: {}", schemaName);
        TenantProvisioningMessage message = new TenantProvisioningMessage(organization.getId(), schemaName);
        
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.TENANT_EXCHANGE, 
            RabbitMqConfig.TENANT_PROVISIONING_ROUTING_KEY, 
            message
        );
        log.info("Message sent to RabbitMQ successfully.");

        return CreateSaaSOrganizationResponse.builder()
                .organizationId(organization.getId().toString())
                .name(organization.getName())
                .slug(organization.getSlug())
                .schemaName(organization.getSchemaName())
                .temporaryPassword(tempPassword)
                .build();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
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
