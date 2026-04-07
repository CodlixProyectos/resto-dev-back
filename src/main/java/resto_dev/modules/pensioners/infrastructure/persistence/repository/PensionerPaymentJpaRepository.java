package resto_dev.modules.pensioners.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import resto_dev.modules.pensioners.infrastructure.persistence.entity.PensionerPaymentJpaEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PensionerPaymentJpaRepository extends JpaRepository<PensionerPaymentJpaEntity, UUID> {
    
    @Query("SELECT p FROM PensionerPaymentJpaEntity p " +
           "WHERE p.pensionerId = :pensionerId " +
           "AND p.date BETWEEN :startDate AND :endDate " +
           "ORDER BY p.date DESC, p.createdAt DESC")
    Page<PensionerPaymentJpaEntity> findByPensionerAndDateRange(
            @Param("pensionerId") UUID pensionerId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PensionerPaymentJpaEntity p " +
           "WHERE p.pensionerId = :pensionerId " +
           "AND p.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPensionerAndDateRange(
            @Param("pensionerId") UUID pensionerId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT p FROM PensionerPaymentJpaEntity p " +
           "WHERE p.pensionerId = :pensionerId " +
           "AND MONTH(p.date) = :month " +
           "AND YEAR(p.date) = :year " +
           "ORDER BY p.date DESC, p.createdAt DESC")
    Page<PensionerPaymentJpaEntity> findByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PensionerPaymentJpaEntity p " +
           "WHERE p.pensionerId = :pensionerId " +
           "AND MONTH(p.date) = :month " +
           "AND YEAR(p.date) = :year")
    BigDecimal sumAmountByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year);
}
