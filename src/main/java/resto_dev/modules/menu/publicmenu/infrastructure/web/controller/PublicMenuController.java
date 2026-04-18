package resto_dev.modules.menu.publicmenu.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.zones.application.port.output.ZoneRepositoryPort;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.modules.menu.publicmenu.application.service.PublicMenuApplicationService;
import resto_dev.modules.menu.publicmenu.domain.model.PublicMenuCategory;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.menu.publicmenu.infrastructure.web.dto.PublicCategoryDTO;
import resto_dev.modules.menu.publicmenu.infrastructure.web.dto.PublicOrganizationDTO;
import resto_dev.modules.menu.publicmenu.infrastructure.web.dto.PublicProductDTO;
import resto_dev.modules.menu.publicmenu.infrastructure.web.dto.PublicTableDTO;
import resto_dev.shared.errors.ApiException;
import resto_dev.shared.responses.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/organizations/{organizationId}")
@RequiredArgsConstructor
@Tag(name = "Menú Público", description = "Endpoints públicos sin JWT orientados a Clientes escaneando menús QR en Mesa")
public class PublicMenuController {

    private final PublicMenuApplicationService publicMenuService;
    private final TableRepositoryPort tableRepository;
    private final ZoneRepositoryPort zoneRepository;
    private final OrganizationRepositoryPort organizationRepository;

    @Operation(summary = "Obtener Branding de la Organización", description = "Devuelve logo y colores corporativos")
    @GetMapping("/branding")
    public ResponseEntity<ApiResponse<PublicOrganizationDTO>> getBranding(
            @PathVariable UUID organizationId) {

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> ApiException.notFound("Organización no encontrada."));

        PublicOrganizationDTO dto = PublicOrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .logoUrl(org.getLogoUrl())
                .primaryColor(org.getPrimaryColor())
                .secondaryColor(org.getSecondaryColor())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @Operation(summary = "Obtener Carta Digital", description = "Devuelve todo el menú ordenado por categorías con todos los productos disponibles")
    @GetMapping("/menu")
    public ResponseEntity<ApiResponse<List<PublicCategoryDTO>>> getMenu(
            @PathVariable UUID organizationId) {

        // La arquitectura TenantFilter automáticamente aislará por X-Organization-Id o por el Path.
        List<PublicMenuCategory> domainMenu = publicMenuService.getPublicMenu();

        // Map domain to DTO for response
        List<PublicCategoryDTO> menu = domainMenu.stream().map(cat -> PublicCategoryDTO.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .products(cat.getProducts().stream().map(p -> PublicProductDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .build()).toList())
                .build()).toList();

        return ResponseEntity.ok(ApiResponse.ok(menu));
    }

    @Operation(summary = "Verificar Ubicación de QR", description = "Devuelve la información de la mesa escaneada para que el cliente sepa dónde está sumando sus pedidos")
    @GetMapping("/tables/{tableId}")
    public ResponseEntity<ApiResponse<PublicTableDTO>> getTableInfo(
            @PathVariable UUID organizationId,
            @PathVariable UUID tableId) {

        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> ApiException.notFound("La mesa escaneada no existe."));

        if (!table.isActive()) {
            throw ApiException.badRequest("Esta mesa está inactiva actualmente.");
        }

        Zone zone = zoneRepository.findById(table.getZoneId())
                .orElseThrow(() -> ApiException.internal("No se pudo obtener el piso de la mesa."));

        PublicTableDTO dto = PublicTableDTO.builder()
                .id(table.getId())
                .name(table.getTableNumber())
                .capacity(table.getCapacity())
                .zoneId(zone.getId())
                .zoneName(zone.getName())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
