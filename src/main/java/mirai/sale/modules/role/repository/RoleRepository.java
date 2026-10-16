package mirai.sale.modules.role.repository;

import mirai.sale.modules.role.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);
    
    List<Role> findByCodeIn(Collection<String> codes); 
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, UUID id);

    @Query("""
        SELECT r FROM Role r
        WHERE (:keyword IS NULL OR :keyword = '' 
               OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
               OR LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR (r.description IS NOT NULL AND LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))))
          AND (:active IS NULL OR r.active = :active)
        ORDER BY r.createdAt DESC
    """)
    Page<Role> searchRoles(@Param("keyword") String keyword, @Param("active") Boolean active, Pageable pageable);
}