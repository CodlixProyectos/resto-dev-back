package resto_dev.shared.common.pagination;

import java.util.List;

/**
 * Pure Domain or Application-level representation of a paginated list.
 * Completely agnostic of Spring Data (Page/Pageable) or Web representations.
 *
 * @param <T> the type of elements inside the page
 */
public record PageModel<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
