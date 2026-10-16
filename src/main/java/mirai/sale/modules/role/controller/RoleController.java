package mirai.sale.modules.role.controller;

import jakarta.validation.Valid;
import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.role.dto.*;
import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.role.repository.RoleRepository;
import mirai.sale.modules.role.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final RoleService roleService;

    public RoleController(RoleRepository roleRepository, RoleService roleService) {
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }

    /**
     * Trả về danh sách vai trò dạng gọn cho các dropdown chọn vai trò
     */
    @GetMapping
    public List<RoleSummary> getAllRoles() {
        return roleRepository.findAll().stream()
                .filter(Role::getActive)
                .map(r -> new RoleSummary(r.getId(), r.getCode(), r.getName()))
                .toList();
    }

    /**
     * Tìm kiếm và phân trang danh sách vai trò cho màn hình quản trị
     */
    @GetMapping("/search")
    public PageResponse<RoleListItem> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return roleService.search(keyword, active, page, size);
    }

    /**
     * Lấy thông tin chi tiết vai trò và danh sách quyền hạn
     */
    @GetMapping("/{id}")
    public RoleDetailResponse getById(@PathVariable UUID id) {
        return roleService.getById(id);
    }

    /**
     * Tạo mới vai trò
     */
    @PostMapping
    public RoleDetailResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return roleService.create(request);
    }

    /**
     * Cập nhật vai trò
     */
    @PutMapping("/{id}")
    public RoleDetailResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return roleService.update(id, request);
    }

    /**
     * Bật / Tắt trạng thái hoạt động của vai trò
     */
    @PatchMapping("/{id}/toggle-status")
    public RoleDetailResponse toggleStatus(@PathVariable UUID id) {
        return roleService.toggleStatus(id);
    }

    /**
     * Xóa vai trò
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa vai trò thành công"));
    }

    /**
     * Lấy toàn bộ danh mục quyền hạn có trong hệ thống
     */
    @GetMapping("/permissions")
    public List<PermissionItemResponse> getAllPermissions() {
        return roleService.getAllPermissions();
    }
}
