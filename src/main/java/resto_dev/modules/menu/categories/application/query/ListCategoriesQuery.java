package resto_dev.modules.menu.categories.application.query;

import lombok.Builder;
import lombok.Value;

/**
 * Pure Application Query.
 * Contains the parameters required to execute a paginated list with filters.
 */
@Value
@Builder
public class ListCategoriesQuery {
    String name;
    Boolean isActive;
    int page;
    int size;
}
