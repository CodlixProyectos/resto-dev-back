package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.entity.PensionerConsumptionJpaEntity;

import java.math.BigDecimal;
import java.util.UUID;

public interface PensionerConsumptionJpaRepository extends JpaRepository<PensionerConsumptionJpaEntity, UUID> {

    @Query(value = "SELECT * FROM admin.pensioner_consumptions " +
           "WHERE pensioner_id = :pensionerId " +
           "AND EXTRACT(MONTH FROM date) = :month " +
           "AND EXTRACT(YEAR FROM date) = :year " +
           "ORDER BY date DESC", 
           nativeQuery = true)
    Page<PensionerConsumptionJpaEntity> findByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year,
            Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM admin.pensioner_consumptions " +
           "WHERE pensioner_id = :pensionerId " +
           "AND EXTRACT(MONTH FROM date) = :month " +
           "AND EXTRACT(YEAR FROM date) = :year", 
           nativeQuery = true)
    BigDecimal sumTotalAmountByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year);
}
