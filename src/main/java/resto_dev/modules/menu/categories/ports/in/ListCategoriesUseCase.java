package resto_dev.modules.menu.categories.ports.in;

import resto_dev.modules.menu.categories.domain.Category;

import java.util.List;

public interface ListCategoriesUseCase {
    List<Category> execute();
}
