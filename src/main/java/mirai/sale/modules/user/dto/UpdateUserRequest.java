/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 *
 * @author phankien
 */
public record UpdateUserRequest(
        @NotBlank(message = "Tên không được để trống")
        @Size(max = 250, message = "Tên tối đa 250 ký tự")
        String name,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email tối đa 100 ký tự")
        String email,

        @NotNull(message = "Phải chọn trạng thái")
        Boolean active,

        @NotEmpty(message = "Phải chọn ít nhất một vai trò")
        Set<@NotBlank String> roleCode
) {}
