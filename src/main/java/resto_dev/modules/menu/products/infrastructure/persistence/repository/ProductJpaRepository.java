package resto_dev.modules.menu.products.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import resto_dev.modules.menu.products.infrastructure.persistence.entity.ProductJpaEntity;

import java.util.UUID;

@Repository
public interface ProductJpaRepository
        extends JpaRepository<ProductJpaEntity, UUID>, JpaSpecificationExecutor<ProductJpaEntity> {

    boolean existsByNameAndCategoryId(String name, UUID categoryId);
}
