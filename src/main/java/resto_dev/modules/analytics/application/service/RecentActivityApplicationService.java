package resto_dev.modules.analytics.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.analytics.application.port.input.GetRecentActivityUseCase;
import resto_dev.modules.analytics.application.port.output.AnalyticsRepositoryPort;
import resto_dev.modules.analytics.domain.model.RecentActivity;
import resto_dev.shared.model.DateRange;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentActivityApplicationService implements GetRecentActivityUseCase {

    private final AnalyticsRepositoryPort repositoryPort;

    @Override
    public List<RecentActivity> execute(UUID organizationId, DateRange dateRange, int limit, int offset) {
        if (dateRange == null || dateRange.getStartDate() == null) {
            dateRange = DateRange.of(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now()
            );
        }
        return repositoryPort.getRecentActivity(organizationId, dateRange, limit, offset);
    }
}
