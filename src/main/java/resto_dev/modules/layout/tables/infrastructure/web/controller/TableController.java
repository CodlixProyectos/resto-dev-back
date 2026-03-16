package resto_dev.modules.layout.tables.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.layout.tables.application.command.CreateTableCommand;
import resto_dev.modules.layout.tables.application.command.UpdateTableCommand;
import resto_dev.modules.layout.tables.application.port.input.CreateTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.DeleteTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.ListTablesUseCase;
import resto_dev.modules.layout.tables.application.port.input.UpdateTableUseCase;
import resto_dev.modules.layout.tables.application.query.SearchTablesQuery;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
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
        private final ListTablesUseCase listTablesUseCase;
        private final UpdateTableUseCase updateTableUseCase;
        private final DeleteTableUseCase deleteTableUseCase;
        private final TableWebMapper webMapper;

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
                                .page(pageModel.page() + 1)
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
}
