package resto_dev.modules.adminsaas.pensioners.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.pensioners.application.port.input.AddConsumptionUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.DeleteConsumptionUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.GetConsumptionsUseCase;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerConsumption;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/pensioners/{pensionerId}/consumptions")
@RequiredArgsConstructor
@Tag(name = "Consumos de Pensionistas", description = "Historial de consumo mensual por pensionista.")
public class PensionerConsumptionController {

    private final AddConsumptionUseCase addConsumptionUseCase;
    private final GetConsumptionsUseCase getConsumptionsUseCase;
    private final DeleteConsumptionUseCase deleteConsumptionUseCase;

    @PostMapping
    @Operation(summary = "Registrar un consumo")
    public ResponseEntity<ApiResponse<ConsumptionResponse>> addConsumption(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @Valid @RequestBody AddConsumptionRequest request) {

        PensionerConsumption saved = addConsumptionUseCase.execute(
                organizationId, pensionerId,
                request.date(), request.description(), request.totalAmount(),
                request.isExtra() != null && request.isExtra(),
                request.itemsSnapshot(), request.notes(), request.paymentType()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toResponse(saved), "Consumo registrado"));
    }

    @GetMapping
    @Operation(summary = "Obtener consumos del mes")
    public ResponseEntity<ApiResponse<PaginatedResponse<ConsumptionResponse>>> getConsumptions(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {

        int resolvedMonth = month == 0 ? LocalDate.now().getMonthValue() : month;
        int resolvedYear = year == 0 ? LocalDate.now().getYear() : year;

        Page<PensionerConsumption> result = getConsumptionsUseCase.execute(
                pensionerId, resolvedMonth, resolvedYear, startDate, endDate, PageRequest.of(page, limit)
        );

        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(result.map(this::toResponse))));
    }

    @DeleteMapping("/{consumptionId}")
    @Operation(summary = "Eliminar un consumo")
    public ResponseEntity<ApiResponse<Void>> deleteConsumption(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @PathVariable UUID consumptionId) {

        deleteConsumptionUseCase.execute(consumptionId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Consumo eliminado"));
    }

    private ConsumptionResponse toResponse(PensionerConsumption c) {
        return new ConsumptionResponse(
                c.getId(), c.getPensionerId(), c.getOrganizationId(),
                c.getDate(), c.getDescription(), c.getTotalAmount(),
                c.isExtra(), c.getItemsSnapshot(), c.getNotes(),
                c.getPaymentType(), c.getCreatedAt()
        );
    }

    // ─── Nested DTOs ───────────────────────────────────────────────────────────

    public record AddConsumptionRequest(
            LocalDate date,
            String description,
            BigDecimal totalAmount,
            Boolean isExtra,
            String itemsSnapshot,
            String notes,
            String paymentType
    ) {}

    public record ConsumptionResponse(
            UUID id, UUID pensionerId, UUID organizationId,
            LocalDate date, String description, BigDecimal totalAmount,
            boolean isExtra, String itemsSnapshot, String notes,
            String paymentType, LocalDateTime createdAt
    ) {}
}
