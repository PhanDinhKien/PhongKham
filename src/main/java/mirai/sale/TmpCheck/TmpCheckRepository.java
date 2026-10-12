package mirai.sale.TmpCheck;

import java.util.List;
import java.util.UUID;
import mirai.sale.Dto.RoleUserCount;
import mirai.sale.Dto.UserRoleView;
import mirai.sale.Entity.Permission;
import mirai.sale.Entity.Role;
import mirai.sale.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TmpCheckRepository extends JpaRepository<Permission, UUID> {

    @Query("""
        SELECT DISTINCT p FROM UserRole ur
        JOIN ur.role r
        JOIN RolePermission rp ON rp.role = r
        JOIN rp.permission p
        WHERE ur.user.email = :email AND r.active = true
        """)
    List<Permission> findPermissionsByUserEmail(@Param("email") String email);

    @Query("""
        SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END
        FROM UserRole ur
        JOIN RolePermission rp ON rp.role = ur.role
        WHERE ur.user.email = :email AND rp.permission.code = :code
        """)
    boolean userHasPermission(@Param("email") String email, @Param("code") String code);

    @Query("""
        SELECT new mirai.sale.Dto.RoleUserCount(r.code, r.name, COUNT(ur.id))
        FROM Role r
        LEFT JOIN UserRole ur ON ur.role = r
        GROUP BY r.code, r.name
        ORDER BY COUNT(ur.id) DESC
        """)
    List<RoleUserCount> countUsersPerRole();

    @Query("""
        SELECT new mirai.sale.Dto.UserRoleView(u.email, u.name, r.code, r.name)
        FROM UserRole ur
        JOIN ur.user u
        JOIN ur.role r
        WHERE u.active = true
        ORDER BY u.email
        """)
    List<UserRoleView> findAllUserRoles();

    @Query("""
        SELECT u FROM User u
        WHERE NOT EXISTS (SELECT 1 FROM UserRole ur WHERE ur.user = u)
        """)
    List<User> findUsersWithoutRole();

    @Query("""
        SELECT DISTINCT r FROM Role r
        JOIN RolePermission rp ON rp.role = r
        WHERE rp.permission.module = :module
        """)
    List<Role> findRolesByPermissionModule(@Param("module") String module);
}
