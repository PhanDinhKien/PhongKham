package mirai.sale.Dto;

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