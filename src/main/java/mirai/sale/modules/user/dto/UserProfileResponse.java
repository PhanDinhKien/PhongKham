package mirai.sale.modules.user.dto;

import mirai.sale.modules.role.dto.RoleSummary;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        boolean active,
        List<RoleSummary> roles,
        Set<String> permissions
) {}