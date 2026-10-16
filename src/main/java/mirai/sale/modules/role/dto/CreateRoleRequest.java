package mirai.sale.modules.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRoleRequest(
        @NotBlank(message = "Mã vai trò không được để trống")
        @Size(max = 50, message = "Mã vai trò tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Mã vai trò chỉ gồm chữ cái, số và dấu gạch dưới")
        String code,

        @NotBlank(message = "Tên vai trò không được để trống")
        @Size(max = 100, message = "Tên vai trò tối đa 100 ký tự")
        String name,

        @Size(max = 250, message = "Mô tả tối đa 250 ký tự")
        String description,

        Boolean active,

        List<String> permissionCodes
) {
    public CreateRoleRequest {
        if (active == null) active = true;
        if (permissionCodes == null) permissionCodes = List.of();
    }
}
