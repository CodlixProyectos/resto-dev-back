package resto_dev.modules.adminsaas.organizations.adapters.web;

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
import resto_dev.modules.adminsaas.organizations.adapters.web.dto.OrganizationResponse;
import resto_dev.modules.adminsaas.organizations.domain.Organization;
import resto_dev.modules.adminsaas.organizations.ports.in.CreateOrganizationPort;
import resto_dev.modules.adminsaas.organizations.ports.in.dto.CreateOrganizationCommand;
import resto_dev.modules.adminsaas.organizations.ports.out.OrganizationRepositoryPort;
import resto_dev.shared.responses.ApiResponse;

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

        private final CreateOrganizationPort createOrganizationPort;
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
                        @Valid @RequestBody CreateOrganizationCommand command,
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID ownerId) {
                Organization org = createOrganizationPort.execute(command, ownerId);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.created(toResponse(org), "Organization created"));
        }

        @Operation(summary = "Mis organizaciones", description = "Lista las organizaciones del usuario autenticado")
        @GetMapping("/mine")
        public ResponseEntity<ApiResponse<List<OrganizationResponse>>> myOrganizations(
                        @Parameter(hidden = true) @AuthenticationPrincipal UUID ownerId) {
                List<OrganizationResponse> list = organizationRepository.findByOwnerId(ownerId).stream()
                                .map(this::toResponse).toList();
                return ResponseEntity.ok(ApiResponse.ok(list));
        }

        private OrganizationResponse toResponse(Organization o) {
                return new OrganizationResponse(o.getId(), o.getName(), o.getSlug(),
                                o.getSchemaName(), o.getType(), o.getOwnerId(), o.isActive());
        }
}
