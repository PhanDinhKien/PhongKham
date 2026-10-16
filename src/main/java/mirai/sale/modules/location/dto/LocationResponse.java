package mirai.sale.modules.location.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.location.entity.Location;

public record LocationResponse(
    UUID id,
    String code,
    String name,
    String address,
    boolean active,
    String createdBy,
    String updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static LocationResponse fromEntity(Location loc) {
        return new LocationResponse(
            loc.getId(),
            loc.getCode(),
            loc.getName(),
            loc.getAddress(),
            loc.isActive(),
            loc.getCreatedBy() != null ? loc.getCreatedBy() : "-",
            loc.getUpdatedBy() != null ? loc.getUpdatedBy() : "-",
            loc.getCreatedAt(),
            loc.getUpdatedAt()
        );
    }
}
