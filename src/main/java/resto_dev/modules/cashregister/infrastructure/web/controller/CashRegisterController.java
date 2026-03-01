package resto_dev.modules.cashregister.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.cashregister.application.port.input.CloseShiftUseCase;
import resto_dev.modules.cashregister.application.port.input.GetCurrentShiftUseCase;
import resto_dev.modules.cashregister.application.port.input.OpenShiftUseCase;
import resto_dev.modules.cashregister.domain.model.CashShift;
import resto_dev.modules.cashregister.infrastructure.web.dto.CashShiftResponse;
import resto_dev.modules.cashregister.infrastructure.web.dto.CloseShiftRequest;
import resto_dev.modules.cashregister.infrastructure.web.dto.OpenShiftRequest;
import resto_dev.shared.responses.ApiResponse;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cash-register")
@RequiredArgsConstructor
@Tag(name = "Caja Registradora", description = "Endpoints para la apertura, control y cierre de caja (Cash Shifts) en una organización")
public class CashRegisterController {

    private final OpenShiftUseCase openShiftUseCase;
    private final CloseShiftUseCase closeShiftUseCase;
    private final GetCurrentShiftUseCase getCurrentShiftUseCase;

    @PostMapping("/open")
    // @PreAuthorize("hasPermission(#orgId, 'Organization', 'POS_ACCESS')")
    @Operation(summary = "Abrir Turno de Caja", description = "Abre un nuevo turno de caja con un fondo inicial especificado. Solo puede existir un turno abierto a la vez.")
    public ResponseEntity<ApiResponse<CashShiftResponse>> openShift(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UUID currentUserId,
            @Valid @RequestBody OpenShiftRequest request) {

        CashShift shift = openShiftUseCase.openShift(currentUserId, request.getStartingCash());

        return new ResponseEntity<>(ApiResponse.created(toResponse(shift), "Turno de caja abierto exitosamente"),
                HttpStatus.CREATED);
    }

    @PostMapping("/{shiftId}/close")
    // @PreAuthorize("hasPermission(#orgId, 'Organization', 'POS_ACCESS')")
    @Operation(summary = "Cerrar Turno de Caja", description = "Cierra el turno de caja especificado, registrando el dinero conteo real y calculando descuadres.")
    public ResponseEntity<ApiResponse<CashShiftResponse>> closeShift(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @PathVariable UUID shiftId,
            @Valid @RequestBody CloseShiftRequest request) {

        CashShift shift = closeShiftUseCase.closeShift(shiftId, request.getActualCash());

        return ResponseEntity.ok(ApiResponse.ok(toResponse(shift), "Turno de caja cerrado exitosamente"));
    }

    @GetMapping("/current")
    // @PreAuthorize("hasPermission(#orgId, 'Organization', 'POS_ACCESS')")
    @Operation(summary = "Obtener Turno Abierto Actual", description = "Obtiene los detalles del turno de caja que se encuentra actualmente abierto, si existe.")
    public ResponseEntity<ApiResponse<CashShiftResponse>> getCurrentShift(
            @RequestHeader("X-Organization-Id") UUID orgId) {

        Optional<CashShift> shiftOpt = getCurrentShiftUseCase.getCurrentShift();

        if (shiftOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(null, "No hay ningún turno de caja abierto actualmente"));
        }

        return ResponseEntity.ok(ApiResponse.ok(toResponse(shiftOpt.get())));
    }

    private CashShiftResponse toResponse(CashShift shift) {
        return CashShiftResponse.builder()
                .id(shift.getId())
                .openedBy(shift.getOpenedBy())
                .openedAt(shift.getOpenedAt())
                .closedAt(shift.getClosedAt())
                .startingCash(shift.getStartingCash())
                .expectedCash(shift.getExpectedCash())
                .actualCash(shift.getActualCash())
                .difference(shift.getDifference())
                .status(shift.getStatus().name())
                .build();
    }
}
