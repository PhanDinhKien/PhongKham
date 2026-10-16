package mirai.sale.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import mirai.sale.modules.auth.dto.TokenResponse;
import mirai.sale.modules.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Chạy với mọi request:
 * 1. Kiểm tra accessToken (Header hoặc Cookie).
 * 2. Nếu accessToken hết hạn hoặc chưa có, tự động sử dụng refreshToken để cấp lại token mới.
 * 3. Hợp lệ thì nạp thông tin người dùng vào SecurityContext và cập nhật Cookie mới.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final ObjectProvider<AuthService> authServiceProvider;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectProvider<AuthService> authServiceProvider) {
        this.jwtService = jwtService;
        this.authServiceProvider = authServiceProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        boolean authenticated = false;

        if (token != null) {
            try {
                // 1. Kiểm tra chữ ký và hạn dùng của token
                Claims claims = jwtService.parse(token);
                setAuthentication(claims);
                authenticated = true;
            } catch (ExpiredJwtException e) {
                // Token đã hết hạn (sau 15 phút), chuyển sang kiểm tra refreshToken
                log.debug("AccessToken đã hết hạn, tiến hành kiểm tra refreshToken...");
            } catch (JwtException | IllegalArgumentException e) {
                // Token không hợp lệ hoặc bị sửa đổi
                SecurityContextHolder.clearContext();
            }
        }

        // 2. Nếu chưa được xác thực (token thiếu hoặc đã hết hạn 15 phút), tự động gia hạn bằng refreshToken
        if (!authenticated) {
            String refreshToken = extractRefreshToken(request);
            if (refreshToken != null && !refreshToken.isBlank()) {
                try {
                    AuthService authService = authServiceProvider.getIfAvailable();
                    if (authService != null) {
                        TokenResponse newTokens = authService.refresh(refreshToken);

                        // Cập nhật lại 2 Cookie accessToken và refreshToken mới vào response
                        addAuthCookies(response, newTokens, authService.getRefreshTokenSeconds());

                        // Đính kèm accessToken mới vào Header response để API client có thể nhận
                        response.setHeader("X-Access-Token", newTokens.accessToken());

                        // Gán accessToken mới vào SecurityContext
                        Claims newClaims = jwtService.parse(newTokens.accessToken());
                        setAuthentication(newClaims);
                        authenticated = true;
                        log.debug("Đã tự động gia hạn phiên đăng nhập thành công bằng refreshToken");
                    }
                } catch (Exception ex) {
                    log.debug("Tự động gia hạn token thất bại: {}", ex.getMessage());
                    clearAuthCookies(response);
                    SecurityContextHolder.clearContext();
                }
            }
        }

        // 3. Cho request đi tiếp tới Controller
        chain.doFilter(request, response);
    }

    private void setAuthentication(Claims claims) {
        List<?> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles != null
                ? roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList()
                : List.of();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                claims.get("email", String.class), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String extractToken(HttpServletRequest request) {
        // Ưu tiên đọc từ Header Authorization
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // Nếu không có trong header, đọc từ Cookie accessToken
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        // 1. Đọc từ header X-Refresh-Token nếu có
        String header = request.getHeader("X-Refresh-Token");
        if (header != null && !header.isBlank()) {
            return header.trim();
        }

        // 2. Đọc từ Cookie refreshToken
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void addAuthCookies(HttpServletResponse response, TokenResponse tokenResponse, long refreshTokenSeconds) {
        Cookie tokenCookie = new Cookie("accessToken", tokenResponse.accessToken());
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge((int) tokenResponse.expiresIn());
        response.addCookie(tokenCookie);

        Cookie refreshCookie = new Cookie("refreshToken", tokenResponse.refreshToken());
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge((int) refreshTokenSeconds);
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
}