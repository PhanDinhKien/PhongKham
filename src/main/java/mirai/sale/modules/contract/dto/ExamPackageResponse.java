package mirai.sale.modules.contract.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import mirai.sale.modules.contract.entity.ExamPackage;

public record ExamPackageResponse(
    UUID id,
    String packageCode,
    String packageName,
    String status,
    String statusLabel,
    String createdBy,
    LocalDateTime createdAt,
    String updatedBy,
    LocalDateTime updatedAt,
    int serviceCount,
    List<PackageServiceResponse> services
) {
    public static ExamPackageResponse from(ExamPackage p, List<PackageServiceResponse> services) {
        String status = p.getStatus() != null ? p.getStatus() : "HIEU_LUC";
        String statusLabel = "HIEU_LUC".equalsIgnoreCase(status) ? "Hiệu lực" : "Hết hiệu lực";
        return new ExamPackageResponse(
            p.getId(),
            p.getPackageCode(),
            p.getPackageName(),
            status,
            statusLabel,
            p.getCreatedBy() != null ? p.getCreatedBy() : "-",
            p.getCreatedAt(),
            p.getUpdatedBy() != null ? p.getUpdatedBy() : "-",
            p.getUpdatedAt(),
            services != null ? services.size() : 0,
            services != null ? services : List.of()
        );
    }
}
