package resto_dev.modules.adminsaas.members.application.port.input;

import resto_dev.modules.adminsaas.members.domain.model.StaffStats;
import java.util.UUID;

/**
 * Use case to get aggregated statistics for an organization's staff.
 */
public interface GetStaffStatsUseCase {
    StaffStats execute(UUID organizationId);
}
