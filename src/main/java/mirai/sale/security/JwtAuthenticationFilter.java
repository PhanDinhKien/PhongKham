package mirai.sale.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Chạy với mọi request: đọc JWT trong header Authorization hoặc Cookie accessToken,
 * hợp lệ thì báo cho Spring Security biết ai đang gọi API/truy cập trang.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                // 1. Kiểm tra chữ ký và hạn dùng của token
                Claims claims = jwtService.parse(token);

                // 2. Đọc danh sách role trong token, đổi ADMIN → ROLE_ADMIN
                List<?> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles != null
                        ? roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList()
                        : List.of();

                // 3. Ghi thông tin người dùng vào SecurityContext
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        claims.get("email", String.class), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException | IllegalArgumentException e) {
                // Token sai, bị sửa hoặc hết hạn → coi như chưa đăng nhập
                SecurityContextHolder.clearContext();
            }
        }

        // 4. Cho request đi tiếp tới Controller
        chain.doFilter(request, response);
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
}