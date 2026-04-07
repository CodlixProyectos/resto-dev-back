package resto_dev.modules.adminsaas.members.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.members.application.port.input.PinLoginOrganizationMemberUseCase;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.entity.OrganizationMemberJpaEntity;
import resto_dev.modules.adminsaas.members.infrastructure.persistence.repository.OrganizationMemberJpaRepository;
import resto_dev.modules.adminsaas.members.infrastructure.web.dto.input.PinLoginRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.OrganizationResponse;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.modules.adminsaas.users.application.command.AuthResult;

import resto_dev.modules.adminsaas.organizations.application.port.input.UpdateOrganizationUseCase;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.UpdateOrganizationRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.mapper.OrganizationWebMapper;
import resto_dev.shared.errors.ResourceNotFoundException;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Endpoints para autenticación y gestión a nivel de cliente.")
public class OrganizationAuthController {

    private final PinLoginOrganizationMemberUseCase pinLoginUseCase;
    private final OrganizationMemberJpaRepository memberRepository;
    private final OrganizationRepositoryPort organizationRepository;
    private final UpdateOrganizationUseCase updateOrganizationUseCase;
    private final OrganizationWebMapper webMapper;

    @Operation(summary = "Obtener organización por ID", description = "Retorna los detalles básicos de una organización.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getById(@PathVariable UUID id) {
        var org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(org)));
    }

    @Operation(summary = "Actualizar organización", description = "Actualiza los datos generales de la organización.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        var command = new UpdateOrganizationUseCase.UpdateOrganizationCommand(
                request.name(),
                request.legalName(),
                request.businessId(),
                request.email(),
                request.phone(),
                request.address(),
                request.logoUrl(),
                request.primaryColor(),
                request.secondaryColor(),
                request.sunatUser(),
                request.sunatPassword(),
                request.sunatClientId(),
                request.sunatClientSecret(),
                request.yapeQrUrl(),
                request.plinQrUrl());

        Organization updated = updateOrganizationUseCase.execute(id, command);
        return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(updated), "Organization updated"));
    }

    @Operation(summary = "Mis organizaciones", description = "Retorna las organizaciones donde el usuario autenticado es miembro.")
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getMyOrganizations(
            @AuthenticationPrincipal UUID userId) {

        List<OrganizationResponse> orgs = memberRepository.findActiveByUserIdWithOrganization(userId)
                .stream()
                .map(OrganizationMemberJpaEntity::getOrganization)
                .distinct()
                .map(org -> new OrganizationResponse(
                        org.getId(),
                        org.getName(),
                        org.getSlug(),
                        org.getSchemaName(),
                        org.getType(),
                        org.getOwnerId(),
                        org.isActive(),
                        org.getLegalName(),
                        org.getBusinessId(),
                        org.getEmail(),
                        org.getPhone(),
                        org.getAddress(),
                        org.getLogoUrl(),
                        org.getPrimaryColor(),
                        org.getSecondaryColor(),
                        org.getSunatUser(),
                        org.getSunatPassword(),
                        org.getSunatClientId(),
                        org.getSunatClientSecret(),
                        null,
                        org.getInvitationCode(),
                        org.isHasInventory(),
                        org.isHasPensioners(),
                        org.isHasKds(),
                        org.getYapeQrUrl(),
                        org.getPlinQrUrl()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(orgs));
    }

    @Operation(summary = "POS PIN Login (Global)", 
               description = "Login rápido usando PIN para tablets de Punto de Venta o Cocina, sin especificar organización en la URL.")
    @PostMapping("/auth/pin")
    public ResponseEntity<ApiResponse<AuthResult>> globalPinLogin(@Valid @RequestBody PinLoginRequest request) {
        AuthResult result = pinLoginUseCase.execute(null, request.getEmail(), request.getDni(), request.getPin());
        return ResponseEntity.ok(ApiResponse.ok(result, "POS Global Login Successful"));
    }

    @Operation(summary = "POS PIN Login (Organization Specific)", 
               description = "Login rápido usando PIN especificando la organización en la URL.")
    @PostMapping("/{organizationId}/auth/pin")
    public ResponseEntity<ApiResponse<AuthResult>> organizationPinLogin(
            @PathVariable UUID organizationId,
            @Valid @RequestBody PinLoginRequest request) {
        AuthResult result = pinLoginUseCase.execute(organizationId, request.getEmail(), request.getDni(), request.getPin());
        return ResponseEntity.ok(ApiResponse.ok(result, "POS Login Successful"));
    }
}
