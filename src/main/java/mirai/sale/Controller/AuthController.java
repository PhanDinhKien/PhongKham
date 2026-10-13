/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.Controller;


import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import mirai.sale.Dto.auth.LoginRequest;
import mirai.sale.Dto.auth.RefreshRequest;
import mirai.sale.Dto.auth.TokenResponse;
import mirai.sale.Service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
     @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req, HttpServletRequest http) {
        return authService.login(req.email(), req.passsword());
    }
}
