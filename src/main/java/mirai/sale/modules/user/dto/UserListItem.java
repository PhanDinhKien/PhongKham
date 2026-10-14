package mirai.sale.modules.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserListItem(UUID id, String name, String email, boolean active, LocalDateTime createdAt) {}