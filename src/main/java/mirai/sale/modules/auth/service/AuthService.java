/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.auth.service;


import mirai.sale.common.exception.AuthException;
import mirai.sale.modules.auth.dto.TokenResponse;
import mirai.sale.modules.auth.entity.RefreshToken;
import mirai.sale.modules.auth.repository.RefreshTokenRepository;
import mirai.sale.modules.user.entity.User;
import mirai.sale.modules.user.repository.UserRepository;
import mirai.sale.modules.user.repository.UserRoleRepository;
import mirai.sale.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import static java.util.Objects.hash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private static final int MAX_FAILED_LOGIN = 5;
    private static final int LOCK_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshTokenDays;

    public AuthService(UserRepository userRepository, UserRoleRepository userRoleRepository,
                       RefreshTokenRepository refreshTokenRepository, JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.jwt.refresh-token-days}") long refreshTokenDays) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenDays = refreshTokenDays;
    }
    
    public TokenResponse login (String email , String password){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new AuthException("Email không đúng")); 
        
        if(!user.getActive()){
            throw new AuthException("Tài khoản của bạn đang bị khóa"); 
        }
        
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AuthException("Tài khoản bị khoá đến " + user.getLockedUntil());
        }
        
        if(!passwordEncoder.matches(password, user.getPasswordHash())){
           int failed = user.getFailedLoginCount() + 1 ; 
           if(failed >= MAX_FAILED_LOGIN) {
               user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
               failed = 0;
           }
           user.setFailedLoginCount(failed);
           
           throw new AuthException("Sai mật khẩu"); 
        }
        
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        
        return issueTokens(user);
    }
    
    private static String generateRandomToken() {
        byte[] bytes = new byte[256];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    private static String hash(String value) {
        if (value == null) {
            throw new AuthException("Thiếu refresh token");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private TokenResponse issueTokens(User user ) {
       List<String> roles = userRoleRepository.findRoleCodeByUser(user);
        String accessToken = jwtService.generateAccessToken(user, roles);

        String rawRefreshToken = generateRandomToken();
        RefreshToken refreshToken = new RefreshToken(user, hash(rawRefreshToken),
                LocalDateTime.now().plusDays(refreshTokenDays));
        refreshTokenRepository.save(refreshToken);

        // Trả bản gốc cho client, DB chỉ giữ bản băm
        return new TokenResponse(accessToken, rawRefreshToken, "Bearer", jwtService.getAccessTokenSeconds());
       
    }

    /**
     * Cấp lại accessToken và refreshToken mới khi nhận được refreshToken hợp lệ
     */
    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AuthException("Refresh token không hợp lệ hoặc thiếu");
        }

        String tokenHash = hash(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("Refresh token không tồn tại hoặc đã hết hiệu lực"));

        if (!token.isActive()) {
            throw new AuthException("Refresh token đã hết hạn hoặc đã bị thu hồi");
        }

        User user = token.getUser();
        if (user == null || !Boolean.TRUE.equals(user.getActive())) {
            throw new AuthException("Tài khoản người dùng đã bị khóa hoặc không tồn tại");
        }

        // Thu hồi token cũ (Refresh token rotation)
        token.revoke();
        refreshTokenRepository.save(token);

        // Cấp phát cặp token mới
        return issueTokens(user);
    }

    /**
     * Thu hồi refresh token khi đăng xuất
     */
    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        try {
            String tokenHash = hash(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
            });
        } catch (Exception ignored) {
        }
    }

    /**
     * Thời gian sống của refresh token tính theo giây
     */
    public long getRefreshTokenSeconds() {
        return refreshTokenDays * 24 * 60 * 60;
    }
}
