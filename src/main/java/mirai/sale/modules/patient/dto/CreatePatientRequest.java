package mirai.sale.modules.patient.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mirai.sale.modules.patient.entity.Gender;

public record CreatePatientRequest(
    @NotBlank(message = "Mã người bệnh không được để trống")
    @Size(max = 50, message = "Mã người bệnh tối đa 50 ký tự")
    String patientCode,

    @NotBlank(message = "Tên người bệnh không được để trống")
    @Size(max = 255, message = "Tên người bệnh tối đa 255 ký tự")
    String fullName,

    @Min(value = 1900, message = "Năm sinh từ năm 1900")
    @Max(value = 2100, message = "Năm sinh không hợp lệ")
    Integer birthYear,

    @NotNull(message = "Giới tính không được để trống")
    Gender gender,

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    String phone,

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    String address,

    @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
    String note
) {
    public CreatePatientRequest {
        if (patientCode != null) {
            patientCode = patientCode.trim().toUpperCase();
        }
        if (fullName != null) {
            fullName = fullName.trim();
        }
        if (phone != null) {
            phone = phone.trim();
        }
        if (address != null) {
            address = address.trim();
        }
        if (note != null) {
            note = note.trim();
        }
    }
}
