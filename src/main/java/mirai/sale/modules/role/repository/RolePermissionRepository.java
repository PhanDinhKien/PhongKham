package mirai.sale.modules.role.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.role.entity.RolePermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    
    @Query("SELECT rp.permission.id FROM RolePermission rp WHERE rp.role = :role")
    Set<UUID> findPermissionIdByRole(@Param("role") Role role);

    @Query("SELECT rp.permission.code FROM RolePermission rp WHERE rp.role = :role")
    List<String> findPermissionCodesByRole(@Param("role") Role role);

    @EntityGraph(attributePaths = {"permission", "role"})
    List<RolePermission> findByRole(Role role);

    @EntityGraph(attributePaths = {"permission", "role"})
    List<RolePermission> findByRoleIn(Collection<Role> roles);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role = :role")
    void deleteByRole(@Param("role") Role role);
}
