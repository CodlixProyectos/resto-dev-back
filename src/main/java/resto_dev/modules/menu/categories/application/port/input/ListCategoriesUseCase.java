package resto_dev.modules.menu.categories.application.port.input;

import resto_dev.modules.menu.categories.domain.model.Category;

import resto_dev.modules.menu.categories.application.query.ListCategoriesQuery;
import resto_dev.shared.common.pagination.PageModel;

public interface ListCategoriesUseCase {
    PageModel<Category> execute(ListCategoriesQuery query);
}
