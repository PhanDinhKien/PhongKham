package mirai.sale.modules.location.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignContractToLocationRequest(
    @NotNull(message = "Vui lòng chọn hợp đồng")
    UUID contractId,

    Boolean active,

    String shiftConfig
) {
    public AssignContractToLocationRequest {
        if (active == null) {
            active = true;
        }
    }
}
