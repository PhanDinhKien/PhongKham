package mirai.sale.modules.contract.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.contract.entity.ContractPatient;

public record ContractPatientResponse(
    UUID id,
    UUID patientId,
    String patientCode,
    String patientName,
    Integer birthYear,
    String gender,
    String phone,
    String recordCode,
    String doctorName,
    LocalDate examDate,
    String status,
    String statusLabel,
    String syncStatus,
    LocalDateTime updatedAt
) {
    public static ContractPatientResponse from(ContractPatient cp) {
        String status = cp.getStatus() != null ? cp.getStatus() : "MOI";
        String statusLabel = switch (status.toUpperCase()) {
            case "CHO_DUYET" -> "Chờ duyệt";
            case "DANG_DUYET" -> "Đang duyệt";
            case "DA_DUYET" -> "Đã duyệt";
            default -> "Mới";
        };

        String syncStatus = switch (status.toUpperCase()) {
            case "DA_DUYET" -> "Đã đồng bộ";
            case "CHO_DUYET", "MOI" -> "Chưa đồng bộ";
            default -> "Chưa đồng bộ";
        };

        return new ContractPatientResponse(
            cp.getId(),
            cp.getPatient().getId(),
            cp.getPatient().getPatientCode(),
            cp.getPatient().getFullName(),
            cp.getPatient().getBirthYear(),
            cp.getPatient().getGender() != null ? cp.getPatient().getGender().name() : "",
            cp.getPatient().getPhone(),
            cp.getRecordCode() != null ? cp.getRecordCode() : "-",
            cp.getDoctorName() != null ? cp.getDoctorName() : "-",
            cp.getExamDate(),
            status,
            statusLabel,
            syncStatus,
            cp.getUpdatedAt() != null ? cp.getUpdatedAt() : cp.getCreatedAt()
        );
    }
}
