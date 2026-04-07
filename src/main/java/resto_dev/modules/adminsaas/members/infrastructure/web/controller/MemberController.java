package resto_dev.modules.adminsaas.members.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.members.application.port.input.*;
import resto_dev.modules.adminsaas.members.application.query.GetOrganizationMembersQuery;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.infrastructure.web.dto.input.*;
import resto_dev.modules.adminsaas.members.infrastructure.web.dto.output.OrganizationMemberResponse;
import resto_dev.modules.adminsaas.members.infrastructure.web.dto.output.StaffStatsResponse;
import resto_dev.modules.adminsaas.users.application.port.output.UserRepositoryPort;
import resto_dev.modules.adminsaas.users.domain.model.User;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;
import resto_dev.shared.security.permissions.RoleEntity;
import resto_dev.shared.security.permissions.RoleRepository;
// import java.math.BigDecimal; // Removed because it's reported as unused

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
@RequiredArgsConstructor
@Tag(name = "Organization Staff / Members", description = "Endpoints para gestionar el personal de una organización (contratar, cambiar rol, despedir) y login POS.")
public class MemberController {

        private final GetMyPermissionsUseCase getMyPermissionsPort;
        private final GetOrganizationMembersUseCase getOrganizationMembersUseCase;
        private final GetOrganizationMemberUseCase getOrganizationMemberUseCase;
        private final GetStaffStatsUseCase getStaffStatsUseCase;
        private final AddOrganizationMemberUseCase addOrganizationMemberUseCase;
        private final UpdateOrganizationMemberUseCase updateOrganizationMemberUseCase;
        private final DeactivateOrganizationMemberUseCase deactivateOrganizationMemberUseCase;
        private final UserRepositoryPort userRepositoryPort;
        private final RoleRepository roleRepository;

        @Operation(summary = "Obtener mis permisos", description = "Devuelve los permisos del usuario actual en esta empresa.")
        @GetMapping("/my-permissions")
        public ResponseEntity<ApiResponse<List<String>>> getMyPermissions(
                        @Parameter(description = "ID de la organización", required = true) @PathVariable UUID organizationId,
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {

                List<String> permissions = getMyPermissionsPort.execute(organizationId, userId);
                return ResponseEntity.ok(ApiResponse.ok(permissions));
        }

        @Operation(summary = "Listar personal", description = "Lista los miembros empleados de esta organización.")
        @GetMapping("/members")
        public ResponseEntity<ApiResponse<PaginatedResponse<OrganizationMemberResponse>>> getMembers(
                        @PathVariable UUID organizationId,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false, name = "active") Boolean isActive,
                        @RequestParam(required = false) String role) {

                var query = GetOrganizationMembersQuery.builder()
                                .organizationId(organizationId)
                                .page(page)
                                .size(limit)
                                .search(search)
                                .isActive(isActive)
                                .role(role)
                                .build();

                PaginatedResponse<OrganizationMember> result = getOrganizationMembersUseCase.getMembers(query);

                PaginatedResponse<OrganizationMemberResponse> response = PaginatedResponse
                                .<OrganizationMemberResponse>builder()
                                .data(result.getData().stream().map(this::toResponse).toList())
                                .page(result.getPage())
                                .size(result.getSize())
                                .totalElements(result.getTotalElements())
                                .totalPages(result.getTotalPages())
                                .hasNext(result.isHasNext())
                                .hasPrevious(result.isHasPrevious())
                                .build();

                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        @Operation(summary = "Obtener detalle de empleado", description = "Devuelve la información completa de un empleado por su ID.")
        @GetMapping("/members/{memberId}")
        public ResponseEntity<ApiResponse<OrganizationMemberResponse>> getMemberById(
                        @PathVariable UUID organizationId,
                        @PathVariable UUID memberId) {

                OrganizationMember member = getOrganizationMemberUseCase.getMember(organizationId, memberId);
                return ResponseEntity.ok(ApiResponse.ok(toResponse(member)));
        }

        @Operation(summary = "Obtener estadísticas de personal", description = "Devuelve el conteo de empleados y la nómina total.")
        @GetMapping("/members/stats")
        public ResponseEntity<ApiResponse<StaffStatsResponse>> getStats(
                        @PathVariable UUID organizationId) {

                var stats = getStaffStatsUseCase.execute(organizationId);
                var response = new StaffStatsResponse(
                                stats.totalEmployees(),
                                stats.activeCount(),
                                stats.onLeaveCount(),
                                stats.totalPayroll());

                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        @Operation(summary = "Contratar o Invitar Empleado", description = "Crea un empleado directamente en la organización con nombre, rol y PIN POS.")
        @PostMapping("/members")
        public ResponseEntity<ApiResponse<OrganizationMemberResponse>> addMember(
                        @PathVariable UUID organizationId,
                        @Valid @RequestBody AddMemberRequest request) {

                OrganizationMember member = addOrganizationMemberUseCase.execute(
                                organizationId,
                                request.getFullName(),
                                request.getEmail(),
                                request.getDni(),
                                request.getPhoneNumber(),
                                request.getRoleName(),
                                request.getPin(),
                                request.getSalary());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.created(toResponse(member), "Empleado registrado exitosamente"));
        }

        @Operation(summary = "Actualizar Rol o PIN", description = "Cambia el rol o el PIN de un empleado existente.")
        @PutMapping("/members/{memberId}")
        public ResponseEntity<ApiResponse<OrganizationMemberResponse>> updateMember(
                        @PathVariable UUID organizationId,
                        @PathVariable UUID memberId,
                        @Valid @RequestBody UpdateMemberRequest request) {

                OrganizationMember member = updateOrganizationMemberUseCase.execute(
                                organizationId, memberId, request.getRoleId(), request.getCurrentPin(), request.getPin(), 
                                request.getSalary(), request.getStatus(), request.getRoleName(),
                                request.getFullName(), request.getEmail(), request.getDni(), request.getPhoneNumber());

                return ResponseEntity.ok(ApiResponse.ok(toResponse(member), "Empleado actualizado exitosamente"));
        }

        @Operation(summary = "Desvincular Empleado", description = "Desactiva a un empleado asegurando que no pueda iniciar sesión nuevamente.")
        @DeleteMapping("/members/{memberId}")
        public ResponseEntity<ApiResponse<Void>> deactivateMember(
                        @PathVariable UUID organizationId,
                        @PathVariable UUID memberId) {

                deactivateOrganizationMemberUseCase.execute(organizationId, memberId);

                return ResponseEntity.ok(ApiResponse.ok(null, "Empleado desactivado exitosamente"));
        }

        private OrganizationMemberResponse toResponse(OrganizationMember member) {
                String fullName = "";
                String email = "";
                String phoneNumber = "";
                String dni = "";
                String roleName = "";

                // Fetch user info
                User user = userRepositoryPort.findById(member.getUserId()).orElse(null);
                if (user != null) {
                        fullName = user.getFullName();
                        email = user.getEmail();
                        phoneNumber = user.getPhoneNumber();
                        dni = user.getDni();
                }

                // Fetch role name
                RoleEntity role = roleRepository.findById(member.getRoleId()).orElse(null);
                if (role != null) {
                        roleName = role.getName();
                }

                return new OrganizationMemberResponse(
                                member.getId(),
                                member.getOrganizationId(),
                                member.getUserId(),
                                member.getRoleId(),
                                fullName,
                                email,
                                phoneNumber,
                                dni,
                                roleName,
                                member.isActive(),
                                member.getStatus(),
                                member.getSalary(),
                                member.getJoinedAt());
        }
}
