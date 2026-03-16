package resto_dev.modules.adminsaas.members.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.members.application.port.input.AddOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.DeactivateOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.PinLoginOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.GetStaffStatsUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.UpdateOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.GetOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.output.MemberRepositoryPort;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.domain.model.StaffStats;
import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.shared.errors.ApiException;
import resto_dev.shared.security.jwt.JwtProvider;
import resto_dev.shared.security.permissions.RoleEntity;
import resto_dev.shared.security.permissions.RoleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberApplicationService implements
        AddOrganizationMemberUseCase,
        UpdateOrganizationMemberUseCase,
        DeactivateOrganizationMemberUseCase,
        PinLoginOrganizationMemberUseCase,
        GetStaffStatsUseCase,
        GetOrganizationMemberUseCase {

    private final MemberRepositoryPort memberRepository;
    private final UserRepositoryPort userRepository;
    private final JwtProvider jwtProvider;
    private final RoleRepository roleRepository;

    @Override
    public OrganizationMember execute(UUID organizationId, String fullName, String email, String dni, String phoneNumber, String roleName, String pin, BigDecimal salary) {
        // Buscar el rol por nombre
        RoleEntity role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> ApiException.badRequest("Rol '" + roleName + "' no encontrado. Roles disponibles: OWNER, MANAGER, WAITER, KITCHEN."));
        // Si se proporcionó email, verificar si el usuario ya existe
        User user;
        if (email != null && !email.isBlank()) {
            var existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Verificar que no sea ya miembro de esta organización
                memberRepository.findByOrganizationAndUser(organizationId, user.getId())
                        .ifPresent(m -> {
                            throw ApiException.conflict("Este usuario ya pertenece al restaurante.");
                        });
            } else {
                // Crear usuario automáticamente con el email proporcionado
                user = createStaffUser(fullName, email, dni, phoneNumber);
            }
        } else {
            // Sin email: generar un email interno único para el sistema
            String autoEmail = "staff_" + UUID.randomUUID().toString().substring(0, 8) + "@internal.pos";
            user = createStaffUser(fullName, autoEmail, dni, phoneNumber);
        }

        // Verificar que el PIN no esté en uso en esta organización
        memberRepository.findByOrganizationAndPin(organizationId, pin)
                .ifPresent(m -> {
                    throw ApiException.conflict("Este PIN ya está en uso por otro empleado en esta organización.");
                });

        OrganizationMember newMember = OrganizationMember.builder()
                .organizationId(organizationId)
                .userId(user.getId())
                .roleId(role.getId())
                .pin(pin)
                .active(true)
                .status("ACTIVE")
                .salary(salary)
                .joinedAt(LocalDateTime.now())
                .build();

        return memberRepository.save(newMember);
    }

    /**
     * Crea un usuario del sistema para un empleado del staff.
     * El usuario creado no tendrá contraseña (solo acceso POS vía PIN).
     */
    private User createStaffUser(String fullName, String email, String dni, String phoneNumber) {
        User newUser = User.builder()
                .email(email)
                .passwordHash("$NO_LOGIN$") // Staff user — no password login, POS PIN only
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .dni(dni)
                .superAdmin(false)
                .active(true)
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public OrganizationMember execute(UUID organizationId, UUID memberId, UUID newRoleId, String newPin, BigDecimal newSalary) {
        OrganizationMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("Staff member not found"));

        if (!member.getOrganizationId().equals(organizationId)) {
            throw ApiException.forbidden("No puedes modificar miembros de otra organización");
        }

        member.setRoleId(newRoleId);
        if (newPin != null && !newPin.isBlank()) {
            member.setPin(newPin); // Positively fast lookup
        }
        member.setSalary(newSalary);

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

    @Override
    @Transactional(readOnly = true)
    public StaffStats execute(UUID organizationId) {
        return memberRepository.getStats(organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationMember getMember(UUID organizationId, UUID memberId) {
        OrganizationMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("Staff member not found"));

        if (!member.getOrganizationId().equals(organizationId)) {
            throw ApiException.forbidden("No puedes acceder a miembros de otra organización");
        }

        return member;
    }
}
