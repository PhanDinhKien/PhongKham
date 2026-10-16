package mirai.sale.modules.discount.repository;

import java.util.List;
import java.util.UUID;
import mirai.sale.modules.discount.entity.DiscountPolicyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountPolicyTierRepository extends JpaRepository<DiscountPolicyTier, UUID> {
    List<DiscountPolicyTier> findByPolicyIdOrderByTierNumberAsc(UUID policyId);
    void deleteByPolicyId(UUID policyId);
}
