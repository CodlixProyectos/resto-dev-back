package resto_dev.modules.menu.categories.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.menu.categories.domain.Category;
import resto_dev.modules.menu.categories.ports.in.CreateCategoryUseCase;
import resto_dev.modules.menu.categories.ports.in.DeleteCategoryUseCase;
import resto_dev.modules.menu.categories.ports.in.ListCategoriesUseCase;
import resto_dev.modules.menu.categories.ports.in.UpdateCategoryUseCase;
import resto_dev.modules.menu.categories.ports.in.dto.CreateCategoryCommand;
import resto_dev.modules.menu.categories.ports.in.dto.UpdateCategoryCommand;
import resto_dev.modules.menu.categories.ports.out.CategoryRepositoryPort;

import java.util.List;
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
                .id(UUID.randomUUID())
                .name(command.getName())
                .description(command.getDescription())
                .isActive(true)
                .build();

        return categoryRepositoryPort.save(newCategory);
    }

    @Override
    public List<Category> execute() {
        return categoryRepositoryPort.findAll();
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
