package mirai.sale.modules.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import mirai.sale.modules.service.entity.ServiceType;

public record UpdateServiceRequest(
    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 255, message = "Tên dịch vụ tối đa 255 ký tự")
    String name,

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá không được âm")
    BigDecimal price,

    @NotNull(message = "Loại dịch vụ không được để trống")
    ServiceType serviceType,

    Boolean active,

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    String description
) {
    public UpdateServiceRequest {
        if (active == null) {
            active = true;
        }
        if (name != null) {
            name = name.trim();
        }
    }
}
