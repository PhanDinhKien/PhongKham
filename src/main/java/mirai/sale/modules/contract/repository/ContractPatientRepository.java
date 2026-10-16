package mirai.sale.modules.contract.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.contract.entity.ContractPatient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractPatientRepository extends JpaRepository<ContractPatient, UUID> {

    @Query("""
        SELECT cp FROM ContractPatient cp
        JOIN FETCH cp.patient p
        WHERE cp.contract.id = :contractId
        ORDER BY cp.createdAt DESC
    """)
    List<ContractPatient> findByContractIdWithPatient(@Param("contractId") UUID contractId);

    @Query("""
        SELECT cp FROM ContractPatient cp
        JOIN FETCH cp.patient p
        WHERE cp.contract.id = :contractId
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(cp.recordCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(cp.doctorName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR :status = '' OR cp.status = :status)
        ORDER BY cp.createdAt DESC
    """)
    Page<ContractPatient> searchContractPatients(
        @Param("contractId") UUID contractId,
        @Param("keyword") String keyword,
        @Param("status") String status,
        Pageable pageable
    );

    long countByContractId(UUID contractId);

    boolean existsByContractIdAndPatientId(UUID contractId, UUID patientId);

    Optional<ContractPatient> findByContractIdAndPatientId(UUID contractId, UUID patientId);

    void deleteByContractIdAndPatientId(UUID contractId, UUID patientId);
}
