package mirai.sale.modules.location.repository;

import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.location.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByIdAndIsDeletedFalse(UUID id);

    Optional<Location> findByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIdNotAndIsDeletedFalse(String code, UUID id);

    @Query("""
        SELECT l FROM Location l
        WHERE l.isDeleted = false
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(l.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(l.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:active IS NULL OR l.active = :active)
        ORDER BY l.createdAt DESC
    """)
    Page<Location> searchLocations(
        @Param("keyword") String keyword,
        @Param("active") Boolean active,
        Pageable pageable
    );
}
