package mirai.sale.modules.quota.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateServiceQuotaRequest(
    @NotNull(message = "Dịch vụ áp dụng không được để trống")
    UUID serviceId,

    @NotNull(message = "Địa điểm áp dụng không được để trống")
    UUID locationId,

    UUID contractId,

    Integer shift1Quantity,

    Integer shift2Quantity,

    String shiftDetails,

    LocalDate validFrom,

    LocalDate validTo,

    Boolean active
) {
    public CreateServiceQuotaRequest {
        if (shift1Quantity == null) shift1Quantity = 0;
        if (shift2Quantity == null) shift2Quantity = 0;
        if (active == null) active = true;
    }
}
