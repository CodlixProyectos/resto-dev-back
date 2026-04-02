package resto_dev.modules.layout.tables.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletRequest;
import resto_dev.shared.security.sse.SseTicketService;
import resto_dev.modules.layout.tables.infrastructure.web.sse.SseTableEventPublisher;
import resto_dev.modules.layout.tables.application.service.TablePdfService;
import resto_dev.modules.layout.tables.application.command.CreateTableCommand;
import resto_dev.modules.layout.tables.application.command.UpdateTableCommand;
import resto_dev.modules.layout.tables.application.port.input.BulkCreateTableUseCase;
import resto_dev.modules.adminsaas.organizations.application.port.output.OrganizationRepositoryPort;
import resto_dev.modules.adminsaas.organizations.domain.model.Organization;
import resto_dev.modules.layout.tables.application.port.input.CreateTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.DeleteTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.ListTablesUseCase;
import resto_dev.modules.layout.tables.application.port.input.UpdateTableUseCase;
import resto_dev.modules.layout.tables.application.query.SearchTablesQuery;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.layout.zones.application.port.output.ZoneRepositoryPort;
import resto_dev.modules.layout.zones.application.query.SearchZonesQuery;
import resto_dev.modules.layout.zones.domain.model.Zone;
import java.util.Map;
import java.util.stream.Collectors;
import resto_dev.modules.layout.tables.infrastructure.web.dto.input.BulkCreateTableRequest;
import resto_dev.modules.layout.tables.infrastructure.web.dto.input.CreateTableRequest;
import resto_dev.modules.layout.tables.infrastructure.web.dto.input.UpdateTableRequest;
import resto_dev.modules.layout.tables.infrastructure.web.dto.output.TableResponse;
import resto_dev.modules.layout.tables.infrastructure.web.mapper.TableWebMapper;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@Tag(name = "Mesas", description = "Endpoints para la gestión de mesas dentro de las zonas del restaurante")
public class TableController {

        private final CreateTableUseCase createTableUseCase;
        private final BulkCreateTableUseCase bulkCreateTableUseCase;
        private final ListTablesUseCase listTablesUseCase;
        private final UpdateTableUseCase updateTableUseCase;
        private final DeleteTableUseCase deleteTableUseCase;
        private final TableWebMapper webMapper;
        private final TablePdfService pdfService;
        private final OrganizationRepositoryPort organizationRepository;
        private final ZoneRepositoryPort zoneRepository;
        private final SseTableEventPublisher sseTableEventPublisher;
        private final SseTicketService sseTicketService;

