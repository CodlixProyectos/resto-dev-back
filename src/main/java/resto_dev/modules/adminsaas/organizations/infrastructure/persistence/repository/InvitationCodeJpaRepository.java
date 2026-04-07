package resto_dev.modules.adminsaas.organizations.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.adminsaas.organizations.infrastructure.persistence.entity.InvitationCodeJpaEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationCodeJpaRepository extends JpaRepository<InvitationCodeJpaEntity, UUID> {
    Optional<InvitationCodeJpaEntity> findByCodeAndUsedFalse(String code);
}
