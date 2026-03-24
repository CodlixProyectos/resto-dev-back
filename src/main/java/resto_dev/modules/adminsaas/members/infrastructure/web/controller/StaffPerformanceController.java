package resto_dev.modules.adminsaas.members.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.adminsaas.members.application.port.input.GetStaffPerformanceUseCase;
import resto_dev.modules.adminsaas.members.application.port.input.GetStaffPerformanceUseCase.StaffPerformance;
import resto_dev.modules.adminsaas.members.application.port.input.GetStaffPerformanceUseCase.StaffPerformanceQuery;
import resto_dev.shared.responses.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Staff Performance", description = "Endpoints para estadísticas de rendimiento del personal.")
public class StaffPerformanceController {

    private final GetStaffPerformanceUseCase getStaffPerformanceUseCase;

    @Operation(summary = "Obtener mis estadísticas de rendimiento", description = "Devuelve los pedidos hoy, mesas asignadas y puntuación del empleado actual.")
    @GetMapping("/me/performance-stats")
    public ResponseEntity<ApiResponse<StaffPerformance>> getMyPerformance(
            @RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {

        var query = new StaffPerformanceQuery(organizationId, userId);
        var stats = getStaffPerformanceUseCase.execute(query);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
