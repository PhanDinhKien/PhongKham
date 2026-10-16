package mirai.sale.modules.discount.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "discount_policy_tiers")
public class DiscountPolicyTier {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private DiscountPolicy policy;

    @Column(name = "tier_number", nullable = false)
    private int tierNumber;

    @Column(name = "from_quantity", nullable = false)
    private int fromQuantity;

    @Column(name = "to_quantity")
    private Integer toQuantity;

    @Column(name = "discount_value", precision = 18, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DiscountPolicyTier() {
    }

    public DiscountPolicyTier(DiscountPolicy policy, int tierNumber, int fromQuantity, Integer toQuantity, BigDecimal discountValue, String createdBy) {
        this.policy = policy;
        this.tierNumber = tierNumber;
        this.fromQuantity = fromQuantity;
        this.toQuantity = toQuantity;
        this.discountValue = discountValue;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public DiscountPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(DiscountPolicy policy) {
        this.policy = policy;
    }

    public int getTierNumber() {
        return tierNumber;
    }

    public void setTierNumber(int tierNumber) {
        this.tierNumber = tierNumber;
    }

    public int getFromQuantity() {
        return fromQuantity;
    }

    public void setFromQuantity(int fromQuantity) {
        this.fromQuantity = fromQuantity;
    }

    public Integer getToQuantity() {
        return toQuantity;
    }

    public void setToQuantity(Integer toQuantity) {
        this.toQuantity = toQuantity;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
