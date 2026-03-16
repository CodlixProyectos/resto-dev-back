package resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import resto_dev.modules.adminsaas.pensioners.infrastructure.persistence.entity.PensionerPaymentJpaEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PensionerPaymentJpaRepository extends JpaRepository<PensionerPaymentJpaEntity, UUID> {
    
    @Query(value = "SELECT * FROM admin.pensioner_payments " +
           "WHERE pensioner_id = :pensionerId " +
           "AND EXTRACT(MONTH FROM date) = :month " +
           "AND EXTRACT(YEAR FROM date) = :year " +
           "ORDER BY date DESC, created_at DESC", 
           nativeQuery = true)
    Page<PensionerPaymentJpaEntity> findByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year,
            Pageable pageable
    );

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM admin.pensioner_payments " +
           "WHERE pensioner_id = :pensionerId " +
           "AND EXTRACT(MONTH FROM date) = :month " +
           "AND EXTRACT(YEAR FROM date) = :year", 
           nativeQuery = true)
    BigDecimal sumAmountByPensionerAndMonth(
            @Param("pensionerId") UUID pensionerId,
            @Param("month") int month,
            @Param("year") int year);
}
