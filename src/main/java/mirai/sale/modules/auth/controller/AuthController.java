/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.auth.controller;


import mirai.sale.modules.auth.dto.LoginRequest;
import mirai.sale.modules.auth.dto.TokenResponse;
import mirai.sale.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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
        return authService.login(req.email(), req.password());
    }
}
