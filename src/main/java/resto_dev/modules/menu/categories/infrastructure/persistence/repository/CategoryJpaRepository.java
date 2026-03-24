package resto_dev.modules.menu.categories.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import resto_dev.modules.menu.categories.infrastructure.persistence.entity.CategoryJpaEntity;

import java.util.UUID;

@Repository
public interface CategoryJpaRepository
        extends JpaRepository<CategoryJpaEntity, UUID>, JpaSpecificationExecutor<CategoryJpaEntity> {

    // Spring Data JPA methods like findByName etc can go here

}
