/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.auth.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import mirai.sale.common.exception.AuthException;
import mirai.sale.modules.auth.dto.LoginRequest;
import mirai.sale.modules.auth.dto.RefreshRequest;
import mirai.sale.modules.auth.dto.TokenResponse;
import mirai.sale.modules.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(req.email(), req.password());
        addAuthCookies(response, tokenResponse);
        return tokenResponse;
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody(required = false) RefreshRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String tokenToRefresh = null;
        if (req != null && req.refreshToken() != null && !req.refreshToken().isBlank()) {
            tokenToRefresh = req.refreshToken();
        } else {
            tokenToRefresh = extractCookie(request, "refreshToken");
        }

        if (tokenToRefresh == null || tokenToRefresh.isBlank()) {
            throw new AuthException("Thiếu refresh token để làm mới phiên đăng nhập");
        }

        TokenResponse tokenResponse = authService.refresh(tokenToRefresh);
        addAuthCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestBody(required = false) RefreshRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = null;
        if (req != null && req.refreshToken() != null) {
            token = req.refreshToken();
        } else {
            token = extractCookie(request, "refreshToken");
        }

        if (token != null) {
            authService.revokeRefreshToken(token);
        }
        clearAuthCookies(response);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã đăng xuất thành công"
        ));
    }

    private void addAuthCookies(HttpServletResponse response, TokenResponse tokenResponse) {
        Cookie tokenCookie = new Cookie("accessToken", tokenResponse.accessToken());
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge((int) tokenResponse.expiresIn());
        response.addCookie(tokenCookie);

        Cookie refreshCookie = new Cookie("refreshToken", tokenResponse.refreshToken());
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge((int) authService.getRefreshTokenSeconds());
        response.addCookie(refreshCookie);
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

    private String extractCookie(HttpServletRequest request, String cookieName) {
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
