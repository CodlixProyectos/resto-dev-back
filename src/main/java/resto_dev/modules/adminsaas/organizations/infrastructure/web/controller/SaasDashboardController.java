package resto_dev.modules.adminsaas.organizations.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import resto_dev.modules.adminsaas.organizations.application.service.SaasDashboardStatsService;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.DashboardStatsResponse;
import resto_dev.shared.responses.ApiResponse;

@RestController
@RequestMapping("/api/v1/saas/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public class SaasDashboardController {

    private final SaasDashboardStatsService statsService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getStats()));
    }
}
