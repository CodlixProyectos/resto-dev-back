package resto_dev.modules.cashregister.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseShiftRequest {

    @NotNull(message = "El monto físico real contado es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    private BigDecimal actualCash;
}
