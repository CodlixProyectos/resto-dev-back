package resto_dev.modules.layout.zones.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.layout.zones.application.command.CreateZoneCommand;
import resto_dev.modules.layout.zones.application.command.UpdateZoneCommand;
import resto_dev.modules.layout.zones.application.port.input.CreateZoneUseCase;
import resto_dev.modules.layout.zones.application.port.input.DeleteZoneUseCase;
import resto_dev.modules.layout.zones.application.port.input.ListZonesUseCase;
import resto_dev.modules.layout.zones.application.port.input.UpdateZoneUseCase;
import resto_dev.modules.layout.zones.application.query.SearchZonesQuery;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.modules.layout.zones.infrastructure.web.dto.input.CreateZoneRequest;
import resto_dev.modules.layout.zones.infrastructure.web.dto.input.UpdateZoneRequest;
import resto_dev.modules.layout.zones.infrastructure.web.dto.output.ZoneResponse;
import resto_dev.modules.layout.zones.infrastructure.web.mapper.ZoneWebMapper;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@Tag(name = "Zonas y Salones", description = "Endpoints para la gestión de ambientes del restaurante (Terraza, Salón, etc.)")
public class ZoneController {

    private final CreateZoneUseCase createZoneUseCase;
    private final ListZonesUseCase listZonesUseCase;
    private final UpdateZoneUseCase updateZoneUseCase;
    private final DeleteZoneUseCase deleteZoneUseCase;
    private final ZoneWebMapper webMapper;

    @PostMapping("/create")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_LAYOUT')")
    @Operation(summary = "Crear una nueva zona", description = "Registra un nuevo salón o ambiente físico.")
    public ResponseEntity<ApiResponse<ZoneResponse>> createZone(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @Valid @RequestBody CreateZoneRequest request) {
        CreateZoneCommand command = webMapper.toCommand(request);
        Zone createdZone = createZoneUseCase.execute(command);
        return new ResponseEntity<>(ApiResponse.created(webMapper.toResponse(createdZone), "Zona creada exitosamente"),
                HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_LAYOUT')")
    @Operation(summary = "Listar zonas", description = "Devuelve las zonas paginadas con soporte de filtros.")
    public ResponseEntity<ApiResponse<PaginatedResponse<ZoneResponse>>> listZones(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SearchZonesQuery query = SearchZonesQuery.builder()
                .search(search)
                .isActive(isActive)
                .page(page)
                .size(size)
                .build();

        PageModel<Zone> pageModel = listZonesUseCase.execute(query);

        List<ZoneResponse> responses = pageModel.content().stream()
                .map(webMapper::toResponse)
                .toList();

        PaginatedResponse<ZoneResponse> pageResponse = PaginatedResponse.<ZoneResponse>builder()
                .data(responses)
                .page(pageModel.page() + 1) // ajustado a 1-indexed para el cliente
                .size(pageModel.size())
                .totalElements(pageModel.totalElements())
                .totalPages(pageModel.totalPages())
                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                .hasPrevious(pageModel.page() > 0)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_LAYOUT')")
    @Operation(summary = "Actualizar zona", description = "Actualiza el nombre, descripción o estado activo de una zona.")
    public ResponseEntity<ApiResponse<ZoneResponse>> updateZone(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateZoneRequest request) {
        UpdateZoneCommand command = webMapper.toCommand(request);
        Zone updatedZone = updateZoneUseCase.execute(id, command);
        return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(updatedZone)));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_LAYOUT')")
    @Operation(summary = "Eliminar zona", description = "Elimina físicamente una zona si no tiene mesas enlazadas.")
    public ResponseEntity<ApiResponse<Void>> deleteZone(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @PathVariable UUID id) {
        deleteZoneUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Zona eliminada exitosamente"));
    }
}
