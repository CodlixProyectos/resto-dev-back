package resto_dev.modules.layout.tables.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import resto_dev.modules.layout.tables.domain.model.TableStatus;

import java.util.UUID;

public record CreateTableRequest(
        @NotNull(message = "El ID de la zona es obligatorio.") UUID zoneId,

        @NotBlank(message = "El número de mesa es obligatorio.") @Size(max = 50, message = "El número no debe superar los 50 caracteres.") String tableNumber,

        @Positive(message = "La capacidad debe ser un número positivo.") int capacity,

        TableStatus status) {
}
