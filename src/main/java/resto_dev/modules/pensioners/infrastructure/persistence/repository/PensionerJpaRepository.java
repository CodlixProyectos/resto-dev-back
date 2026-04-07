package resto_dev.modules.pensioners.infrastructure.persistence.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resto_dev.modules.pensioners.infrastructure.persistence.entity.PensionerJpaEntity;

import java.util.UUID;

@Repository
public interface PensionerJpaRepository extends JpaRepository<PensionerJpaEntity, UUID> {
    Page<PensionerJpaEntity> findAllByOrganizationId(UUID organizationId, Pageable pageable);

    @Query("SELECT p FROM PensionerJpaEntity p WHERE p.organizationId = :organizationId AND (" +
           "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "p.dni LIKE CONCAT('%', :search, '%') OR " +
           "p.phoneNumber LIKE CONCAT('%', :search, '%'))")
    Page<PensionerJpaEntity> findAllByOrganizationIdAndSearch(
            @Param("organizationId") UUID organizationId,
            @Param("search") String search,
            Pageable pageable);

    boolean existsByOrganizationIdAndDni(UUID organizationId, String dni);
    boolean existsByOrganizationIdAndEmail(UUID organizationId, String email);
}
