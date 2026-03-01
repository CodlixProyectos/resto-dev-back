package resto_dev.modules.sales.payments.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.mapper.OrderWebMapper;
import resto_dev.modules.sales.payments.application.command.CheckoutOrderCommand;
import resto_dev.modules.sales.payments.application.port.input.CheckoutOrderUseCase;
import resto_dev.modules.sales.payments.infrastructure.web.dto.input.CheckoutRequest;
import resto_dev.modules.sales.payments.infrastructure.web.mapper.PaymentWebMapper;
import resto_dev.shared.responses.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/checkout")
@RequiredArgsConstructor
@Tag(name = "Payments & Checkout", description = "Endpoints para el cierre de cuentas y registro de métodos de pago en el POS")
public class PaymentController {

    private final CheckoutOrderUseCase checkoutOrderUseCase;
    private final PaymentWebMapper paymentMapper;
    private final OrderWebMapper orderWebMapper; // Reutilizamos DTOs de orden para la respuesta final

    @Operation(summary = "Pagar y Cerrar Orden", description = "Registra los métodos de pago (split) que cubren el monto total, marca la orden como PAGADA y libera la mesa para los siguientes clientes. Autorizado para Admins, Managers o Cajeros explícitos.")
    @PreAuthorize("hasAuthority('MANAGE_ORDERS')")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> processCheckout(
            @PathVariable UUID orderId,
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutOrderCommand command = paymentMapper.toCommand(orderId, request);

        Order finalizedOrder = checkoutOrderUseCase.execute(command);

        OrderResponse response = orderWebMapper.toResponse(finalizedOrder);

        return ResponseEntity.ok(ApiResponse.ok(response, "Orden pagada y mesa liberada exitosamente."));
    }
}
