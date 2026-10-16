package mirai.sale.modules.role.service;

import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.role.dto.*;
import mirai.sale.modules.role.entity.Permission;
import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.role.entity.RolePermission;
import mirai.sale.modules.role.repository.PermissionRepository;
import mirai.sale.modules.role.repository.RolePermissionRepository;
import mirai.sale.modules.role.repository.RoleRepository;
import mirai.sale.modules.user.entity.UserRole;
import mirai.sale.modules.user.repository.UserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository,
                       RolePermissionRepository rolePermissionRepository,
                       PermissionRepository permissionRepository,
                       UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // ========================================================================
    // Truy vấn danh sách vai trò có phân trang & lọc
    // ========================================================================

    @Transactional(readOnly = true)
    public PageResponse<RoleListItem> search(String keyword, Boolean active, int page, int size) {
        Page<Role> pageResult = roleRepository.searchRoles(
                keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                active,
                PageRequest.of(page, size)
        );

        List<Role> roles = pageResult.getContent();
        if (roles.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, pageResult.getTotalElements());
        }

        // Đếm số người dùng theo vai trò
        List<UserRole> userRoles = userRoleRepository.findByRoleIn(roles);
        Map<UUID, Long> userCountMap = userRoles.stream()
                .collect(Collectors.groupingBy(ur -> ur.getRole().getId(), Collectors.counting()));

        // Tải quyền hạn theo vai trò
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIn(roles);
        Map<UUID, List<String>> permissionCodesMap = rolePermissions.stream()
                .collect(Collectors.groupingBy(
                        rp -> rp.getRole().getId(),
                        Collectors.mapping(rp -> rp.getPermission().getCode(), Collectors.toList())
                ));

        List<RoleListItem> items = roles.stream().map(r -> {
            List<String> permCodes = permissionCodesMap.getOrDefault(r.getId(), List.of());
            long userCount = userCountMap.getOrDefault(r.getId(), 0L);
            return new RoleListItem(
                    r.getId(),
                    r.getCode(),
                    r.getName(),
                    r.getDescription(),
                    r.getActive(),
                    userCount,
                    permCodes.size(),
                    permCodes,
                    r.getCreatedAt()
            );
        }).toList();

        return new PageResponse<>(items, page, size, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RoleDetailResponse getById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vai trò không tồn tại"));

        List<String> permissionCodes = rolePermissionRepository.findPermissionCodesByRole(role);
        return new RoleDetailResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getActive(),
                permissionCodes
        );
    }

    // ========================================================================
    // Tạo mới vai trò
    // ========================================================================

    @Transactional
    public RoleDetailResponse create(CreateRoleRequest req) {
        String code = req.code().trim().toUpperCase(Locale.ROOT);
        if (roleRepository.existsByCode(code)) {
            throw new BadRequestException("Mã vai trò đã tồn tại: " + code);
        }

        Role role = new Role();
        role.setCode(code);
        role.setName(req.name().trim());
        role.setDescription(req.description() != null ? req.description().trim() : null);
        role.setActive(req.active());

        role = roleRepository.save(role);

        if (req.permissionCodes() != null && !req.permissionCodes().isEmpty()) {
            assignPermissions(role, req.permissionCodes());
        }

        return getById(role.getId());
    }

    // ========================================================================
    // Cập nhật vai trò
    // ========================================================================

    @Transactional
    public RoleDetailResponse update(UUID id, UpdateRoleRequest req) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vai trò không tồn tại"));

        if ("ADMIN".equalsIgnoreCase(role.getCode()) && Boolean.FALSE.equals(req.active())) {
            throw new BadRequestException("Không thể vô hiệu hóa vai trò Quản trị viên tối cao (ADMIN)");
        }

        role.setName(req.name().trim());
        role.setDescription(req.description() != null ? req.description().trim() : null);
        role.setActive(req.active());
        role = roleRepository.save(role);

        // Đồng bộ quyền hạn
        syncPermissions(role, req.permissionCodes());

        return getById(role.getId());
    }

    // ========================================================================
    // Đổi trạng thái (Khóa / Mở khóa)
    // ========================================================================

    @Transactional
    public RoleDetailResponse toggleStatus(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vai trò không tồn tại"));

        if ("ADMIN".equalsIgnoreCase(role.getCode()) && role.getActive()) {
            throw new BadRequestException("Không thể khóa vai trò Quản trị viên tối cao (ADMIN)");
        }

        role.setActive(!role.getActive());
        roleRepository.save(role);

        return getById(role.getId());
    }

    // ========================================================================
    // Xóa vai trò
    // ========================================================================

    @Transactional
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vai trò không tồn tại"));

        if ("ADMIN".equalsIgnoreCase(role.getCode())) {
            throw new BadRequestException("Không thể xóa vai trò Quản trị viên tối cao (ADMIN)");
        }

        if (userRoleRepository.existsByRole(role)) {
            long count = userRoleRepository.countByRole(role);
            throw new BadRequestException("Không thể xóa vai trò vì đang có " + count + " tài khoản sử dụng. Vui lòng chuyển vai trò của tài khoản trước khi xóa.");
        }

        // Xóa liên kết quyền hạn
        rolePermissionRepository.deleteByRole(role);

        // Xóa vai trò
        roleRepository.delete(role);
    }

    // ========================================================================
    // Lấy danh sách toàn bộ quyền hạn trong hệ thống
    // ========================================================================

    @Transactional(readOnly = true)
    public List<PermissionItemResponse> getAllPermissions() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(p -> new PermissionItemResponse(
                        p.getId(),
                        p.getCode(),
                        p.getName(),
                        p.getModule(),
                        p.getDescription()
                ))
                .toList();
    }

    // ========================================================================
    // Helper nội bộ
    // ========================================================================

    private void assignPermissions(Role role, Collection<String> permissionCodes) {
        List<Permission> permissions = permissionRepository.findByCodeIn(permissionCodes);
        List<RolePermission> rolePermissions = permissions.stream()
                .map(p -> new RolePermission(role, p))
                .toList();
        rolePermissionRepository.saveAll(rolePermissions);
    }

    private void syncPermissions(Role role, Collection<String> newPermissionCodes) {
        List<RolePermission> currentRolePermissions = rolePermissionRepository.findByRole(role);
        Set<String> newCodes = new HashSet<>(newPermissionCodes);

        List<RolePermission> toRemove = currentRolePermissions.stream()
                .filter(rp -> !newCodes.contains(rp.getPermission().getCode()))
                .toList();

        Set<String> currentCodes = currentRolePermissions.stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toSet());

        List<String> toAddCodes = newCodes.stream()
                .filter(code -> !currentCodes.contains(code))
                .toList();

        if (!toRemove.isEmpty()) {
            rolePermissionRepository.deleteAll(toRemove);
        }

        if (!toAddCodes.isEmpty()) {
            assignPermissions(role, toAddCodes);
        }
    }
}
