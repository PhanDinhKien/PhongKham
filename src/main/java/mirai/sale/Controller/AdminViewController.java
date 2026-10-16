package mirai.sale.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller phục vụ hiển thị các màn hình Quản trị (Admin MVC Views)
 */
@Controller
public class AdminViewController {

    /**
     * Điều hướng và hiển thị màn hình trang chủ quản trị / Danh mục dịch vụ
     */
    @GetMapping({"/", "/admin", "/admin/dashboard", "/admin/services"})
    public String showDashboard() {
        return "admin/services";
    }

    /**
     * Hiển thị màn hình Quản lý tài khoản người dùng
     */
    @GetMapping({"/admin/users", "/admin/accounts"})
    public String showUsersPage() {
        return "admin/users";
    }

    /**
     * Hiển thị màn hình Quản lý vai trò & quyền
     */
    @GetMapping({"/admin/roles", "/admin/permissions"})
    public String showRolesPage() {
        return "admin/roles";
    }

    /**
     * Màn hình riêng biệt để thêm mới hoặc chỉnh sửa vai trò & phân quyền
     */
    @GetMapping({"/admin/roles/create", "/admin/roles/edit", "/admin/roles/{id}/edit"})
    public String showRoleEditPage() {
        return "admin/role-edit";
    }

    /**
     * Hiển thị màn hình Quản lý địa điểm khám
     */
    @GetMapping("/admin/locations")
    public String showLocationsPage() {
        return "admin/locations";
    }
}
