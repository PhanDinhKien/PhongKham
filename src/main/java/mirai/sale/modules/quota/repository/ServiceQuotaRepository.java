package mirai.sale.modules.quota.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.quota.entity.ServiceQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceQuotaRepository extends JpaRepository<ServiceQuota, UUID> {

    @Query("""
        SELECT sq FROM ServiceQuota sq
        JOIN FETCH sq.location l
        JOIN FETCH sq.service s
        LEFT JOIN FETCH sq.contract c
        WHERE sq.service.id = :serviceId AND l.isDeleted = false
        ORDER BY sq.createdAt DESC
    """)
    List<ServiceQuota> findByServiceIdWithDetails(@Param("serviceId") UUID serviceId);

    @Query("""
        SELECT COUNT(sq) FROM ServiceQuota sq
        WHERE sq.service.id = :serviceId AND sq.active = true
    """)
    long countActiveByServiceId(@Param("serviceId") UUID serviceId);

    @Query("""
        SELECT COUNT(sq) FROM ServiceQuota sq
        WHERE sq.service.id = :serviceId
    """)
    long countTotalByServiceId(@Param("serviceId") UUID serviceId);

    @Query("""
        SELECT sq FROM ServiceQuota sq
        WHERE sq.service.id = :serviceId 
          AND sq.location.id = :locationId
          AND (:contractId IS NULL AND sq.contract IS NULL OR sq.contract.id = :contractId)
    """)
    Optional<ServiceQuota> findExistingQuota(
        @Param("serviceId") UUID serviceId,
        @Param("locationId") UUID locationId,
        @Param("contractId") UUID contractId
    );

    @Query("""
        SELECT CASE WHEN COUNT(sq) > 0 THEN true ELSE false END FROM ServiceQuota sq
        WHERE sq.service.id = :serviceId 
          AND sq.location.id = :locationId
          AND (:contractId IS NULL AND sq.contract IS NULL OR sq.contract.id = :contractId)
          AND sq.id <> :excludeId
    """)
    boolean existsExistingQuotaExcludingId(
        @Param("serviceId") UUID serviceId,
        @Param("locationId") UUID locationId,
        @Param("contractId") UUID contractId,
        @Param("excludeId") UUID excludeId
    );

    Optional<ServiceQuota> findByServiceIdAndLocationId(UUID serviceId, UUID locationId);

    boolean existsByServiceIdAndLocationId(UUID serviceId, UUID locationId);

    boolean existsByServiceIdAndLocationIdAndIdNot(UUID serviceId, UUID locationId, UUID id);
}

