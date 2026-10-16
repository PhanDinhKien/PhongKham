package mirai.sale.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UpdateUserRequest(
        @NotBlank(message = "Tên không được để trống")
        @Size(max = 250, message = "Tên tối đa 250 ký tự")
        String name,

        @Size(max = 50, message = "Mã nhân viên tối đa 50 ký tự")
        String staffCode,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email tối đa 100 ký tự")
        String email,

        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        String phone,

        @Size(max = 72, message = "Mật khẩu tối đa 72 ký tự")
        String password,

        @NotNull(message = "Phải chọn trạng thái")
        Boolean active,

        @NotEmpty(message = "Phải chọn ít nhất một vai trò")
        Set<@NotBlank String> roleCode
) {}
