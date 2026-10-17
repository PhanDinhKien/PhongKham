package mirai.sale.modules.location.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mirai.sale.modules.location.entity.Location;

public record LocationResponse(
    UUID id,
    String code,
    String name,
    String address,
    boolean active,
    List<ShiftDto> shifts,
    long contractCount,
    String createdBy,
    String updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static LocationResponse fromEntity(Location loc) {
        return fromEntity(loc, 0);
    }

    public static LocationResponse fromEntity(Location loc, long contractCount) {
        List<ShiftDto> shiftDtos = loc.getShifts() != null
            ? loc.getShifts().stream().map(ShiftDto::fromEntity).toList()
            : Collections.emptyList();

        return new LocationResponse(
            loc.getId(),
            loc.getCode(),
            loc.getName(),
            loc.getAddress() != null ? loc.getAddress() : "",
            loc.isActive(),
            shiftDtos,
            contractCount,
            loc.getCreatedBy() != null ? loc.getCreatedBy() : "-",
            loc.getUpdatedBy() != null ? loc.getUpdatedBy() : "-",
            loc.getCreatedAt(),
            loc.getUpdatedAt()
        );
    }
}
