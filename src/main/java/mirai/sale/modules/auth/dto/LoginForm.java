package mirai.sale.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Model biểu mẫu đăng nhập cho mô hình MVC.
 */
public class LoginForm {

    @NotBlank(message = "Vui lòng nhập địa chỉ email")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;

    private boolean rememberMe;

    public LoginForm() {
    }

    public LoginForm(String email, String password, boolean rememberMe) {
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
