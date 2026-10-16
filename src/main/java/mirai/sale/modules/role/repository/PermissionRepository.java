package mirai.sale.modules.role.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import mirai.sale.modules.role.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    List<Permission> findByCodeIn(Collection<String> codes);

    List<Permission> findAllByOrderByModuleAscCodeAsc();
}
