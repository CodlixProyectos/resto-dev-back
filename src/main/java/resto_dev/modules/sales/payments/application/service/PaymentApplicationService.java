package resto_dev.modules.sales.payments.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.sales.orders.application.port.output.OrderRepositoryPort;
import resto_dev.modules.sales.orders.domain.model.Order;
import resto_dev.modules.sales.orders.domain.model.OrderStatus;
import resto_dev.modules.sales.payments.application.command.CheckoutOrderCommand;
import resto_dev.modules.sales.payments.application.port.input.CheckoutOrderUseCase;
import resto_dev.modules.sales.payments.application.port.output.PaymentRepositoryPort;
import resto_dev.modules.sales.payments.domain.model.Payment;
import resto_dev.shared.errors.ApiException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentApplicationService implements CheckoutOrderUseCase {

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

        // Validate splits match total
        BigDecimal totalPaid = command.splits().stream()
                .map(CheckoutOrderCommand.PaymentSplitCommand::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(order.getTotal()) < 0) {
            throw ApiException.badRequest("El monto pagado (" + totalPaid + ") no cubre el total de la orden ("
                    + order.getTotal() + ").");
        }

        // Actually, if they overpay, it might be a tip. For now, just allow >=.

        // Register Payments
        List<Payment> newPayments = command.splits().stream()
                .map(split -> Payment.builder()
                        .orderId(order.getId())
                        .amount(split.amount())
                        .method(split.method())
                        .referenceNotes(split.referenceNotes())
                        .build())
                .toList();

        newPayments.forEach(paymentRepository::save);

        // Update Order
        order.setStatus(OrderStatus.PAID);
        Order savedOrder = orderRepository.save(order);

        // Free the Table
        if (order.getTableId() != null) {
            Table table = tableRepository.findById(order.getTableId())
                    .orElseThrow(() -> ApiException.internal("Mesa no encontrada durante el cierre."));
            table.setStatus(TableStatus.FREE);
            tableRepository.save(table);
        }

        return savedOrder;
    }
}
