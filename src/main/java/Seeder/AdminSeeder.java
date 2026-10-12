/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Seeder;

import org.slf4j.Logger;
import mirai.sale.Entity.Role;
import mirai.sale.Entity.User;
import mirai.sale.Entity.UserRole;
import mirai.sale.Repository.RoleRepository;
import mirai.sale.Repository.UserRepository;
import mirai.sale.Repository.UserRoleRepository;
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
    
    public AdminSeeder(RoleRepository roleRepository, UserRepository userRepository , UserRoleRepository userRoleRepository){
       this.roleRepository = roleRepository; 
       this.userRepository = userRepository; 
       this.userRoleRepository = userRoleRepository; 
    }
    
    @Value("${app.seed.admin.name}")
    private String adminName;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;
    

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Role roleAdmin = roleRepository.findByCode(ADMIN_ROLE_CODE).orElseGet(this:: createAdminRole); 
        User admin = userRepository.findByEmail(adminEmail).orElseGet(this::createAdminUser);
        if(admin != null){
           if(!userRoleRepository.existsByUserAndRole(admin, roleAdmin)){
              userRoleRepository.save(new UserRole(admin, roleAdmin)); 
              log.info("Đã gán role {} cho {}", ADMIN_ROLE_CODE, adminEmail);
           }
        }
    }
    
    private Role createAdminRole(){
       Role role = new Role(); 
       role.setCode(ADMIN_ROLE_CODE);
       role.setName("Quản trị viên");
       role.setDescription("Toàn quyền quản trị hệ thống");
       role.setActive(true);
       return roleRepository.save(role);
    }
    
    private User createAdminUser() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("Chưa đặt ADMIN_PASSWORD nên bỏ qua việc tạo tài khoản admin {}", adminEmail);
            return null;
        }
        User user = new User(adminName, adminEmail, true);
        user.setPasswordHash(passwordEncoder.encode(adminPassword));
        log.info("Đã tạo tài khoản admin {}", adminEmail);
        return userRepository.save(user);
    } 
}
