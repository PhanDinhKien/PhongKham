package mirai.sale.modules.discount.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.discount.constant.DiscountType;
import mirai.sale.modules.discount.entity.DiscountPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, UUID> {

    Optional<DiscountPolicy> findByIdAndIsDeletedFalse(UUID id);

    Optional<DiscountPolicy> findByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);

    @Query("SELECT DISTINCT p FROM DiscountPolicy p " +
           "LEFT JOIN p.contracts pc " +
           "LEFT JOIN pc.contract c " +
           "WHERE p.isDeleted = false " +
           "AND (:keyword IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:companyOrContract IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :companyOrContract, '%')) OR LOWER(c.contractCode) LIKE LOWER(CONCAT('%', :companyOrContract, '%')) OR LOWER(c.contractName) LIKE LOWER(CONCAT('%', :companyOrContract, '%'))) " +
           "AND (:discountType IS NULL OR p.discountType = :discountType) " +
           "AND (:active IS NULL OR p.active = :active) " +
           "AND (:fromDate IS NULL OR p.startDate >= :fromDate) " +
           "AND (:toDate IS NULL OR p.startDate <= :toDate)")
    Page<DiscountPolicy> searchPolicies(
            @Param("keyword") String keyword,
            @Param("companyOrContract") String companyOrContract,
            @Param("discountType") DiscountType discountType,
            @Param("active") Boolean active,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM DiscountPolicy p WHERE p.isDeleted = false")
    long countActivePolicies();
}
