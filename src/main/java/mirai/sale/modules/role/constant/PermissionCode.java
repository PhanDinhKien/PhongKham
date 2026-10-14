package mirai.sale.modules.role.constant;

public enum PermissionCode {

    // Người dùng
    USER_VIEW("USER", "Xem người dùng"),
    USER_CREATE("USER", "Tạo người dùng"),
    USER_UPDATE("USER", "Sửa người dùng"),
    USER_DELETE("USER", "Xoá người dùng"),

    // Vai trò & phân quyền
    ROLE_VIEW("ROLE", "Xem vai trò"),
    ROLE_CREATE("ROLE", "Tạo vai trò"),
    ROLE_UPDATE("ROLE", "Sửa vai trò"),
    ROLE_DELETE("ROLE", "Xoá vai trò"),
    ROLE_ASSIGN_PERMISSION("ROLE", "Phân quyền cho vai trò");
    
    
    
    private final String module;
    private final String label;

    PermissionCode(String module, String label) {
        this.module = module;
        this.label = label;
    }

    public String getModule() { return module; }
    public String getLabel() { return label; }
}
