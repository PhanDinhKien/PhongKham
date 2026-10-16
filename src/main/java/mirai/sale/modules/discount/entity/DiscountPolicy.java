package mirai.sale.modules.discount.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mirai.sale.modules.discount.constant.DiscountType;
import mirai.sale.modules.discount.constant.TierScope;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "discount_policies")
public class DiscountPolicy {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Nationalized
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "apply_type", length = 50, nullable = false)
    private String applyType = "HOP_DONG";

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 50, nullable = false)
    private DiscountType discountType = DiscountType.PERCENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_scope", length = 50, nullable = false)
    private TierScope tierScope = TierScope.MAX_TIER_ALL;

    @Column(name = "max_discount_per_service", precision = 18, scale = 2)
    private BigDecimal maxDiscountPerService;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscountPolicyContract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("tierNumber ASC")
    private List<DiscountPolicyTier> tiers = new ArrayList<>();

    public DiscountPolicy() {
    }

    public DiscountPolicy(String code, String name, DiscountType discountType, TierScope tierScope, LocalDate startDate, LocalDate endDate, boolean active, String createdBy) {
        this.code = code;
        this.name = name;
        this.applyType = "HOP_DONG";
        this.discountType = discountType != null ? discountType : DiscountType.PERCENT;
        this.tierScope = tierScope != null ? tierScope : TierScope.MAX_TIER_ALL;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.createdBy = createdBy;
        this.isDeleted = false;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public TierScope getTierScope() {
        return tierScope;
    }

    public void setTierScope(TierScope tierScope) {
        this.tierScope = tierScope;
    }

    public BigDecimal getMaxDiscountPerService() {
        return maxDiscountPerService;
    }

    public void setMaxDiscountPerService(BigDecimal maxDiscountPerService) {
        this.maxDiscountPerService = maxDiscountPerService;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<DiscountPolicyContract> getContracts() {
        return contracts;
    }

    public void setContracts(List<DiscountPolicyContract> contracts) {
        this.contracts = contracts;
    }

    public List<DiscountPolicyTier> getTiers() {
        return tiers;
    }

    public void setTiers(List<DiscountPolicyTier> tiers) {
        this.tiers = tiers;
    }
}
