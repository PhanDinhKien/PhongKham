package mirai.sale.Service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import mirai.sale.Dto.*;
import mirai.sale.Dto.user.CreateUserRequest;
import mirai.sale.Entity.Role;
import mirai.sale.Entity.User;
import mirai.sale.Entity.UserRole;
import mirai.sale.Exception.BadRequestException;
import mirai.sale.Repository.RoleRepository;
import mirai.sale.Repository.UserRepository;
import mirai.sale.Repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================================================================
    // Truy vấn
    // ========================================================================

    @Transactional(readOnly = true)
    public PageResponse<UserListItem> search(UserSearchRequest req) {
        List<UserListItem> items = userRepository
                .search(req.keyword(), req.active(), req.roleCode(), req.page(), req.size())
                .stream()
                .map(this::toListItem)
                .toList();

        // TODO: thay 100 bằng count() thật khi IUserQuery có hàm đếm theo cùng bộ lọc
        return new PageResponse<>(items, req.page(), req.size(), 100);
    }

    // ========================================================================
    // Tạo mới
    // ========================================================================

    @Transactional
    public UserProfileResponse create(CreateUserRequest req) {
        String email = normalizeEmail(req.email());

        ensureEmailNotTaken(email);
        List<Role> roles = findRolesByCodes(req.roleCode());

        User user = userRepository.save(buildNewUser(req.name(), email, req.password()));
        assignRoles(user, roles);

        return toProfileResponse(user, roles);
    }

    // ========================================================================
    // Các hàm dùng lại được (gán role, đổi role... sẽ gọi lại những hàm này)
    // ========================================================================

    /** Báo lỗi nếu email đã có tài khoản dùng. Email phải được chuẩn hóa trước. */
    private void ensureEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được sử dụng");
        }
    }

    /** Tìm role theo danh sách mã. Báo lỗi và liệt kê các mã không tồn tại. */
    private List<Role> findRolesByCodes(Collection<String> codes) {
        List<Role> roles = roleRepository.findByCodeIn(codes);

        Set<String> foundCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        List<String> missing = codes.stream()
                .filter(code -> !foundCodes.contains(code))
                .toList();

        if (!missing.isEmpty()) {
            throw new BadRequestException("Vai trò không tồn tại: " + String.join(", ", missing));
        }
        return roles;
    }

    /** Tạo Entity User mới (chưa lưu), mật khẩu đã được băm. */
    private User buildNewUser(String name, String email, String rawPassword) {
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFailedLoginCount(0);
        return user;
    }

    /** Gán danh sách role cho user. User phải được lưu trước (đã có id). */
    private void assignRoles(User user, Collection<Role> roles) {
        List<UserRole> userRoles = roles.stream()
                .map(role -> new UserRole(user, role))
                .toList();
        userRoleRepository.saveAll(userRoles);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // ========================================================================
    // Chuyển Entity → DTO
    // ========================================================================

    private UserListItem toListItem(User user) {
        return new UserListItem(user.getId(), user.getName(), user.getEmail(),
                                user.getActive(), user.getCreatedAt());
    }

    private RoleSummary toRoleSummary(Role role) {
        return new RoleSummary(role.getId(), role.getCode(), role.getName());
    }

    private UserProfileResponse toProfileResponse(User user, Collection<Role> roles) {
        List<RoleSummary> roleSummaries = roles.stream()
                .map(this::toRoleSummary)
                .toList();

        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(),
                                       user.getActive(), roleSummaries, Set.of());
    }
}
