package mirai.sale.modules.contract.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractStatus;

public record ContractDetailResponse(
    UUID id,
    String contractCode,
    String contractName,
    String companyCode,
    String companyName,
    ContractStatus status,
    String statusLabel,
    String createdBy,
    LocalDateTime createdAt,
    String updatedBy,
    LocalDateTime updatedAt,
    long patientCount,
    long locationCount,
    long packageCount
) {
    public static ContractDetailResponse from(Contract c, long patientCount, long locationCount, long packageCount) {
        String statusLabel = c.getStatus() != null ? c.getStatus().getLabel() : "Chưa xác định";
        return new ContractDetailResponse(
            c.getId(),
            c.getContractCode(),
            c.getContractName(),
            c.getCompanyCode(),
            c.getCompanyName(),
            c.getStatus(),
            statusLabel,
            c.getCreatedBy() != null ? c.getCreatedBy() : "-",
            c.getCreatedAt(),
            c.getUpdatedBy() != null ? c.getUpdatedBy() : "-",
            c.getUpdatedAt(),
            patientCount,
            locationCount,
            packageCount
        );
    }
}
