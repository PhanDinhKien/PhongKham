package mirai.sale.modules.contract.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.contract.entity.PackageService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PackageServiceRepository extends JpaRepository<PackageService, UUID> {

    @Query("""
        SELECT ps FROM PackageService ps
        JOIN FETCH ps.medicalService ms
        WHERE ps.examPackage.id = :packageId AND ms.isDeleted = false
        ORDER BY ps.createdAt DESC
    """)
    List<PackageService> findByPackageIdWithService(@Param("packageId") UUID packageId);

    long countByExamPackageId(UUID packageId);

    boolean existsByExamPackageIdAndMedicalServiceId(UUID packageId, UUID serviceId);

    Optional<PackageService> findByExamPackageIdAndMedicalServiceId(UUID packageId, UUID serviceId);

    void deleteByExamPackageIdAndMedicalServiceId(UUID packageId, UUID serviceId);
}
