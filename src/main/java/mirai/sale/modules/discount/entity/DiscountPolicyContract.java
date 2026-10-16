package mirai.sale.modules.discount.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.contract.entity.Contract;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
    name = "discount_policy_contracts",
    uniqueConstraints = {
        @UniqueConstraint(name = "UC_DISCOUNT_POLICY_CONTRACT", columnNames = {"policy_id", "contract_id"})
    }
)
public class DiscountPolicyContract {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private DiscountPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DiscountPolicyContract() {
    }

    public DiscountPolicyContract(DiscountPolicy policy, Contract contract, String createdBy) {
        this.policy = policy;
        this.contract = contract;
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

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
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
