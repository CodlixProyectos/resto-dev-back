package resto_dev.modules.menu.categories.application.command;

import lombok.Builder;
import lombok.Value;

/**
 * Pure Application Command (CQS).
 * Agnostic of Web or Database frameworks.
 */
@Value
@Builder
public class UpdateCategoryCommand {
    String name;
    String description;
    boolean isActive;
}
