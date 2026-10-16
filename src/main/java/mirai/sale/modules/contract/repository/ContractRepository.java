package mirai.sale.modules.contract.repository;

import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.contract.entity.Contract;
import mirai.sale.modules.contract.entity.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByIdAndIsDeletedFalse(UUID id);

    Optional<Contract> findByContractCodeAndIsDeletedFalse(String contractCode);

    boolean existsByContractCodeAndIsDeletedFalse(String contractCode);

    boolean existsByContractCodeAndIdNotAndIsDeletedFalse(String contractCode, UUID id);

    @Query("""
        SELECT c FROM Contract c
        WHERE c.isDeleted = false
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(c.contractCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.contractName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.companyCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR c.status = :status)
        ORDER BY c.createdAt DESC
    """)
    Page<Contract> searchContracts(
        @Param("keyword") String keyword,
        @Param("status") ContractStatus status,
        Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM contracts", nativeQuery = true)
    long countAllContracts();
}
