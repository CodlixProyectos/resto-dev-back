package resto_dev.modules.sales.payments.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.sales.orders.application.port.output.OrderRepositoryPort;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderItemStatus;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import resto_dev.modules.sales.payments.application.command.CheckoutOrderCommand;
import resto_dev.modules.sales.payments.application.command.CheckoutTableCommand;
import resto_dev.modules.sales.payments.application.port.input.CheckoutOrderUseCase;
import resto_dev.modules.sales.payments.application.port.input.CheckoutTableUseCase;
import resto_dev.modules.sales.payments.application.port.output.PaymentRepositoryPort;
import resto_dev.modules.sales.payments.domain.model.Payment;
import resto_dev.shared.errors.ApiException;
import resto_dev.modules.sales.orders.application.query.SearchOrdersQuery;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentApplicationService implements CheckoutOrderUseCase, CheckoutTableUseCase {

    private final OrderRepositoryPort orderRepository;
    private final TableRepositoryPort tableRepository;
    private final PaymentRepositoryPort paymentRepository;

    @Override
    @Transactional
    public Order execute(CheckoutOrderCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> ApiException.notFound("La orden no existe."));

        if (order.getStatus() == OrderStatus.PAID) {
            throw ApiException.conflict("Esta orden ya ha sido pagada y cerrada.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw ApiException.conflict("No se puede cobrar una orden cancelada.");
        }

        // VALIDACIÓN: Todos los platos deben estar entregados (o cancelados)
        boolean allServed = order.getItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.SERVED || 
                                 item.getStatus() == OrderItemStatus.CANCELLED);
        
        if (!allServed && order.getStatus() != OrderStatus.DELIVERED) {
            throw ApiException.badRequest("No se puede cobrar la orden porque aún tiene platos pendientes de entrega.");
        }

        // Validate splits match total
        BigDecimal totalPaid = command.splits().stream()
                .map(CheckoutOrderCommand.PaymentSplitCommand::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(order.getTotal()) < 0) {
            throw ApiException.badRequest("El monto pagado (" + totalPaid + ") no cubre el total de la orden ("
                    + order.getTotal() + ").");
        }

        // Register Payments
        List<Payment> newPayments = command.splits().stream()
                .map(split -> Payment.builder()
                        .orderId(order.getId())
                        .amount(split.amount())
                        .method(split.method())
                        .referenceNotes(split.referenceNotes())
                        .evidenceUrl(split.evidenceUrl())
                        .build())
                .toList();

        newPayments.forEach(paymentRepository::save);

        // Update Order
        order.setStatus(OrderStatus.PAID);
        // Sync items status as served upon payment
        order.getItems().forEach(item -> {
            if (item.getStatus() != OrderItemStatus.CANCELLED) {
                item.setStatus(OrderItemStatus.SERVED);
            }
        });
        
        Order savedOrder = orderRepository.save(order);

        // Free the Table only if no more active orders remain
        if (order.getTableId() != null) {
            boolean hasOtherActiveOrders = orderRepository.searchOrders(SearchOrdersQuery.builder()
                            .tableId(order.getTableId())
                            .statuses(List.of(
                                    OrderStatus.PENDING_KITCHEN,
                                    OrderStatus.PREPARING,
                                    OrderStatus.READY_TO_SERVE,
                                    OrderStatus.DELIVERED))
                            .page(0)
                            .size(1)
                            .build())
                    .content().stream().anyMatch(o -> !o.getId().equals(order.getId()));

            if (!hasOtherActiveOrders) {
                tableRepository.findById(order.getTableId()).ifPresent(table -> {
                    table.setStatus(TableStatus.FREE);
                    tableRepository.save(table);
                });
            }
        }
        return savedOrder;
    }

    @Override
    @Transactional
    public void execute(CheckoutTableCommand command) {
        List<Order> activeOrders = orderRepository.searchOrders(SearchOrdersQuery.builder()
                        .tableId(command.tableId())
                        .statuses(List.of(
                                OrderStatus.PENDING_KITCHEN,
                                OrderStatus.PREPARING,
                                OrderStatus.READY_TO_SERVE,
                                OrderStatus.DELIVERED))
                        .page(0)
                        .size(100)
                        .build())
                .content();

        if (activeOrders.isEmpty()) {
            tableRepository.findById(command.tableId()).ifPresent(table -> {
                if (table.getStatus() != TableStatus.FREE) {
                    table.setStatus(TableStatus.FREE);
                    tableRepository.save(table);
                }
            });
            return;
        }

        // VALIDACIÓN: Todas las órdenes activas deben estar entregadas (DELIVERED)
        boolean allOrdersDelivered = activeOrders.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.DELIVERED);

        if (!allOrdersDelivered) {
            throw ApiException.badRequest("No se puede cerrar la mesa porque hay órdenes aún en cocina o pendientes de entrega.");
        }

        BigDecimal totalOwed = activeOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = command.splits().stream()
                .map(CheckoutOrderCommand.PaymentSplitCommand::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(totalOwed) < 0) {
            throw ApiException.badRequest("El monto pagado (" + totalPaid + ") no cubre el total de la mesa ("
                    + totalOwed + ").");
        }

        CheckoutOrderCommand.PaymentSplitCommand mainSplit = command.splits().get(0);

        for (Order order : activeOrders) {
            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .amount(order.getTotal())
                    .method(mainSplit.method())
                    .referenceNotes(command.cashierNotes() != null ? command.cashierNotes() : "Pago Mesa " + command.tableId())
                    .evidenceUrl(mainSplit.evidenceUrl())
                    .build();

            paymentRepository.save(payment);
            order.setStatus(OrderStatus.PAID);
            // Sync items
            order.getItems().forEach(item -> {
                if (item.getStatus() != OrderItemStatus.CANCELLED) {
                    item.setStatus(OrderItemStatus.SERVED);
                }
            });
            orderRepository.save(order);
        }

        // Final Table Release
        tableRepository.findById(command.tableId()).ifPresent(table -> {
            table.setStatus(TableStatus.FREE);
            tableRepository.save(table);
        });
    }
}
