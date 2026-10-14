package mirai.sale.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
 * Chạy với mọi request: đọc JWT trong header Authorization,
 * hợp lệ thì báo cho Spring Security biết ai đang gọi API.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                // 1. Bỏ chữ "Bearer " để lấy token, kiểm tra chữ ký và hạn dùng
                Claims claims = jwtService.parse(header.substring(7));

                // 2. Đọc danh sách role trong token, đổi ADMIN → ROLE_ADMIN
                List<?> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();

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
}