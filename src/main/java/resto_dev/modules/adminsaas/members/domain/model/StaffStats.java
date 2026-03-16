package resto_dev.modules.adminsaas.members.domain.model;

import java.math.BigDecimal;

/**
 * Domain record representing aggregated staff statistics.
 */
public record StaffStats(
    long totalEmployees,
    long activeCount,
    long onLeaveCount,
    BigDecimal totalPayroll
) {
}
