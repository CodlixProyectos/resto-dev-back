package resto_dev.modules.menu.categories.application.port.input;

import org.springframework.web.multipart.MultipartFile;
import resto_dev.modules.menu.categories.domain.model.Category;

import java.util.List;

public interface BulkAddCategoriesUseCase {
    List<Category> importCategories(MultipartFile file);
}
