package mirai.sale.modules.contract.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.contract.entity.ExamPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamPackageRepository extends JpaRepository<ExamPackage, UUID> {

    List<ExamPackage> findByContractIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID contractId);

    Optional<ExamPackage> findByIdAndIsDeletedFalse(UUID id);

    long countByContractIdAndIsDeletedFalse(UUID contractId);

    boolean existsByContractIdAndPackageCodeAndIsDeletedFalse(UUID contractId, String packageCode);

    boolean existsByContractIdAndPackageCodeAndIdNotAndIsDeletedFalse(UUID contractId, String packageCode, UUID id);

    @Query(value = "SELECT COUNT(*) FROM exam_packages WHERE contract_id = :contractId", nativeQuery = true)
    long countAllByContractId(@Param("contractId") UUID contractId);
}
