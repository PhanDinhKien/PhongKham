package mirai.sale.modules.quota.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateServiceQuotaRequest(
    UUID locationId,

    UUID contractId,

    Integer shift1Quantity,

    Integer shift2Quantity,

    String shiftDetails,

    LocalDate validFrom,

    LocalDate validTo,

    Boolean active
) {
    public UpdateServiceQuotaRequest {
        if (shift1Quantity == null) shift1Quantity = 0;
        if (shift2Quantity == null) shift2Quantity = 0;
        if (active == null) active = true;
    }
}
