/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.user.repository;

import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.user.entity.User;
import mirai.sale.modules.user.entity.UserRole;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author macos
 */
public interface UserRoleRepository extends JpaRepository<UserRole, UUID>  {
    boolean existsByUserAndRole(User user, Role role);
    
    @Query("SELECT ur.role.code FROM UserRole as ur WHERE ur.user = :user AND ur.role.active = true")
    List<String> findRoleCodeByUser(@Param("user") User user);
    
    @EntityGraph(attributePaths = "role")
    List<UserRole> findByUser(User user); 
}
