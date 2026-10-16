package mirai.sale.modules.contract.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignServiceToPackageRequest(
    @NotNull(message = "Dịch vụ không được để trống")
    UUID serviceId
) {
}
