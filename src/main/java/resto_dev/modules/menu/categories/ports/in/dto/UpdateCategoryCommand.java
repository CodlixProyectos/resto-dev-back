package resto_dev.modules.menu.categories.ports.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Objeto DTO para actualizar una categoría de menú existente")
public class UpdateCategoryCommand {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Schema(description = "El nuevo nombre de la categoría", example = "Bebidas Frías")
    private String name;

    @Schema(description = "La nueva descripción para la categoría", example = "Gaseosas y jugos")
    private String description;

    @Schema(description = "Indica si la categoría está activa y visible en el menú", example = "true")
    private boolean isActive;
}
