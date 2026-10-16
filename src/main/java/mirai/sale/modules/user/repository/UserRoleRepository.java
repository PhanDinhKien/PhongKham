package mirai.sale.modules.user.repository;

import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.user.entity.User;
import mirai.sale.modules.user.entity.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    boolean existsByUserAndRole(User user, Role role);

    boolean existsByRole(Role role);

    long countByRole(Role role);
    
    @Query("SELECT ur.role.code FROM UserRole as ur WHERE ur.user = :user AND ur.role.active = true")
    List<String> findRoleCodeByUser(@Param("user") User user);
    
    @EntityGraph(attributePaths = "role")
    List<UserRole> findByUser(User user); 

    @EntityGraph(attributePaths = {"role", "user"})
    List<UserRole> findByUserIn(Collection<User> users);

    @EntityGraph(attributePaths = {"role", "user"})
    List<UserRole> findByRoleIn(Collection<Role> roles);
}
