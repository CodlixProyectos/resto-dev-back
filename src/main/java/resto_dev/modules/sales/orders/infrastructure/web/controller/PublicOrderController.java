package resto_dev.modules.sales.orders.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resto_dev.modules.sales.orders.application.command.CreateOrderCommand;
import resto_dev.modules.sales.orders.application.port.input.CreateOrderUseCase;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.infrastructure.web.dto.input.CreateOrderRequest;
import resto_dev.modules.sales.orders.infrastructure.web.dto.output.OrderResponse;
import resto_dev.modules.sales.orders.infrastructure.web.mapper.OrderWebMapper;
import resto_dev.shared.responses.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/organizations/{organizationId}/orders")
@RequiredArgsConstructor
@Tag(name = "Public Orders", description = "Endpoints públicos para emisión de comandas de clientes vía código QR")
public class PublicOrderController {

    private final CreateOrderUseCase superCreateOrderPort;
    private final OrderWebMapper orderWebMapper;

    @Operation(summary = "Crear Pedido QR", description = "Permite a los comensales pedir comida directamente a la cocina sin autenticación de usuario. Requiere Header X-Organization-Id.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateOrderRequest request) {

        // Validaciones o rate-limits irían aquí en una versión masiva

        CreateOrderCommand command = orderWebMapper.toCommand(request);
        Order order = superCreateOrderPort.execute(command);
        OrderResponse response = orderWebMapper.toResponse(order);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Orden QR generada y enviada a cocina existosamente"));
    }
}
