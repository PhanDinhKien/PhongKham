/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.role.repository;

import java.util.UUID;
import mirai.sale.modules.role.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 *
 * @author macos
 */
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
}
