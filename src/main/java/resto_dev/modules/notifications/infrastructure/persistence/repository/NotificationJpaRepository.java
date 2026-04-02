package resto_dev.modules.notifications.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.notifications.infrastructure.persistence.entity.NotificationJpaEntity;

import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {
    Page<NotificationJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);
    
    long countByOrganizationIdAndReadFalse(UUID organizationId);
}
