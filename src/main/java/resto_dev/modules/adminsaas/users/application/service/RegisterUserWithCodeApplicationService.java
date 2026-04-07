package resto_dev.modules.adminsaas.users.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.organizations.application.service.SaasOrganizationCreatorService;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.InvitationCodeJpaEntity;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.InvitationCodeJpaRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.CreateSaaSOrganizationRequest;
import resto_dev.modules.adminsaas.users.application.command.RegisterWithCodeCommand;
import resto_dev.modules.adminsaas.users.application.port.input.RegisterUserWithCodeUseCase;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.entity.UserJpaEntity;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.mapper.UserJpaMapper;
import resto_dev.modules.adminsaas.users.infrastructure.persistence.repository.UserJpaRepository;
import resto_dev.shared.errors.ApiException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserWithCodeApplicationService implements RegisterUserWithCodeUseCase {

    private final UserJpaRepository userRepository;
    private final InvitationCodeJpaRepository invitationCodeRepository;
    private final SaasOrganizationCreatorService organizationCreatorService;
    private final UserJpaMapper userMapper;

    @Override
    public User execute(RegisterWithCodeCommand command) {
        log.info("🚀 Starting self-service registration for code: {}", command.invitationCode());

        // 1. Find and validate invitation code
        InvitationCodeJpaEntity invitation = invitationCodeRepository.findByCodeAndUsedFalse(command.invitationCode())
                .orElseThrow(() -> ApiException.badRequest("Código de invitación inválido o ya utilizado"));

        // 2. Check if user already exists
        if (userRepository.existsByEmail(command.email())) {
            throw ApiException.conflict("El email ya está registrado: " + command.email());
        }

        // 3. Trigger Full Organization Creation
        CreateSaaSOrganizationRequest createRequest = new CreateSaaSOrganizationRequest();
        createRequest.setName(command.organizationName());
        createRequest.setOwnerEmail(command.email());
        createRequest.setInitialPlan(invitation.getPlanName());
        createRequest.setTrialDaysCount(invitation.getTrialDays());
        createRequest.setInitialPassword(command.password());
        createRequest.setOwnerFullName(command.fullName());

        organizationCreatorService.create(createRequest);

        // 4. Mark code as used and record organization
        invitation.setUsed(true);
        invitation.setUsedAt(LocalDateTime.now());
        invitation.setUsedByOrganizationName(command.organizationName());
        invitationCodeRepository.save(invitation);

        // 5. Fetch and return the created user
        UserJpaEntity userEntity = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new RuntimeException("Error fatal: Usuario no encontrado después de creación"));

        log.info("✅ Self-service registration completed for restaurant: {}", command.organizationName());

        return userMapper.toDomain(userEntity);
    }
}
