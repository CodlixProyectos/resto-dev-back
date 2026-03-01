package resto_dev.modules.adminsaas.members.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.members.application.port.input.AddOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.DeactivateOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.PinLoginOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.UpdateOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.output.MemberRepositoryPort;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.shared.errors.ApiException;
import resto_dev.shared.security.jwt.JwtProvider;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberApplicationService implements
        AddOrganizationMemberUseCase,
        UpdateOrganizationMemberUseCase,
        DeactivateOrganizationMemberUseCase,
        PinLoginOrganizationMemberUseCase {

    private final MemberRepositoryPort memberRepository;
    private final UserRepositoryPort userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public OrganizationMember execute(UUID organizationId, String email, UUID roleId, String pin) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> ApiException.badRequest("El empleado (" + email + ") debe registrar su cuenta primero."));

        memberRepository.findByOrganizationAndUser(organizationId, user.getId())
                .ifPresent(m -> {
                    throw ApiException.conflict("Este usuario ya pertenece al restaurante.");
                });

        OrganizationMember newMember = OrganizationMember.builder()
                .organizationId(organizationId)
                .userId(user.getId())
                .roleId(roleId)
                .pin(pin) // Store as plain text or deterministic hash (SHA-256) for fast POS lookup
                .active(true)
                .joinedAt(LocalDateTime.now())
                .build();

        return memberRepository.save(newMember);
    }

    @Override
    public OrganizationMember execute(UUID organizationId, UUID memberId, UUID newRoleId, String newPin) {
        OrganizationMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("Staff member not found"));

        if (!member.getOrganizationId().equals(organizationId)) {
            throw ApiException.forbidden("No puedes modificar miembros de otra organización");
        }

        member.setRoleId(newRoleId);
        if (newPin != null && !newPin.isBlank()) {
            member.setPin(newPin); // Positively fast lookup
        }

        return memberRepository.save(member);
    }

    @Override
    public void execute(UUID organizationId, UUID memberId) {
        OrganizationMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("Staff member not found"));

        if (!member.getOrganizationId().equals(organizationId)) {
            throw ApiException.forbidden("No puedes modificar miembros de otra organización");
        }

        member.setActive(false);
        memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult execute(UUID organizationId, String pin) {
        OrganizationMember member = memberRepository.findByOrganizationAndPin(organizationId, pin)
                .orElseThrow(() -> ApiException.unauthorized("PIN incorrecto."));

        if (!member.isActive()) {
            throw ApiException.forbidden("El empleado está desactivado.");
        }

        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> ApiException.badRequest("Usuario asociado no encontrado."));

        // Generate a standard JWT token impersonating this user
        String token = jwtProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.isSuperAdmin());

        return new AuthResult(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isSuperAdmin());
    }
}
