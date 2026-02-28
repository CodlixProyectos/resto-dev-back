package resto_dev.modules.menu.categories.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.menu.categories.domain.model.Category;
import resto_dev.modules.menu.categories.application.command.CreateCategoryCommand;
import resto_dev.modules.menu.categories.application.command.UpdateCategoryCommand;
import resto_dev.modules.menu.categories.infrastructure.web.dto.input.CreateCategoryRequest;
import resto_dev.modules.menu.categories.infrastructure.web.dto.input.UpdateCategoryRequest;
import resto_dev.modules.menu.categories.infrastructure.web.dto.output.CategoryResponse;

@Component
public class CategoryWebMapper {

    public CreateCategoryCommand toCommand(CreateCategoryRequest request) {
        if (request == null)
            return null;
        return CreateCategoryCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public UpdateCategoryCommand toCommand(UpdateCategoryRequest request) {
        if (request == null)
            return null;
        return UpdateCategoryCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.isActive())
                .build();
    }

    public CategoryResponse toResponse(Category category) {
        if (category == null)
            return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
