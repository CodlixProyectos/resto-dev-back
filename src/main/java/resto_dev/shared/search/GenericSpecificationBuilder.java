package resto_dev.shared.search;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic builder to dynamically create JPA Specifications for filtering
 * entities.
 */
public class GenericSpecificationBuilder {

    private GenericSpecificationBuilder() {
        // Utility class
    }

    /**
     * Creates a specification that searches for a given text across multiple string
     * fields using OR.
     * 
     * @param searchText The text to search for
     * @param fields     The entity fields to search within
     * @param <T>        The Entity type
     * @return Specification to be used in JpaSpecificationExecutor
     */
    public static <T> Specification<T> searchInFields(String searchText, String... fields) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty() || fields.length == 0) {
                return cb.conjunction(); // returns true (no filter)
            }

            String likePattern = "%" + searchText.toLowerCase().trim() + "%";
            List<Predicate> predicates = new ArrayList<>();

            for (String field : fields) {
                predicates.add(cb.like(cb.lower(root.get(field).as(String.class)), likePattern));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Creates a specification that matches exactly an active status boolean.
     * 
     * @param isActive Boolean indicating active status
     * @param <T>      The Entity type
     * @return Specification
     */
    public static <T> Specification<T> isEntityActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), isActive);
        };
    }
}
