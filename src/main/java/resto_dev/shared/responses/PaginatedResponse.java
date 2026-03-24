package resto_dev.shared.responses;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standard generic response for paginated data.
 * 
 * @param <T> The DTO type of the list elements.
 */
@Getter
@Builder
public class PaginatedResponse<T> {
    private final List<T> data;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    /**
     * Map a Spring Data Page directly to our standard format.
     */
    public static <T> PaginatedResponse<T> of(Page<T> pageResult) {
        return PaginatedResponse.<T>builder()
                .data(pageResult.getContent())
                .page(pageResult.getNumber()) // Backend standardized to 0-based
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }
}
