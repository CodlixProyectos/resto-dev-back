package resto_dev.modules.menu.categories.infrastructure.web.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Objeto DTO que representa una categoría devuelta al cliente")
public class CategoryResponse {

    @Schema(description = "El ID único de la categoría")
    private UUID id;

    @Schema(description = "Nombre de la categoría")
    private String name;

    @Schema(description = "Descripción de la categoría")
    private String description;

    @Schema(description = "Si la categoría está activa")
    private boolean isActive;

    @Schema(description = "Fecha de creación de la categoría")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última modificación de la categoría")
    private LocalDateTime updatedAt;
}
