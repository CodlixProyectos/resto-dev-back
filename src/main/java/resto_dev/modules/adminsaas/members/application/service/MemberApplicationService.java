package resto_dev.modules.adminsaas.members.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.adminsaas.members.application.port.input.AddOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.DeactivateOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.PinLoginOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.GetStaffStatsUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.UpdateOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.GetOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.GetStaffPerformanceUseCase;
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
        GetOrganizationMemberUseCase,
        GetStaffPerformanceUseCase {

    private final MemberRepositoryPort memberRepository;
    private final UserRepositoryPort userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final resto_dev.modules.sales.orders.application.port.output.OrderRepositoryPort orderRepository;

    @Override
    public OrganizationMember execute(UUID organizationId, String fullName, String email, String dni, String phoneNumber, String roleName, String pin, BigDecimal salary) {
        // --- VALIDACIONES DE ENTRADA ---
        if (fullName == null || fullName.trim().isBlank()) {
            throw ApiException.badRequest("El nombre completo es obligatorio.");
        }
        if (pin == null || pin.trim().length() < 4 || pin.trim().length() > 6) {
            throw ApiException.badRequest("El PIN debe tener entre 4 y 6 dígitos.");
        }
        if (roleName == null || roleName.trim().isBlank()) {
            throw ApiException.badRequest("El rol es obligatorio.");
        }

        // Validar unicidad de DNI en la organización (si se proporciona)
        if (dni != null && !dni.trim().isBlank()) {
            memberRepository.findAllByOrganization(organizationId).stream()
                .filter(m -> {
                    // Obtener el DNI del usuario asociado
                    return userRepository.findById(m.getUserId())
                        .map(u -> dni.equals(u.getDni()))
                        .orElse(false);
                })
                .findAny()
                .ifPresent(existingMember -> {
                    throw ApiException.conflict("Ya existe un empleado con el DNI '" + dni + "' en esta organización.");
                });
        }

        if (salary != null && salary.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.badRequest("El salario no puede ser negativo.");
        }
        // -------------------------------

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
                        .ifPresent(alreadyMember -> {
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

        // El PIN se hashea antes de guardarse para máxima seguridad
        OrganizationMember newMember = OrganizationMember.builder()
                .organizationId(organizationId)
                .userId(user.getId())
                .roleId(role.getId())
                .pin(passwordEncoder.encode(pin))
                .active(true)
                .status("ACTIVE")
                .salary(salary)
                .joinedAt(LocalDateTime.now())
                .build();

        return memberRepository.save(newMember);
    }

    private boolean matchesAndMigratePin(String rawPin, OrganizationMember member) {
        boolean matches = passwordEncoder.matches(rawPin, member.getPin());
        
        // Transparent migration: if it doesn't match as hash but matches as plain text
        if (!matches && rawPin.equals(member.getPin())) {
            member.setPin(passwordEncoder.encode(rawPin));
            memberRepository.save(member);
            return true;
        }
        return matches;
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
    public OrganizationMember execute(UUID organizationId, UUID memberId, UUID newRoleId, String currentPin, String newPin, BigDecimal newSalary, String newStatus, String newRoleName) {
        OrganizationMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("Staff member not found"));

        if (!member.getOrganizationId().equals(organizationId)) {
            throw ApiException.forbidden("No puedes modificar miembros de otra organización");
        }

        // --- PROTECCIÓN DE CUENTA ADMINISTRATIVA ---
        // Obtener el ID del usuario autenticado desde el contexto de seguridad
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UUID authenticatedUserId) {
            // Si el usuario está intentando modificarse a sí mismo
            if (member.getUserId().equals(authenticatedUserId)) {
                // Verificar si es un OWNER o ADMIN (puedes ajustar según tus IDs de roles si es necesario o por nombre)
                String currentRoleName = roleRepository.findById(member.getRoleId()).map(RoleEntity::getName).orElse("");
                
                // Si intenta cambiar su ROLE o su STATUS y es dueño, lo bloqueamos
                if (currentRoleName.equals("OWNER") || currentRoleName.equals("ADMIN")) {
                    if ((newRoleName != null && !newRoleName.equalsIgnoreCase(currentRoleName)) || 
                        (newRoleId != null && !newRoleId.equals(member.getRoleId())) ||
                        (newStatus != null && !newStatus.equalsIgnoreCase(member.getStatus()))) {
                        throw ApiException.forbidden("No puedes cambiar tu propio rol o estado siendo el Administrador Principal.");
                    }
                }
            }
        }
        // --------------------------------------------

        if (newRoleName != null && !newRoleName.isBlank()) {
            RoleEntity role = roleRepository.findByName(newRoleName.toUpperCase())
                    .orElseThrow(() -> ApiException.badRequest("Rol '" + newRoleName + "' no encontrado."));
            member.setRoleId(role.getId());
        } else if (newRoleId != null) {
            member.setRoleId(newRoleId);
        }

        if (newPin != null && !newPin.isBlank()) {
            // Validar PIN actual si se intenta cambiar (Seguridad)
            if (member.getPin() != null && (currentPin == null || !member.getPin().equals(currentPin))) {
                throw ApiException.unauthorized("El PIN actual es incorrecto");
            }
            member.setPin(newPin); // Positively fast lookup
        }
        if (newSalary != null) {
            member.setSalary(newSalary);
        }
        if (newStatus != null && !newStatus.isBlank()) {
            member.setStatus(newStatus.toUpperCase());
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

        // Bloquear auto-desactivación del OWNER
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UUID authenticatedUserId && member.getUserId().equals(authenticatedUserId)) {
            String currentRoleName = roleRepository.findById(member.getRoleId()).map(RoleEntity::getName).orElse("");
            if (currentRoleName.equals("OWNER") || currentRoleName.equals("ADMIN")) {
                throw ApiException.forbidden("No puedes desactivar tu propia cuenta desde aquí.");
            }
        }

        member.setActive(false);
        memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult execute(UUID organizationId, String email, String dni, String pin) {
        OrganizationMember member = null;

        // Unified Search Strategy: Find member by identifier across ALL organizations
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> ApiException.unauthorized("Email no registrado."));
            member = memberRepository.findByUserId(user.getId()).stream()
                    .filter(m -> m.isActive() && matchesAndMigratePin(pin, m))
                    .findFirst()
                    .orElseThrow(() -> ApiException.unauthorized("PIN incorrecto."));
        } else if (dni != null && !dni.isBlank()) {
            User user = userRepository.findByDni(dni)
                    .orElseThrow(() -> ApiException.unauthorized("DNI no registrado."));
            member = memberRepository.findByUserId(user.getId()).stream()
                    .filter(m -> m.isActive() && matchesAndMigratePin(pin, m))
                    .findFirst()
                    .orElseThrow(() -> ApiException.unauthorized("PIN incorrecto o local no encontrado."));
        } else if (organizationId != null) {
            member = memberRepository.findAllByOrganization(organizationId).stream()
                    .filter(m -> m.isActive() && matchesAndMigratePin(pin, m))
                    .findFirst()
                    .orElseThrow(() -> ApiException.unauthorized("PIN incorrecto en este local."));
        }

        if (member == null) {
            throw ApiException.badRequest("Se requiere Email, DNI o Local para el login.");
        }

        if (!member.isActive()) {
            throw ApiException.forbidden("Tu cuenta de empleado está desactivada.");
        }

        // Fetch User details for the token (needed for name, superAdmin, etc.)
        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> ApiException.notFound("User not found"));
        // Generate a standard JWT token impersonating this user
        String token = jwtProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.isSuperAdmin());

        // Fetch Role Name
        String roleName = roleRepository.findById(member.getRoleId())
                .map(RoleEntity::getName)
                .orElse("WAITER");

        return new AuthResult(
                token,
                user.getId(),
                member.getId(),
                member.getOrganizationId(),
                user.getEmail(),
                user.getFullName(),
                user.isSuperAdmin(),
                roleName,
                user.isNotificationsEnabled(),
                user.isSoundEnabled(),
                user.isDarkModeEnabled());
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

    @Override
    @Transactional(readOnly = true)
    public StaffPerformance execute(StaffPerformanceQuery query) {
        // En este sistema POS, usamos userId (del token) como waiterId en las órdenes
        long ordersToday = orderRepository.countByWaiterAndDate(query.userId(), java.time.LocalDate.now());
        
        // Tablas asignadas: Podríamos contar tablas únicas en órdenes activas del mesero
        // Por ahora lo dejamos en 0 o implementamos una búsqueda simple si es necesario
        int tablesAssigned = 0; 
        
        return new StaffPerformance((int) ordersToday, tablesAssigned, 5.0);
    }
}
