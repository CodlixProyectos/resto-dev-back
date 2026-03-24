package resto_dev.modules.adminsaas.members.application.port.input;

import java.util.UUID;

public interface GetStaffPerformanceUseCase {
    StaffPerformance execute(StaffPerformanceQuery query);

    record StaffPerformanceQuery(
            UUID organizationId,
            UUID userId
    ) {}

    record StaffPerformance(
            int ordersToday,
            int tablesAssigned,
            double avgRating
    ) {}
}
