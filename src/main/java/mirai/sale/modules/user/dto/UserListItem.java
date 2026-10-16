package mirai.sale.modules.user.dto;

import mirai.sale.modules.role.dto.RoleSummary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserListItem(
        UUID id,
        String staffCode,
        String name,
        String email,
        String phone,
        boolean active,
        List<RoleSummary> roles,
        LocalDateTime createdAt
) {}