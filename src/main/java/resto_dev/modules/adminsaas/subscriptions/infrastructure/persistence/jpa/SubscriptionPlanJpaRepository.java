package resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanJpaRepository extends JpaRepository<SubscriptionPlanJpaEntity, UUID> {

    Optional<SubscriptionPlanJpaEntity> findByName(String name);
}
