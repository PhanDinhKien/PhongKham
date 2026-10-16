package mirai.sale.modules.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLocationRequest(
    @NotBlank(message = "Tên địa điểm không được để trống")
    @Size(max = 255, message = "Tên địa điểm tối đa 255 ký tự")
    String name,

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    String address,

    Boolean active
) {
    public UpdateLocationRequest {
        if (active == null) {
            active = true;
        }
        if (name != null) {
            name = name.trim();
        }
        if (address != null) {
            address = address.trim();
        }
    }
}
