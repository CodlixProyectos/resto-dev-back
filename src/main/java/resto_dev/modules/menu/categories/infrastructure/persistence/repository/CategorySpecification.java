package resto_dev.modules.menu.categories.infrastructure.persistence.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import resto_dev.modules.menu.categories.infrastructure.persistence.entity.CategoryJpaEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates dynamic dynamic SQL filtering queries for Categories using JPA
 * Criteria API.
 */
public class CategorySpecification {

    public static Specification<CategoryJpaEntity> withFilters(String name, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("active"), isActive));
            }

            // Returns the conjunction AND of all valid predicates
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
