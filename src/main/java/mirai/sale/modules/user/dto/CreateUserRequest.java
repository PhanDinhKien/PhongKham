/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.user.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
/**
 *
 * @author phankien
 */
public record CreateUserRequest(
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 250, message = "Chiều dài tên tài khoản không được vượt quá 250 ký tự")
    String name, 
    
    @NotBlank(message = "Địa chỉ email không được để trống")
    @Size(max = 100, message = "Địa chỉ email không được để trống")
    @Email(message = "Trường email có định dạng Mail")
    String email, 
    
    @NotBlank(message = "Mật khẩu tài khoản không được để trống")
    @Size( min = 6, max = 72, message =  "Mật khẩu từ 6 đến 72 ký tự")    
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Mật khẩu phải có chữ hoa , chữ thường, và số")      
    String password,
    
    @NotEmpty(message = "Phải chọn ít nhất một vai trò")
    Set<@NotBlank String> roleCode
) {}
