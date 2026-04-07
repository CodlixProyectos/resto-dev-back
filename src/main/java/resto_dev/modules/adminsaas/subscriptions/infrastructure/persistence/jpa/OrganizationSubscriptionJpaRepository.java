package resto_dev.modules.adminsaas.subscriptions.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationSubscriptionJpaRepository extends JpaRepository<OrganizationSubscriptionJpaEntity, UUID> {

    /**
     * Obtains the most recent subscription for a given organization.
     */
    Optional<OrganizationSubscriptionJpaEntity> findFirstByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
