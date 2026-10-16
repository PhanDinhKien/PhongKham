package mirai.sale.modules.contract.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignLocationRequest(
    @NotNull(message = "Địa điểm không được để trống")
    UUID locationId
) {
}
