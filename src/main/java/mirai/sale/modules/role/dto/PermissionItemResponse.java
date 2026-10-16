package mirai.sale.modules.role.dto;

import java.util.UUID;

public record PermissionItemResponse(
        UUID id,
        String code,
        String name,
        String module,
        String description
) {}
