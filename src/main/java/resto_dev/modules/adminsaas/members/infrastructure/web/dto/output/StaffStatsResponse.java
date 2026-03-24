package resto_dev.modules.adminsaas.members.infrastructure.web.dto.output;

import java.math.BigDecimal;

/**
 * DTO for staff statistics response.
 */
public record StaffStatsResponse(
    long totalEmployees,
    long activeCount,
    long onLeaveCount,
    BigDecimal totalPayroll
) {
}
