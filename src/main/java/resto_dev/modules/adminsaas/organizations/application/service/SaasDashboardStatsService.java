package resto_dev.modules.adminsaas.organizations.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository.OrganizationJpaRepository;
import resto_dev.modules.adminsaas.organizations.infrastructure.web.dto.output.DashboardStatsResponse;
import resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa.OrganizationSubscriptionJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaasDashboardStatsService {

    private final OrganizationJpaRepository organizationRepository;
    private final OrganizationSubscriptionJpaRepository subscriptionRepository;

    public DashboardStatsResponse getStats() {
        long totalOrgs = organizationRepository.count();
        
        var subscriptions = subscriptionRepository.findAll();
        
        BigDecimal mrr = subscriptions.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .map(s -> s.getPlan().getPriceMonthly())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long premiumCount = subscriptions.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()) && 
                            !s.getPlan().getName().equalsIgnoreCase("Free"))
                .count();

        // Fetch 5 most recent organizations for activity
        var recentOrgs = organizationRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(org -> DashboardStatsResponse.RecentActivityDto.builder()
                        .type("REGISTRATION")
                        .description(org.getName() + " se unió a la plataforma.")
                        .timeAgo(formatTimeAgo(org.getCreatedAt()))
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalActiveOrganizations(totalOrgs)
                .monthlyRecurringRevenue(mrr)
                .premiumPlansCount(premiumCount)
                .pendingTickets(0) // Placeholder
                .recentActivity(recentOrgs)
                .build();
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Hace un momento";
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        if (minutes < 1) return "Hace un momento";
        if (minutes < 60) return "Hace " + minutes + " min";
        long hours = java.time.Duration.between(dateTime, now).toHours();
        if (hours < 24) return "Hace " + hours + " horas";
        return "Hace " + java.time.Duration.between(dateTime, now).toDays() + " días";
    }
}
