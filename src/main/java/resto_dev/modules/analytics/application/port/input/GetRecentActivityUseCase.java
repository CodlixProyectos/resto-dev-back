package resto_dev.modules.analytics.application.port.input;

import resto_dev.modules.analytics.domain.model.RecentActivity;
import java.util.List;
import java.util.UUID;

public interface GetRecentActivityUseCase {
    List<RecentActivity> execute(UUID organizationId, int limit);
}
