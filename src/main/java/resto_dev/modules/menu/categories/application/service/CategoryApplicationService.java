package resto_dev.modules.menu.categories.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.categories.application.port.input.CreateCategoryUseCase;
import resto_dev.modules.menu.categories.application.port.input.DeleteCategoryUseCase;
import resto_dev.modules.menu.categories.application.port.input.ListCategoriesUseCase;
import resto_dev.modules.menu.categories.application.port.input.UpdateCategoryUseCase;
import resto_dev.modules.menu.categories.application.command.CreateCategoryCommand;
import resto_dev.modules.menu.categories.application.command.UpdateCategoryCommand;
import resto_dev.modules.menu.categories.application.port.output.CategoryRepositoryPort;

import resto_dev.modules.menu.categories.application.query.ListCategoriesQuery;
import resto_dev.shared.common.pagination.PageModel;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService implements
        CreateCategoryUseCase,
        ListCategoriesUseCase,
        UpdateCategoryUseCase,
        DeleteCategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    public Category execute(CreateCategoryCommand command) {
        Category newCategory = Category.builder()
                .name(command.getName())
                .description(command.getDescription())
                .isActive(true)
                .build();

        return categoryRepositoryPort.save(newCategory);
    }

    @Override
    public PageModel<Category> execute(ListCategoriesQuery query) {
        return categoryRepositoryPort.findAll(query);
    }

    @Override
    public Category execute(UUID id, UpdateCategoryCommand command) {
        Optional<Category> existingCategory = categoryRepositoryPort.findById(id);

        if (existingCategory.isEmpty()) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }

        Category category = existingCategory.get();
        category.setName(command.getName());
        category.setDescription(command.getDescription());
        category.setActive(command.isActive());

        return categoryRepositoryPort.save(category);
    }

    @Override
    public void execute(UUID id) {
        if (categoryRepositoryPort.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }
        categoryRepositoryPort.deleteById(id);
    }
}
