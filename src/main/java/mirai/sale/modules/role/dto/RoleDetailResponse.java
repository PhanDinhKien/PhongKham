package mirai.sale.modules.role.dto;

import java.util.List;
import java.util.UUID;

public record RoleDetailResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean active,
        List<String> permissionCodes
) {}
