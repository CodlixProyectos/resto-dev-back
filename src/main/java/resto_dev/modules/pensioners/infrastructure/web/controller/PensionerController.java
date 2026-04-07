package resto_dev.modules.pensioners.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.pensioners.application.port.input.AddPensionerUseCase;
import resto_dev.modules.pensioners.application.port.input.GetPensionerSummaryUseCase;
import resto_dev.modules.pensioners.application.port.input.GetPensionersUseCase;
import resto_dev.modules.pensioners.application.port.input.BulkAddPensionersUseCase;
import resto_dev.modules.pensioners.infrastructure.excel.PensionerExcelParser;
import resto_dev.modules.pensioners.domain.model.Pensioner;
import resto_dev.modules.pensioners.infrastructure.web.dto.input.AddPensionerRequest;
import resto_dev.modules.pensioners.application.port.input.UpdatePensionerUseCase;
import resto_dev.modules.pensioners.infrastructure.web.dto.input.UpdatePensionerRequest;
import resto_dev.modules.pensioners.infrastructure.web.dto.output.PensionerResponse;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/pensioners")
@RequiredArgsConstructor
@Tag(name = "Pensionistas", description = "Gestión de pensionistas (clientes con suscripción).")
public class PensionerController {

    private final AddPensionerUseCase addPensionerUseCase;
    private final UpdatePensionerUseCase updatePensionerUseCase;
    private final GetPensionersUseCase getPensionersUseCase;
    private final GetPensionerSummaryUseCase getSummaryUseCase;
    private final BulkAddPensionersUseCase bulkAddPensionersUseCase;
    private final PensionerExcelParser excelParser;

    @PostMapping
    @Operation(summary = "Crear un pensionista")
    public ResponseEntity<ApiResponse<PensionerResponse>> createPensioner(
            @PathVariable UUID organizationId,
            @Valid @RequestBody AddPensionerRequest request) {
        
        Pensioner pensioner = addPensionerUseCase.execute(
                organizationId,
                request.getFullName(),
                request.getDni(),
                request.getEmail(),
                request.getPhoneNumber()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toResponse(pensioner), "Pensionista creado correctamente"));
    }

    @PutMapping("/{pensionerId}")
    @Operation(summary = "Actualizar datos de un pensionista")
    public ResponseEntity<ApiResponse<PensionerResponse>> updatePensioner(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @Valid @RequestBody UpdatePensionerRequest request) {
        
        Pensioner pensioner = updatePensionerUseCase.execute(
                organizationId,
                pensionerId,
                request.getFullName(),
                request.getDni(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.isActive()
        );
        
        return ResponseEntity.ok(ApiResponse.ok(toResponse(pensioner), "Pensionista actualizado correctamente"));
    }

    @GetMapping
    @Operation(summary = "Listar pensionistas")
    public ResponseEntity<ApiResponse<PaginatedResponse<PensionerResponse>>> getPensioners(
            @PathVariable UUID organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        
        Page<Pensioner> pensionersPage = getPensionersUseCase.execute(
                organizationId,
                PageRequest.of(page, limit),
                search
        );
        
        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(pensionersPage.map(this::toResponse))));
    }

    @GetMapping("/{pensionerId}/summary")
    @Operation(summary = "Obtener resumen financiero del mes o rango")
    public ResponseEntity<ApiResponse<GetPensionerSummaryUseCase.PensionerSummaryResponse>> getSummary(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        
        int rMonth = month == 0 ? java.time.LocalDate.now().getMonthValue() : month;
        int rYear = year == 0 ? java.time.LocalDate.now().getYear() : year;
        
        var summary = getSummaryUseCase.execute(pensionerId, rMonth, rYear, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/bulk-upload")
    @Operation(summary = "Importar pensionistas masivamente desde Excel")
    public ResponseEntity<ApiResponse<Void>> bulkUpload(
            @PathVariable UUID organizationId,
            @RequestParam("file") MultipartFile file) throws Exception {
        
        var rows = excelParser.parse(file);
        
        List<BulkAddPensionersUseCase.BulkPensionerRequest> requests = rows.stream()
                .map(row -> new BulkAddPensionersUseCase.BulkPensionerRequest(
                        row.fullName(),
                        row.dni(),
                        row.email(),
                        row.phoneNumber()
                ))
                .toList();

        bulkAddPensionersUseCase.execute(organizationId, requests);
        
        return ResponseEntity.ok(ApiResponse.ok(null, "Importación masiva completada correctamente"));
    }

    private PensionerResponse toResponse(Pensioner pensioner) {
        return new PensionerResponse(
                pensioner.getId(),
                pensioner.getOrganizationId(),
                pensioner.getFullName(),
                pensioner.getDni(),
                pensioner.getEmail(),
                pensioner.getPhoneNumber(),
                pensioner.isActive(),
                pensioner.getCreatedAt()
        );
    }
}
