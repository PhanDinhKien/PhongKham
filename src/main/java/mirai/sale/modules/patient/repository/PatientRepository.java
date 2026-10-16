package mirai.sale.modules.patient.repository;

import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.patient.entity.Gender;
import mirai.sale.modules.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByIdAndIsDeletedFalse(UUID id);

    Optional<Patient> findByPatientCodeAndIsDeletedFalse(String patientCode);

    boolean existsByPatientCodeAndIsDeletedFalse(String patientCode);

    boolean existsByPatientCodeAndIdNotAndIsDeletedFalse(String patientCode, UUID id);

    @Query("""
        SELECT p FROM Patient p
        WHERE p.isDeleted = false
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR (p.phone IS NOT NULL AND LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
               OR (p.address IS NOT NULL AND LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))))
          AND (:gender IS NULL OR p.gender = :gender)
        ORDER BY p.createdAt DESC
    """)
    Page<Patient> searchPatients(
        @Param("keyword") String keyword,
        @Param("gender") Gender gender,
        Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM patients", nativeQuery = true)
    long countAllPatients();
}
