package mirai.sale.modules.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLocationRequest(
    @NotBlank(message = "Mã địa điểm không được để trống")
    @Size(max = 50, message = "Mã địa điểm tối đa 50 ký tự")
    String code,

    @NotBlank(message = "Tên địa điểm không được để trống")
    @Size(max = 255, message = "Tên địa điểm tối đa 255 ký tự")
    String name,

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    String address,

    Boolean active
) {
    public CreateLocationRequest {
        if (active == null) {
            active = true;
        }
        if (code != null) {
            code = code.trim().toUpperCase();
        }
        if (name != null) {
            name = name.trim();
        }
        if (address != null) {
            address = address.trim();
        }
    }
}
