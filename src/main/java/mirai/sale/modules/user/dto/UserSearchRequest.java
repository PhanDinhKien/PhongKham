package mirai.sale.modules.user.dto;

public record UserSearchRequest(
        String keyword,
        Boolean active,
        String roleCode,
        Integer page,
        Integer size
) {
    public UserSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}