package resto_dev.modules.adminsaas.members.infrastructure.web.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddMemberRequest {
    @NotBlank(message = "El nombre completo es requerido")
    private String fullName;

    private String email;

    private String dni;

    private String phoneNumber;

    @NotBlank(message = "El rol es requerido")
    private String roleName;

    @NotBlank(message = "El PIN es requerido para acceso POS")
    @Size(min = 6, max = 6, message = "El PIN debe tener exactamente 6 caracteres")
    private String pin;

    private BigDecimal salary;
}

