package mirai.sale.modules.patient.dto;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import mirai.sale.modules.patient.entity.Patient;

public record PatientResponse(
    UUID id,
    String patientCode,
    String fullName,
    Integer birthYear,
    Integer age,
    String gender,
    String genderLabel,
    String phone,
    String address,
    String note,
    String createdBy,
    String updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PatientResponse fromEntity(Patient p) {
        Integer age = null;
        if (p.getBirthYear() != null && p.getBirthYear() > 1900) {
            age = Year.now().getValue() - p.getBirthYear();
        }

        String genderCode = p.getGender() != null ? p.getGender().name() : "";
        String genderLabel = p.getGender() != null ? p.getGender().getLabel() : "";

        return new PatientResponse(
            p.getId(),
            p.getPatientCode(),
            p.getFullName(),
            p.getBirthYear(),
            age,
            genderCode,
            genderLabel,
            p.getPhone() != null ? p.getPhone() : "-",
            p.getAddress() != null ? p.getAddress() : "-",
            p.getNote() != null ? p.getNote() : "",
            p.getCreatedBy() != null ? p.getCreatedBy() : "-",
            p.getUpdatedBy() != null ? p.getUpdatedBy() : "-",
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
