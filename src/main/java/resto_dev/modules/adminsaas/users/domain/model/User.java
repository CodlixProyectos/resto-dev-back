package resto_dev.modules.adminsaas.users.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User domain entity — pure POJO, no JPA annotations.
 * Represents a global identity. Roles are handled via restaurant_members.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private boolean superAdmin;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
