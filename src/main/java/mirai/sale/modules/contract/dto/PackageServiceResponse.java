package mirai.sale.modules.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.contract.entity.PackageService;

public record PackageServiceResponse(
    UUID id,
    UUID serviceId,
    String code,
    String name,
    String serviceType,
    String serviceTypeLabel,
    BigDecimal price,
    String createdBy,
    LocalDateTime createdAt
) {
    public static PackageServiceResponse from(PackageService ps) {
        var ms = ps.getMedicalService();
        String typeStr = ms.getServiceType() != null ? ms.getServiceType().name() : "";
        String typeLabel = ms.getServiceType() != null ? ms.getServiceType().getLabel() : "";
        return new PackageServiceResponse(
            ps.getId(),
            ms.getId(),
            ms.getCode(),
            ms.getName(),
            typeStr,
            typeLabel,
            ms.getPrice(),
            ps.getCreatedBy(),
            ps.getCreatedAt()
        );
    }
}
