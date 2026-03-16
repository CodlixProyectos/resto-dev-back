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
import resto_dev.modules.adminsaas.pensioners.application.port.input.AddPensionerPaymentUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.DeletePensionerPaymentUseCase;
import resto_dev.modules.adminsaas.pensioners.application.port.input.GetPensionerPaymentsUseCase;
import resto_dev.modules.adminsaas.pensioners.domain.model.PensionerPayment;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.responses.PaginatedResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/pensioners/{pensionerId}/payments")
@RequiredArgsConstructor
@Tag(name = "Pagos de Pensionistas", description = "Gestión de abonos y pagos de pensionistas")
public class PensionerPaymentController {

    private final AddPensionerPaymentUseCase addUseCase;
    private final GetPensionerPaymentsUseCase getUseCase;
    private final DeletePensionerPaymentUseCase deleteUseCase;

    @PostMapping
    @Operation(summary = "Registrar un abono/pago")
    public ResponseEntity<ApiResponse<PaymentResponse>> addPayment(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @Valid @RequestBody AddPaymentRequest request) {

        PensionerPayment saved = addUseCase.execute(
                organizationId, pensionerId,
                request.amount(), request.date(),
                request.paymentMethod(), request.notes()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toResponse(saved), "Abono registrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Obtener abonos del mes")
    public ResponseEntity<ApiResponse<PaginatedResponse<PaymentResponse>>> getPayments(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {

        int resolvedMonth = month == 0 ? LocalDate.now().getMonthValue() : month;
        int resolvedYear = year == 0 ? LocalDate.now().getYear() : year;

        Page<PensionerPayment> result = getUseCase.execute(
                pensionerId, resolvedMonth, resolvedYear, PageRequest.of(page - 1, limit)
        );

        return ResponseEntity.ok(ApiResponse.ok(PaginatedResponse.of(result.map(this::toResponse))));
    }

    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Eliminar un abono")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable UUID organizationId,
            @PathVariable UUID pensionerId,
            @PathVariable UUID paymentId) {

        deleteUseCase.execute(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Abono eliminado exitosamente"));
    }

    private PaymentResponse toResponse(PensionerPayment p) {
        return new PaymentResponse(
                p.getId(), p.getPensionerId(), p.getOrganizationId(),
                p.getAmount(), p.getDate(), p.getPaymentMethod(),
                p.getNotes(), p.getCreatedAt()
        );
    }

    // ─── DTOs ──────────────────────────────────────────────────────────────────

    public record AddPaymentRequest(
            BigDecimal amount,
            LocalDate date,
            String paymentMethod,
            String notes
    ) {}

    public record PaymentResponse(
            UUID id, UUID pensionerId, UUID organizationId,
            BigDecimal amount, LocalDate date, String paymentMethod,
            String notes, LocalDateTime createdAt
    ) {}
}
