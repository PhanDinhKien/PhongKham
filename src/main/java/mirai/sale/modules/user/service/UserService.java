package mirai.sale.modules.user.service;

import mirai.sale.common.dto.PageResponse;
import mirai.sale.common.exception.BadRequestException;
import mirai.sale.common.exception.NotFoundException;
import mirai.sale.modules.auth.repository.RefreshTokenRepository;
import mirai.sale.modules.role.dto.RoleSummary;
import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.role.repository.RoleRepository;
import mirai.sale.modules.user.dto.CreateUserRequest;
import mirai.sale.modules.user.dto.UpdateUserRequest;
import mirai.sale.modules.user.dto.UserListItem;
import mirai.sale.modules.user.dto.UserProfileResponse;
import mirai.sale.modules.user.dto.UserSearchRequest;
import mirai.sale.modules.user.entity.User;
import mirai.sale.modules.user.entity.UserRole;
import mirai.sale.modules.user.repository.UserRepository;
import mirai.sale.modules.user.repository.UserRoleRepository;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================================================================
    // Truy vấn
    // ========================================================================

    @Transactional(readOnly = true)
    public PageResponse<UserListItem> search(UserSearchRequest req) {
        List<User> users = userRepository.search(req.keyword(), req.active(), req.roleCode(), req.page(), req.size());
        long total = userRepository.countSearch(req.keyword(), req.active(), req.roleCode());

        Map<UUID, List<RoleSummary>> rolesByUser = new HashMap<>();
        if (!users.isEmpty()) {
            List<UserRole> userRoles = userRoleRepository.findByUserIn(users);
            rolesByUser = userRoles.stream()
                    .collect(Collectors.groupingBy(
                            ur -> ur.getUser().getId(),
                            Collectors.mapping(ur -> toRoleSummary(ur.getRole()), Collectors.toList())
                    ));
        }

        Map<UUID, List<RoleSummary>> finalRolesMap = rolesByUser;
        List<UserListItem> items = users.stream()
                .map(u -> new UserListItem(
                        u.getId(),
                        u.getStaffCode(),
                        u.getName(),
                        u.getEmail(),
                        u.getPhone(),
                        u.getActive(),
                        finalRolesMap.getOrDefault(u.getId(), List.of()),
                        u.getCreatedAt()
                ))
                .toList();

        return new PageResponse<>(items, req.page(), req.size(), total);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));
        List<Role> roles = userRoleRepository.findByUser(user).stream()
                .map(UserRole::getRole)
                .toList();
        return toProfileResponse(user, roles);
    }

    // ========================================================================
    // Tạo mới
    // ========================================================================

    @Transactional
    public UserProfileResponse create(CreateUserRequest req) {
        String email = normalizeEmail(req.email());
        ensureEmailNotTaken(email);
        List<Role> roles = findRolesByCodes(req.roleCode());

        User user = buildNewUser(req.name(), email, req.password());
        user.setStaffCode(req.staffCode() != null && !req.staffCode().isBlank() ? req.staffCode().trim() : null);
        user.setPhone(req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : null);
        if (req.active() != null) {
            user.setActive(req.active());
        }

        user = userRepository.save(user);
        assignRoles(user, roles);

        return toProfileResponse(user, roles);
    }

    // ========================================================================
    // Cập nhật
    // ========================================================================

    @Transactional
    public UserProfileResponse update(UpdateUserRequest rq, UUID userId) {
        ensureEmailNotTakenByOthers(rq.email(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));

        user.setEmail(normalizeEmail(rq.email()));
        user.setName(rq.name().trim());
        user.setStaffCode(rq.staffCode() != null && !rq.staffCode().isBlank() ? rq.staffCode().trim() : null);
        user.setPhone(rq.phone() != null && !rq.phone().isBlank() ? rq.phone().trim() : null);
        user.setActive(rq.active());

        if (rq.password() != null && !rq.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(rq.password()));
        }

        List<Role> roles = findRolesByCodes(rq.roleCode());
        upInsertRoleOfUser(roles, user);

        return toProfileResponse(user, roles);
    }

    // ========================================================================
    // Xoá & Khóa
    // ========================================================================

    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));

        // 1. Xoá liên kết vai trò
        List<UserRole> currentRoles = userRoleRepository.findByUser(user);
        if (!currentRoles.isEmpty()) {
            userRoleRepository.deleteAll(currentRoles);
        }

        // 2. Xoá phiên đăng nhập (refresh tokens)
        refreshTokenRepository.deleteByUser(user);

        // 3. Xoá user
        userRepository.delete(user);
    }

    @Transactional
    public UserProfileResponse toggleStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));

        user.setActive(!user.getActive());
        List<Role> roles = userRoleRepository.findByUser(user).stream()
                .map(UserRole::getRole)
                .toList();

        return toProfileResponse(user, roles);
    }

    // ========================================================================
    // Các hàm dùng lại được (gán role, đổi role... sẽ gọi lại những hàm này)
    // ========================================================================

    private void upInsertRoleOfUser(List<Role> roles, User user) {
        List<UserRole> currentRoles = userRoleRepository.findByUser(user);
        Set<UUID> newRoleIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
        Set<UUID> currentRoleId = currentRoles.stream().map(ur -> ur.getRole().getId()).collect(Collectors.toSet());

        List<UserRole> toRemove = currentRoles.stream()
                .filter(ur -> !newRoleIds.contains(ur.getRole().getId()))
                .toList();
        List<Role> toAdd = roles.stream()
                .filter(r -> !currentRoleId.contains(r.getId()))
                .toList();

        if (!toRemove.isEmpty()) {
            userRoleRepository.deleteAll(toRemove);
        }
        if (!toAdd.isEmpty()) {
            assignRoles(user, toAdd);
        }
    }

    private void ensureEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được sử dụng");
        }
    }

    private void ensureEmailNotTakenByOthers(String email, UUID id) {
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new BadRequestException("Email đã được sử dụng");
        }
    }

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

    private User buildNewUser(String name, String email, String rawPassword) {
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFailedLoginCount(0);
        return user;
    }

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

    private RoleSummary toRoleSummary(Role role) {
        return new RoleSummary(role.getId(), role.getCode(), role.getName());
    }

    private UserProfileResponse toProfileResponse(User user, Collection<Role> roles) {
        List<RoleSummary> roleSummaries = roles.stream()
                .map(this::toRoleSummary)
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getStaffCode(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getActive(),
                roleSummaries,
                Set.of()
        );
    }
}
