package mirai.sale.modules.role.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoleListItem(
        UUID id,
        String code,
        String name,
        String description,
        boolean active,
        long userCount,
        long permissionCount,
        List<String> permissionCodes,
        LocalDateTime createdAt
) {}
