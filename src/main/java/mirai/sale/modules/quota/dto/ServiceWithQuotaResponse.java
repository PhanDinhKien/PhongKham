package mirai.sale.modules.quota.dto;

import java.math.BigDecimal;
import java.util.UUID;
import mirai.sale.modules.service.entity.MedicalService;

public record ServiceWithQuotaResponse(
    UUID id,
    String code,
    String name,
    String serviceType,
    String serviceTypeLabel,
    BigDecimal price,
    boolean active,
    long activeQuotaCount,
    long totalQuotaCount
) {
    public static ServiceWithQuotaResponse from(MedicalService s, long activeQuotaCount, long totalQuotaCount) {
        String label = s.getServiceType() != null ? s.getServiceType().getLabel() : "Khác";
        String typeCode = s.getServiceType() != null ? s.getServiceType().name() : "";
        if ("CHAN_DOAN_HINH_ANH".equals(typeCode)) {
            label = "CDHA-TDCN";
        }

        return new ServiceWithQuotaResponse(
            s.getId(),
            s.getCode(),
            s.getName(),
            typeCode,
            label,
            s.getPrice() != null ? s.getPrice() : BigDecimal.ZERO,
            s.isActive(),
            activeQuotaCount,
            totalQuotaCount
        );
    }
}
