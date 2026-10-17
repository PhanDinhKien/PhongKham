package mirai.sale.modules.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateLocationRequest(
    @Size(max = 50, message = "Mã địa điểm tối đa 50 ký tự")
    String code,

    @NotBlank(message = "Tên địa điểm không được để trống")
    @Size(max = 255, message = "Tên địa điểm tối đa 255 ký tự")
    String name,

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    String address,

    Boolean active,

    List<ShiftDto> shifts
) {
    public UpdateLocationRequest {
        if (active == null) {
            active = true;
        }
        if (code != null) {
            code = code.trim().toUpperCase();
            if (code.isEmpty()) {
                code = null;
            }
        }
        if (name != null) {
            name = name.trim();
        }
        if (address != null) {
            address = address.trim();
        } else {
            address = "";
        }
    }
}
