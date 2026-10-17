package mirai.sale.modules.quota.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.quota.entity.ServiceQuota;

public record ServiceQuotaResponse(
    UUID id,
    UUID serviceId,
    String serviceCode,
    String serviceName,
    String serviceType,
    UUID locationId,
    String locationCode,
    String locationName,
    UUID contractId,
    String contractCode,
    String contractName,
    int maxQuantity,
    String applyType,
    int shift1Quantity,
    int shift2Quantity,
    String shiftDetails,
    LocalDate validFrom,
    LocalDate validTo,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ServiceQuotaResponse fromEntity(ServiceQuota sq) {
        return new ServiceQuotaResponse(
            sq.getId(),
            sq.getService().getId(),
            sq.getService().getCode(),
            sq.getService().getName(),
            sq.getService().getServiceType() != null ? sq.getService().getServiceType().name() : "",
            sq.getLocation().getId(),
            sq.getLocation().getCode(),
            sq.getLocation().getName(),
            sq.getContract() != null ? sq.getContract().getId() : null,
            sq.getContract() != null ? sq.getContract().getContractCode() : null,
            sq.getContract() != null ? sq.getContract().getContractName() : null,
            sq.getMaxQuantity() != null ? sq.getMaxQuantity() : 0,
            sq.getApplyType() != null ? sq.getApplyType() : "THEO_CA",
            sq.getShift1Quantity() != null ? sq.getShift1Quantity() : 0,
            sq.getShift2Quantity() != null ? sq.getShift2Quantity() : 0,
            sq.getShiftDetails(),
            sq.getValidFrom(),
            sq.getValidTo(),
            sq.isActive(),
            sq.getCreatedAt(),
            sq.getUpdatedAt()
        );
    }
}
