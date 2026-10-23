package mirai.sale.modules.receipt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateReceiptRequest(
        UUID patientId,
        String patientCode,
        String recordCode,
        @NotBlank(message = "Tên người bệnh không được để trống")
        String patientName,
        String gender,
        String birthYear,
        UUID contractId,
        String companyCode,
        String companyName,
        String contractCode,
        String contractName,
        String packageName,
        List<ReceiptServiceItem> services,
        BigDecimal subtotal,
        String discountCode,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        @NotNull(message = "Số tiền thực thu không được để trống")
        BigDecimal finalAmount,
        BigDecimal customerPaid,
        BigDecimal changeAmount,
        String paymentMethod,
        String cashierName,
        String note
) {
    public record ReceiptServiceItem(
            String id,
            String code,
            String name,
            BigDecimal price,
            String serviceType
    ) {}
}
