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
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        if (logout != null) {
            String refreshToken = extractCookie(request, "refreshToken");
            if (refreshToken != null) {
                authService.revokeRefreshToken(refreshToken);
            }
            clearAuthCookies(response);
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            model.addAttribute("infoMessage", "Bạn đã đăng xuất khỏi hệ thống thành công.");
        }
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    /**
     * Endpoint đăng xuất hệ thống
     */
    @GetMapping("/logout")
    public String handleLogout(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractCookie(request, "refreshToken");
        if (refreshToken != null) {
            authService.revokeRefreshToken(refreshToken);
        }
        clearAuthCookies(response);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return "redirect:/login?logout";
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

            // 1. Lưu JWT accessToken vào cookie (thời hạn 15 phút)
            Cookie tokenCookie = new Cookie("accessToken", tokenResponse.accessToken());
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(true);
            tokenCookie.setMaxAge((int) tokenResponse.expiresIn());
            response.addCookie(tokenCookie);

            // 2. Lưu refreshToken vào cookie (thời hạn 7 ngày)
            Cookie refreshCookie = new Cookie("refreshToken", tokenResponse.refreshToken());
            refreshCookie.setPath("/");
            refreshCookie.setHttpOnly(true);
            refreshCookie.setMaxAge((int) authService.getRefreshTokenSeconds());
            response.addCookie(refreshCookie);

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

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("accessToken", "");
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

    private String extractCookie(jakarta.servlet.http.HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (cookieName.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
