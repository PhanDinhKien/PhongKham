package mirai.sale.modules.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateRoleRequest(
        @NotBlank(message = "Tên vai trò không được để trống")
        @Size(max = 100, message = "Tên vai trò tối đa 100 ký tự")
        String name,

        @Size(max = 250, message = "Mô tả tối đa 250 ký tự")
        String description,

        @NotNull(message = "Phải chọn trạng thái")
        Boolean active,

        List<String> permissionCodes
) {
    public UpdateRoleRequest {
        if (permissionCodes == null) permissionCodes = List.of();
    }
}
