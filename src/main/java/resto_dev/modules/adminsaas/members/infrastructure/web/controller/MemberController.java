package resto_dev.modules.adminsaas.members.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import resto_dev.modules.adminsaas.members.application.port.input.GetMyPermissionsUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.GetOrganizationMembersUseCase;
import resto_dev.modules.adminsaas.members.application.query.GetOrganizationMembersQuery;
import resto_dev.modules.adminsaas.members.domain.model.OrganizationMember;
import resto_dev.modules.adminsaas.members.infrastructure.web.dto.output.OrganizationMemberResponse;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/my-permissions")
@RequiredArgsConstructor
@Tag(name = "Organization Members", description = "Endpoints para gestionar miembros y permisos dentro de una organización")
public class MemberController {

    private final GetMyPermissionsUseCase getMyPermissionsPort;
    private final GetOrganizationMembersUseCase getOrganizationMembersUseCase;

    @Operation(summary = "Obtener mis permisos", description = "Devuelve una lista de códigos de permisos (ej. CREATE_ORDER, VIEW_MENU) que tiene el usuario autenticado dentro de la organización especificada.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permisos obtenidos exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin acceso a la organización")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getMyPermissions(
            @Parameter(description = "ID de la organización", required = true) @PathVariable UUID organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {

        List<String> permissions = getMyPermissionsPort.execute(organizationId, userId);
        return ResponseEntity.ok(ApiResponse.ok(permissions));
    }

    @Operation(summary = "Listar miembros paginados", description = "Lista los miembros de esta organización con soporte para búsqueda, filtrado y paginación")
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

        PaginatedResponse<OrganizationMemberResponse> response = PaginatedResponse.<OrganizationMemberResponse>builder()
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
