package mirai.sale.seeder;

import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.role.repository.RoleRepository;
import mirai.sale.modules.user.entity.User;
import mirai.sale.modules.user.entity.UserRole;
import mirai.sale.modules.user.repository.UserRepository;
import mirai.sale.modules.user.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true) 
public class AdminSeeder implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public AdminSeeder(RoleRepository roleRepository, UserRepository userRepository, UserRoleRepository userRoleRepository) {
       this.roleRepository = roleRepository; 
       this.userRepository = userRepository; 
       this.userRoleRepository = userRoleRepository; 
    }
    
    @Value("${app.seed.admin.name:Administrator}")
    private String adminName;

    @Value("${app.seed.admin.email:admin@phongkham.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin@123}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. Seed các role cơ bản trong hệ thống phòng khám
        seedRole(ADMIN_ROLE_CODE, "Quản trị viên", "Toàn quyền quản trị hệ thống");
        seedRole("BAC_SI_KET_LUAN", "Bác sĩ kết luận", "Bác sĩ phụ trách kết luận khám");
        seedRole("NV_HO_SO", "Nhân viên hồ sơ", "Nhân viên quản lý hồ sơ khám bệnh");
        seedRole("KY_THUAT_VIEN", "Kỹ thuật viên", "Kỹ thuật viên thực hiện xét nghiệm/chẩn đoán");
        seedRole("BAC_SI", "Bác Sĩ", "Bác sĩ khám chuyên khoa");

        // 2. Seed Admin user mặc định
        Role roleAdmin = roleRepository.findByCode(ADMIN_ROLE_CODE).orElse(null); 
        User admin = userRepository.findByEmail(adminEmail).orElseGet(this::createAdminUser);
        if (admin != null) {
            if (admin.getStaffCode() == null || admin.getStaffCode().isBlank()) {
                admin.setStaffCode("NV-0001");
                userRepository.save(admin);
            }
            if (roleAdmin != null && !userRoleRepository.existsByUserAndRole(admin, roleAdmin)) {
                userRoleRepository.save(new UserRole(admin, roleAdmin)); 
                log.info("Đã gán role {} cho {}", ADMIN_ROLE_CODE, adminEmail);
            }
        }

        // 3. Seed dữ liệu mẫu cho danh sách tài khoản theo giao diện thiết kế
        seedSampleUser("NV-0001", "Nguyễn Văn A", "nva@clinicm.com", "0900000001", "BAC_SI_KET_LUAN", true);
        seedSampleUser("NV-0002", "Trần Thị B", "ttb@clinicm.com", "0900000002", "NV_HO_SO", true);
        seedSampleUser("NV-0003", "Lê Văn C", "lvc@clinicm.com", "0900000003", "KY_THUAT_VIEN", true);
        seedSampleUser("NV-0004", "Phạm Văn D", "pvd@clinicm.com", "0900000004", "BAC_SI_KET_LUAN", true);
        seedSampleUser("NV-0005", "Hoàng Thị E", "hte@clinicm.com", "0900000005", "BAC_SI_KET_LUAN", false);
        seedSampleUser("NV-0006", "Đỗ Minh F", "dmf@clinicm.com", "0900000006", "ADMIN", true);
    }
    
    private Role seedRole(String code, String name, String desc) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(desc);
            role.setActive(true);
            return roleRepository.save(role);
        });
    }

    private void seedSampleUser(String staffCode, String name, String email, String phone, String roleCode, boolean active) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User(name, email, active);
            user.setStaffCode(staffCode);
            user.setPhone(phone);
            user.setPasswordHash(passwordEncoder.encode("Password123"));
            user = userRepository.save(user);

            Role role = roleRepository.findByCode(roleCode).orElse(null);
            if (role != null) {
                userRoleRepository.save(new UserRole(user, role));
            }
        }
    }
    
    private User createAdminUser() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("Chưa đặt ADMIN_PASSWORD nên bỏ qua việc tạo tài khoản admin {}", adminEmail);
            return null;
        }
        User user = new User(adminName, adminEmail, true);
        user.setStaffCode("NV-0001");
        user.setPhone("0900000000");
        user.setPasswordHash(passwordEncoder.encode(adminPassword));
        log.info("Đã tạo tài khoản admin {}", adminEmail);
        return userRepository.save(user);
    } 
}
