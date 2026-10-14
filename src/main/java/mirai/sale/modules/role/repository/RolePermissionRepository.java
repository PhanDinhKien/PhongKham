/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.role.repository;

import java.util.Set;
import java.util.UUID;
import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.role.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author macos
 */
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    
    @Query("SELECT rp.permission.id FROM RolePermission rp WHERE rp.role = :role")
    public Set<UUID> findPermissionIdByRole(@Param("role") Role role); 
}
