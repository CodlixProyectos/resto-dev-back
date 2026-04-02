package resto_dev.modules.adminsaas.organizations.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.OrganizationResponse;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.adminsaas.organizations.application.port.input.CreateOrganizationUseCase;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.CreateOrganizationRequest;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.input.UpdateOrganizationRequest;
import resto_dev.modules.adminsaas.organizations.application.port.input.UpdateOrganizationUseCase;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.mapper.OrganizationWebMapper;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;
import resto_dev.modules.adminsaas.organizations.application.port.input.GetOrganizationsUseCase;
import resto_dev.modules.adminsaas.organizations.application.query.GetOrganizationsQuery;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;
import resto_dev.shared.errors.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Organization REST controller.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Gestión de organizaciones (tenants genéricos del SaaS)")
public class OrganizationController {

        private final CreateOrganizationUseCase createOrganizationPort;
        private final OrganizationWebMapper webMapper;
        private final GetOrganizationsUseCase getOrganizationsUseCase;
        private final UpdateOrganizationUseCase updateOrganizationUseCase;
        private final OrganizationRepositoryPort organizationRepository;

        @Operation(summary = "Crear organización", description = "Crea una nueva organización y genera su schema client_{uuid}. "
                        +
                        "El campo 'type' define el tipo de negocio (restaurant, hotel, gym, etc.)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Organización creada"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug ya existe")
        })
        @PostMapping
        public ResponseEntity<ApiResponse<OrganizationResponse>> create(
                        @Valid @RequestBody CreateOrganizationRequest request,
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID ownerId) {

                var command = webMapper.toCommand(request);
                Organization org = createOrganizationPort.execute(command, ownerId);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.created(webMapper.toResponse(org), "Organization created"));
        }

        @Operation(summary = "Mis organizaciones", description = "Lista las organizaciones del usuario autenticado")
        @GetMapping("/mine")
        public ResponseEntity<ApiResponse<List<OrganizationResponse>>> myOrganizations(
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID ownerId) {
                List<OrganizationResponse> list = organizationRepository.findByOwnerId(ownerId).stream()
                                .map(webMapper::toResponse).toList();
                return ResponseEntity.ok(ApiResponse.ok(list));
        }

        @Operation(summary = "Obtener organización por ID", description = "Obtiene los detalles de una organización específica")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<OrganizationResponse>> getById(@PathVariable UUID id) {
                var org = organizationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

                return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(org)));
        }

        @Operation(summary = "Listar organizaciones paginadas", description = "Lista organizaciones con soporte para búsqueda, filtrado y paginación")
        @GetMapping
        public ResponseEntity<ApiResponse<PaginatedResponse<OrganizationResponse>>> getOrganizations(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false, name = "active") Boolean isActive) {

                GetOrganizationsQuery query = GetOrganizationsQuery.builder()
                                .page(page)
                                .size(limit)
                                .search(search)
                                .isActive(isActive)
                                .build();

                PaginatedResponse<Organization> result = getOrganizationsUseCase.getOrganizations(query);

                PaginatedResponse<OrganizationResponse> response = PaginatedResponse.<OrganizationResponse>builder()
                                .data(result.getData().stream().map(webMapper::toResponse).toList())
                                .page(result.getPage())
                                .size(result.getSize())
                                .totalElements(result.getTotalElements())
                                .totalPages(result.getTotalPages())
                                .hasNext(result.isHasNext())
                                .hasPrevious(result.isHasPrevious())
                                .build();

                return ResponseEntity.ok(ApiResponse.ok(response));
        }

        @Operation(summary = "Actualizar organización", description = "Actualiza los datos de identidad y contacto de una organización")
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
                                request.sunatClientSecret());

                Organization updated = updateOrganizationUseCase.execute(id, command);
                return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(updated), "Organization updated"));
        }
}
