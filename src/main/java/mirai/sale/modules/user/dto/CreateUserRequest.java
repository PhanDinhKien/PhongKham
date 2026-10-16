package mirai.sale.modules.user.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record CreateUserRequest(
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 250, message = "Chiều dài tên tài khoản không được vượt quá 250 ký tự")
    String name, 
    
    @Size(max = 50, message = "Mã nhân viên tối đa 50 ký tự")
    String staffCode,

    @NotBlank(message = "Tên đăng nhập / Email không được để trống")
    @Size(max = 100, message = "Địa chỉ email không được để trống")
    @Email(message = "Địa chỉ email không đúng định dạng")
    String email, 
    
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    String phone,

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 72, message = "Mật khẩu từ 6 đến 72 ký tự")
    String password,
    
    Boolean active,

    @NotEmpty(message = "Phải chọn ít nhất một vai trò")
    Set<@NotBlank String> roleCode
) {
    public CreateUserRequest {
        if (active == null) {
            active = true;
        }
    }
}
