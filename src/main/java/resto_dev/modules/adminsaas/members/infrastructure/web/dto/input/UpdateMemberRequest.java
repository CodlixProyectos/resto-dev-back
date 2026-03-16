package resto_dev.modules.adminsaas.members.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateMemberRequest {
    @NotNull(message = "Role ID is required")
    private UUID roleId;

    private String pin;

    private BigDecimal salary;
}
