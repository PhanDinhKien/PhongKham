package mirai.sale.modules.service.repository;

import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.service.entity.MedicalService;
import mirai.sale.modules.service.entity.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, UUID> {

    Optional<MedicalService> findByIdAndIsDeletedFalse(UUID id);

    Optional<MedicalService> findByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIdNotAndIsDeletedFalse(String code, UUID id);

    @Query("""
        SELECT s FROM MedicalService s
        WHERE s.isDeleted = false
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR (s.description IS NOT NULL AND LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))))
          AND (:serviceType IS NULL OR s.serviceType = :serviceType)
          AND (:active IS NULL OR s.active = :active)
        ORDER BY s.createdAt DESC
    """)
    Page<MedicalService> searchServices(
        @Param("keyword") String keyword,
        @Param("serviceType") ServiceType serviceType,
        @Param("active") Boolean active,
        Pageable pageable
    );
}
