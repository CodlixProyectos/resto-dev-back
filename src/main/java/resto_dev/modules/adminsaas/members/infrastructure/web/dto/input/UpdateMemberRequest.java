package resto_dev.modules.adminsaas.members.infrastructure.web.dto.input;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateMemberRequest {
    private UUID roleId;

    private String currentPin;
    private String pin;
    private BigDecimal salary;
    private String status;
    private String roleName;
}
