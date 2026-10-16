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
    ROLE_ASSIGN_PERMISSION("ROLE", "Phân quyền cho vai trò"),

    // Danh mục dịch vụ y tế
    SERVICE_VIEW("SERVICE", "Xem dịch vụ y tế"),
    SERVICE_CREATE("SERVICE", "Tạo dịch vụ y tế"),
    SERVICE_UPDATE("SERVICE", "Sửa dịch vụ y tế"),
    SERVICE_DELETE("SERVICE", "Xoá dịch vụ y tế"),

    // Địa điểm khám
    LOCATION_VIEW("LOCATION", "Xem địa điểm khám"),
    LOCATION_CREATE("LOCATION", "Tạo địa điểm khám"),
    LOCATION_UPDATE("LOCATION", "Sửa địa điểm khám"),
    LOCATION_DELETE("LOCATION", "Xoá địa điểm khám"),

    // Hồ sơ người bệnh
    PATIENT_VIEW("PATIENT", "Xem hồ sơ người bệnh"),
    PATIENT_CREATE("PATIENT", "Tạo hồ sơ người bệnh"),
    PATIENT_UPDATE("PATIENT", "Sửa hồ sơ người bệnh"),
    PATIENT_DELETE("PATIENT", "Xoá hồ sơ người bệnh"),

    // Quản lý hợp đồng
    CONTRACT_VIEW("CONTRACT", "Xem hợp đồng"),
    CONTRACT_CREATE("CONTRACT", "Tạo hợp đồng"),
    CONTRACT_UPDATE("CONTRACT", "Sửa hợp đồng"),
    CONTRACT_DELETE("CONTRACT", "Xoá hợp đồng"),

    // Chính sách giảm giá
    DISCOUNT_VIEW("DISCOUNT", "Xem chính sách giảm giá"),
    DISCOUNT_CREATE("DISCOUNT", "Tạo chính sách giảm giá"),
    DISCOUNT_UPDATE("DISCOUNT", "Sửa chính sách giảm giá"),
    DISCOUNT_DELETE("DISCOUNT", "Xoá chính sách giảm giá");
    private final String module;
    private final String label;

    PermissionCode(String module, String label) {
        this.module = module;
        this.label = label;
    }

    public String getModule() { return module; }
    public String getLabel() { return label; }
}
