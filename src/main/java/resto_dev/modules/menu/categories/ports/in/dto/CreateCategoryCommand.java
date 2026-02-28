package resto_dev.modules.menu.categories.ports.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Objeto DTO para crear una nueva categoría de menú")
public class CreateCategoryCommand {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Schema(description = "Nombre de la categoría", example = "Bebidas Calientes")
    private String name;

    @Schema(description = "Una descripción opcional para la categoría", example = "Cafés, tés y mates")
    private String description;
}
