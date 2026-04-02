package resto_dev.modules.notifications.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.notifications.infrastructure.persistence.entity.NotificationJpaEntity;
import resto_dev.modules.notifications.infrastructure.persistence.repository.NotificationJpaRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationAppService {

    private final NotificationJpaRepository repository;

    @Transactional(readOnly = true)
    public Page<NotificationJpaEntity> getNotifications(UUID organizationId, Pageable pageable) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
    }

    @Transactional
    public void markAsRead(UUID id) {
        repository.findById(id).ifPresent(n -> {
            n.setRead(true);
            repository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(UUID organizationId) {
        // Simple implementation for now
        Page<NotificationJpaEntity> unread = repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, Pageable.unpaged());
        unread.forEach(n -> {
            if (!n.isRead()) {
                n.setRead(true);
            }
        });
        repository.saveAll(unread);
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Transactional
    public NotificationJpaEntity save(NotificationJpaEntity entity) {
        return repository.save(entity);
    }
}
