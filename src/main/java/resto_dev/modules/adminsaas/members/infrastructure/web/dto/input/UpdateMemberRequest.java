package resto_dev.modules.adminsaas.members.infrastructure.web.dto.input;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateMemberRequest {
    private UUID roleId;

    private String currentPin;
    @Size(min = 6, max = 6, message = "El PIN debe tener exactamente 6 caracteres")
    private String pin;
    private BigDecimal salary;
    private String status;
    private String roleName;

    // Personal Information
    private String fullName;
    private String email;
    private String dni;
    private String phoneNumber;
}
