package resto_dev.shared.common.pagination;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO para enviar una respuesta paginada al cliente (Web).
 */
@Schema(description = "Contenedor genérico para respuestas con paginación")
public record PageResponse<T>(

        @Schema(description = "Lista de elementos encontrados en esta página") List<T> content,

        @Schema(description = "Número de página actual (0-indexed)", example = "0") int page,

        @Schema(description = "Tamaño de la página solicitada (elementos máximos)", example = "10") int size,

        @Schema(description = "Cantidad total de elementos que coinciden con la búsqueda", example = "45") long totalElements,

        @Schema(description = "Cantidad total de páginas disponibles de acuerdo al tamaño", example = "5") int totalPages) {
}