        @PostMapping("/create")
        // @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_LAYOUT')")
        @Operation(summary = "Crear nueva mesa", description = """
                        Crea una nueva mesa asociándola a una Zona existente.

                        **Campos requeridos:**
                        - zoneId: UUID de la zona
                        - tableNumber: Identificador único de la mesa
                        - capacity: Número de comensales (1-20)

                        **Campos opcionales:**
                        - status: Estado inicial (por defecto: FREE)

                        **Campos automáticos:**
                        - id: Generado automáticamente como UUID
                        - active: Siempre true al crear
                        """)
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Mesa creada exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o validación fallida"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zona no encontrada"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe una mesa con ese número en la zona")
        })
        public ResponseEntity<ApiResponse<TableResponse>> createTable(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @Valid @RequestBody CreateTableRequest request) {
                CreateTableCommand command = webMapper.toCommand(request);
                Table createdTable = createTableUseCase.execute(command);
                return new ResponseEntity<>(
                                ApiResponse.created(webMapper.toResponse(createdTable), "Mesa creada exitosamente"),
                                HttpStatus.CREATED);
        }

        @PostMapping("/bulk-create")
        @Operation(summary = "Crear múltiples mesas (Masivo)", description = """
                Permite crear varias mesas en una zona de forma secuencial.
                El sistema tomará el 'baseTableNumber' e incrementará el valor para las siguientes mesas.
                """)
        public ResponseEntity<ApiResponse<List<TableResponse>>> bulkCreateTable(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @Valid @RequestBody BulkCreateTableRequest request) {
                List<Table> tables = bulkCreateTableUseCase.execute(request);
                List<TableResponse> responses = tables.stream()
                                .map(webMapper::toResponse)
                                .toList();
                return new ResponseEntity<>(
                                ApiResponse.created(responses, tables.size() + " mesas creadas exitosamente"),
                                HttpStatus.CREATED);
        }

        @GetMapping("/list")

        // @PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_LAYOUT')")
        @Operation(summary = "Listar mesas", description = "Devuelve las mesas con soporte de filtros (buscar número, zona, estado, etc).")
        public ResponseEntity<ApiResponse<PaginatedResponse<TableResponse>>> listTables(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) UUID zoneId,
                        @RequestParam(required = false) TableStatus status,
                        @RequestParam(required = false) Boolean isActive,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                SearchTablesQuery query = SearchTablesQuery.builder()
                                .search(search)
                                .zoneId(zoneId)
                                .status(status)
                                .isActive(isActive)
                                .page(page)
                                .size(size)
                                .build();

                PageModel<Table> pageModel = listTablesUseCase.execute(query);

                List<TableResponse> responses = pageModel.content().stream()
                                .map(webMapper::toResponse)
                                .toList();

                PaginatedResponse<TableResponse> pageResponse = PaginatedResponse.<TableResponse>builder()
                                .data(responses)
                                .page(pageModel.page())
                                .size(pageModel.size())
                                .totalElements(pageModel.totalElements())
                                .totalPages(pageModel.totalPages())
                                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                                .hasPrevious(pageModel.page() > 0)
                                .build();

                return ResponseEntity.ok(ApiResponse.ok(pageResponse));
        }

        @PutMapping("/update/{id}")
        // TODO: Descomentar en producción después de configurar permisos
        // @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_LAYOUT')")
        @Operation(summary = "Actualizar mesa", description = "Actualiza el número de mesa, capacidad o la reasigna a otra Zona.")
        public ResponseEntity<ApiResponse<TableResponse>> updateTable(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateTableRequest request) {
                UpdateTableCommand command = webMapper.toCommand(request);
                Table updatedTable = updateTableUseCase.execute(id, command);
                return ResponseEntity.ok(ApiResponse.ok(webMapper.toResponse(updatedTable)));
        }

        @DeleteMapping("/delete/{id}")
        // TODO: Descomentar en producción después de configurar permisos
        // @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_LAYOUT')")
        @Operation(summary = "Eliminar mesa", description = "Elimina físicamente la estructura de la mesa.")
        public ResponseEntity<ApiResponse<Void>> deleteTable(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @PathVariable UUID id) {
                deleteTableUseCase.execute(id);
                return ResponseEntity.ok(ApiResponse.ok(null, "Mesa eliminada exitosamente"));
        }

        @GetMapping("/download-pdf")
        @Operation(summary = "Descargar catálogo PDF", description = "Genera un archivo PDF con todos los códigos QR.")
        public ResponseEntity<byte[]> downloadPdfCatalog(
                        @RequestParam(required = false) UUID zoneId,
                        @RequestParam(required = false) UUID organizationId,
                        HttpServletRequest request) {

                SearchTablesQuery query = SearchTablesQuery.builder()
                                .zoneId(zoneId)
                                .page(0)
                                .size(1000)
                                .build();

                PageModel<Table> pageModel = listTablesUseCase.execute(query);
                List<Table> tables = pageModel.content();

                String origin = request.getHeader("Origin");
                if (origin == null || origin.isEmpty()) {
                        origin = request.getScheme() + "://" + request.getServerName();
                        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                                origin += ":" + request.getServerPort();
                        }
                }

                // Resolve Organization Name and Zone Names for branding
                String orgName = "Mi Restaurante";
                if (organizationId != null) {
                        orgName = organizationRepository.findById(organizationId)
                                        .map(Organization::getName)
                                        .orElse("Mi Restaurante");
                }

                SearchZonesQuery zoneQuery = SearchZonesQuery.builder().page(0).size(100).build();
                Map<UUID, String> zoneNames = zoneRepository.searchZones(zoneQuery).content()
                                .stream()
                                .collect(Collectors.toMap(Zone::getId, Zone::getName));

                byte[] pdfContent = pdfService.generateQrCatalog(tables, origin, orgName, zoneNames);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Catalogo_Mesas.pdf")
                                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(pdfContent);
        }

        @PostMapping("/events/ticket")
        @Operation(summary = "Generar Ticket SSE para Mesas", description = "Genera un ticket de un solo uso para sincronización en tiempo real.")
        public ResponseEntity<ApiResponse<UUID>> generateSseTicket(
                        @RequestHeader("X-Organization-Id") UUID orgId,
                        @org.springframework.security.core.annotation.AuthenticationPrincipal UUID userId) {
                UUID ticket = sseTicketService.generateTicket(userId, orgId);
                return ResponseEntity.ok(ApiResponse.ok(ticket, "Ticket generado exitosamente"));
        }

        @GetMapping(value = "/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        @Operation(summary = "Stream de Eventos de Mesas", description = "Conexión SSE para recibir cambios de estado de mesas en tiempo real.")
        public SseEmitter streamTableEvents(
                        @RequestParam(value = "organizationId", required = false) UUID orgIdParam,
                        @RequestHeader(value = "X-Organization-Id", required = false) UUID orgIdHeader) {
                
                UUID orgId = orgIdParam != null ? orgIdParam : (orgIdHeader != null ? orgIdHeader : resto_dev.shared.tenancy.TenantContext.getCurrentOrganizationId());
                
                if (orgId == null) {
                        SseEmitter emitter = new SseEmitter();
                        emitter.completeWithError(new RuntimeException("Organization ID required"));
                        return emitter;
                }
                
                return sseTableEventPublisher.subscribe(orgId);
        }
}
