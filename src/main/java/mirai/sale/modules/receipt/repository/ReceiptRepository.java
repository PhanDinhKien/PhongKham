package mirai.sale.modules.receipt.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.receipt.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {

    Optional<Receipt> findByIdAndIsDeletedFalse(UUID id);

    Optional<Receipt> findByReceiptCodeAndIsDeletedFalse(String receiptCode);

    boolean existsByReceiptCodeAndIsDeletedFalse(String receiptCode);

    @Query("SELECT r FROM Receipt r WHERE r.isDeleted = false " +
           "AND (:keyword IS NULL OR " +
           "     LOWER(r.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(r.patientCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(r.recordCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(r.patientName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:company IS NULL OR LOWER(r.companyName) LIKE LOWER(CONCAT('%', :company, '%'))) " +
           "AND (:cashier IS NULL OR LOWER(r.cashierName) LIKE LOWER(CONCAT('%', :cashier, '%'))) " +
           "AND (:paymentMethod IS NULL OR r.paymentMethod = :paymentMethod) " +
           "AND (:syncStatus IS NULL OR r.syncStatus = :syncStatus) " +
           "AND (:fromDate IS NULL OR r.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR r.createdAt <= :toDate)")
    Page<Receipt> searchReceipts(
            @Param("keyword") String keyword,
            @Param("company") String company,
            @Param("cashier") String cashier,
            @Param("paymentMethod") String paymentMethod,
            @Param("syncStatus") String syncStatus,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("SELECT DISTINCT r.companyName FROM Receipt r WHERE r.isDeleted = false AND r.companyName IS NOT NULL AND r.companyName <> ''")
    List<String> findDistinctCompanies();

    @Query("SELECT DISTINCT r.cashierName FROM Receipt r WHERE r.isDeleted = false AND r.cashierName IS NOT NULL AND r.cashierName <> ''")
    List<String> findDistinctCashiers();
}
