package resto_dev.modules.reservations.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;
import resto_dev.modules.layout.tables.infrastructure.persistence.entity.TableJpaEntity;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Pattern(regexp = "^\\+?[0-9](?:[\\s\\-]?[0-9]){6,19}$", message = "Formato de teléfono inválido")
    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @NotNull(message = "La fecha de reserva es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @NotNull(message = "La hora de reserva es obligatoria")
    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Min(value = 1, message = "El número de personas debe ser al menos 1")
    @Column(name = "num_guests", nullable = false)
    private Integer numGuests;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @NotNull(message = "Debes asignar una mesa obligatoriamente")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private TableJpaEntity table;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
