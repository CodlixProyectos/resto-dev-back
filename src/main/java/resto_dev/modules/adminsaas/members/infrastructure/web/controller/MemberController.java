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
import resto_dev.modules.adminsaas.users.application.command.AuthResult;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
@RequiredArgsConstructor
@Tag(name = "Organization Staff / Members", description = "Endpoints para gestionar el personal de una organización (contratar, cambiar rol, despedir) y login POS.")
public class MemberController {

        private final GetMyPermissionsUseCase getMyPermissionsPort;
        private final GetOrganizationMembersUseCase getOrganizationMembersUseCase;
        private final AddOrganizationMemberUseCase addOrganizationMemberUseCase;
        private final UpdateOrganizationMemberUseCase updateOrganizationMemberUseCase;
        private final DeactivateOrganizationMemberUseCase deactivateOrganizationMemberUseCase;
        private final PinLoginOrganizationMemberUseCase pinLoginOrganizationMemberUseCase;

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
                        @RequestParam(required = false, name = "active") Boolean isActive) {

                var query = GetOrganizationMembersQuery.builder()
                                .organizationId(organizationId)
                                .page(page)
                                .size(limit)
                                .search(search)
                                .isActive(isActive)
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

        @Operation(summary = "Contratar o Invitar Empleado", description = "Asigna un empleado a la organización con un Role y un PIN POS.")
        @PostMapping("/members")
        public ResponseEntity<ApiResponse<OrganizationMemberResponse>> addMember(
                        @PathVariable UUID organizationId,
                        @Valid @RequestBody AddMemberRequest request) {

                OrganizationMember member = addOrganizationMemberUseCase.execute(
                                organizationId, request.getEmail(), request.getRoleId(), request.getPin());

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
                                organizationId, memberId, request.getRoleId(), request.getPin());

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

        @Operation(summary = "POS PIN Login", description = "Login rápio usando PIN para tablets de Punto de Venta o Cocina.")
        @PostMapping("/auth/pin")
        public ResponseEntity<ApiResponse<AuthResult>> pinLogin(
                        @PathVariable UUID organizationId,
                        @Valid @RequestBody PinLoginRequest request) {

                AuthResult result = pinLoginOrganizationMemberUseCase.execute(organizationId, request.getPin());

                return ResponseEntity.ok(ApiResponse.ok(result, "POS Login Successful"));
        }

        private OrganizationMemberResponse toResponse(OrganizationMember member) {
                return new OrganizationMemberResponse(
                                member.getId(),
                                member.getOrganizationId(),
                                member.getUserId(),
                                member.getRoleId(),
                                member.isActive(),
                                member.getJoinedAt());
        }
}
