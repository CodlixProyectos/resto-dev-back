package resto_dev.modules.sales.payments.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.sales.payments.application.command.CheckoutOrderCommand;
import resto_dev.modules.sales.payments.application.command.CheckoutTableCommand;
import resto_dev.modules.sales.payments.domain.model.Payment;
import resto_dev.modules.sales.payments.infrastructure.web.dto.input.CheckoutRequest;
import resto_dev.modules.sales.payments.infrastructure.web.dto.output.PaymentResponse;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentWebMapper {

    public CheckoutOrderCommand toCommand(UUID orderId, CheckoutRequest request) {
        if (request == null)
            return null;

        var splits = request.getSplits().stream()
                .map(s -> new CheckoutOrderCommand.PaymentSplitCommand(
                        s.getAmount(),
                        s.getMethod(),
                        s.getReferenceNotes(),
                        s.getEvidenceUrl()))
                .collect(Collectors.toList());

        return new CheckoutOrderCommand(orderId, splits, request.getCashierNotes());
    }

    public CheckoutTableCommand toTableCommand(UUID tableId, CheckoutRequest request) {
        if (request == null)
            return null;

        var splits = request.getSplits().stream()
                .map(s -> new CheckoutOrderCommand.PaymentSplitCommand(
                        s.getAmount(),
                        s.getMethod(),
                        s.getReferenceNotes(),
                        s.getEvidenceUrl()))
                .collect(Collectors.toList());

        return new CheckoutTableCommand(tableId, splits, request.getCashierNotes());
    }

    public PaymentResponse toResponse(Payment domain) {
        if (domain == null)
            return null;

        return PaymentResponse.builder()
                .id(domain.getId())
                .amount(domain.getAmount())
                .method(domain.getMethod())
                .referenceNotes(domain.getReferenceNotes())
                .evidenceUrl(domain.getEvidenceUrl())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
