package mirai.sale.modules.contract.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.contract.entity.ContractLocation;

public record ContractLocationResponse(
    UUID id,
    UUID locationId,
    String code,
    String name,
    String address,
    boolean active,
    String createdBy,
    LocalDateTime createdAt
) {
    public static ContractLocationResponse from(ContractLocation cl) {
        return new ContractLocationResponse(
            cl.getId(),
            cl.getLocation().getId(),
            cl.getLocation().getCode(),
            cl.getLocation().getName(),
            cl.getLocation().getAddress(),
            cl.getLocation().isActive(),
            cl.getCreatedBy(),
            cl.getCreatedAt()
        );
    }
}
