package mirai.sale.TmpCheck;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TmpCheckRunner {
    @Bean
    ApplicationRunner tmpCheck(TmpCheckRepository r) {
        return a -> {
            System.out.println("CHECK perms=" + r.findPermissionsByUserEmail("admin@phongkham.local").size());
            System.out.println("CHECK has=" + r.userHasPermission("admin@phongkham.local", "X"));
            System.out.println("CHECK count=" + r.countUsersPerRole());
            System.out.println("CHECK view=" + r.findAllUserRoles());
            System.out.println("CHECK norole=" + r.findUsersWithoutRole().size());
            System.out.println("CHECK module=" + r.findRolesByPermissionModule("USER").size());
        };
    }
}
