package mirai.sale.modules.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExamPackageRequest(
    @NotBlank(message = "Mã gói khám không được để trống")
    @Size(max = 50, message = "Mã gói tối đa 50 ký tự")
    String packageCode,

    @NotBlank(message = "Tên gói khám không được để trống")
    @Size(max = 255, message = "Tên gói tối đa 255 ký tự")
    String packageName,

    String status
) {
}
