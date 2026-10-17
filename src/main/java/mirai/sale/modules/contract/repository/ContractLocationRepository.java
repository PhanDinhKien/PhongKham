package mirai.sale.modules.contract.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mirai.sale.modules.contract.entity.ContractLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractLocationRepository extends JpaRepository<ContractLocation, UUID> {

    @Query("""
        SELECT cl FROM ContractLocation cl
        JOIN FETCH cl.location l
        WHERE cl.contract.id = :contractId AND l.isDeleted = false
        ORDER BY cl.createdAt DESC
    """)
    List<ContractLocation> findByContractIdWithLocation(@Param("contractId") UUID contractId);

    long countByContractId(UUID contractId);

    boolean existsByContractIdAndLocationId(UUID contractId, UUID locationId);

    Optional<ContractLocation> findByContractIdAndLocationId(UUID contractId, UUID locationId);

    void deleteByContractIdAndLocationId(UUID contractId, UUID locationId);

    @Query("""
        SELECT cl FROM ContractLocation cl
        JOIN FETCH cl.contract c
        WHERE cl.location.id = :locationId AND c.isDeleted = false
        ORDER BY cl.createdAt DESC
    """)
    List<ContractLocation> findByLocationIdWithContract(@Param("locationId") UUID locationId);

    long countByLocationId(UUID locationId);
}
