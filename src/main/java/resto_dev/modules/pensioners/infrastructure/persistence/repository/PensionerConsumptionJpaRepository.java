package resto_dev.modules.pensioners.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import resto_dev.modules.pensioners.infrastructure.persistence.entity.PensionerConsumptionJpaEntity;

import java.math.BigDecimal;
import java.util.UUID;

public interface PensionerConsumptionJpaRepository extends JpaRepository<PensionerConsumptionJpaEntity, UUID> {

    @Query("SELECT c FROM PensionerConsumptionJpaEntity c " +
           "WHERE c.pensionerId = :pensionerId " +
           "AND c.date BETWEEN :startDate AND :endDate " +
           "ORDER BY c.date DESC")
    Page<PensionerConsumptionJpaEntity> findByPensionerAndDateRange(
            @Param("pensionerId") UUID pensionerId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.totalAmount), 0) FROM PensionerConsumptionJpaEntity c " +
           "WHERE c.pensionerId = :pensionerId " +
           "AND c.date BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountByPensionerAndDateRange(
            @Param("pensionerId") UUID pensionerId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT c FROM PensionerConsumptionJpaEntity c " +
           "WHERE c.pensionerId = :pensionerId " +
           "AND MONTH(c.date) = :month " +
           "AND YEAR(c.date) = :year " +
           "ORDER BY c.date DESC")
    Page<PensionerConsumptionJpaEntity> findByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.totalAmount), 0) FROM PensionerConsumptionJpaEntity c " +
           "WHERE c.pensionerId = :pensionerId " +
           "AND MONTH(c.date) = :month " +
           "AND YEAR(c.date) = :year")
    BigDecimal sumTotalAmountByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year);
}
