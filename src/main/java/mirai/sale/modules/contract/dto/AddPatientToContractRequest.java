package mirai.sale.modules.contract.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AddPatientToContractRequest(
    @NotNull(message = "Bệnh nhân không được để trống")
    UUID patientId,

    String recordCode,
    String doctorName,
    LocalDate examDate,
    String status
) {
}
