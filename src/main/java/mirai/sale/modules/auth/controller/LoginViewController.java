package mirai.sale.modules.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mirai.sale.common.exception.AuthException;
import mirai.sale.modules.auth.dto.LoginForm;
import mirai.sale.modules.auth.dto.TokenResponse;
import mirai.sale.modules.auth.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller xử lý luồng hiển thị View và gửi Form đăng nhập theo mô hình MVC.
 */
@Controller
public class LoginViewController {

    private final AuthService authService;

    public LoginViewController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Hiển thị giao diện màn hình đăng nhập (View: login.html)
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Bạn đã đăng xuất khỏi hệ thống thành công.");
        }
        return "login";
    }

    /**
     * Nhận dữ liệu submit form từ View, xác thực qua AuthService và phản hồi lại Model/View
     */
    @PostMapping("/login")
    public String handleLogin(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult bindingResult,
            HttpServletResponse response,
            Model model
    ) {
        // Kiểm tra lỗi validate (Model Binding)
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            // Gọi tầng Service xử lý nghiệp vụ xác thực
            TokenResponse tokenResponse = authService.login(form.getEmail(), form.getPassword());

            // Lưu JWT accessToken vào cookie để hỗ trợ các request MVC / Web sau này
            Cookie tokenCookie = new Cookie("accessToken", tokenResponse.accessToken());
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(true);
            tokenCookie.setMaxAge((int) tokenResponse.expiresIn());
            response.addCookie(tokenCookie);

            // Đăng nhập thành công -> Điều hướng vào màn hình trang chủ quản trị
            return "redirect:/admin/dashboard";
        } catch (AuthException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "login";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Đăng nhập thất bại: " + ex.getMessage());
            return "login";
        }
    }
}
