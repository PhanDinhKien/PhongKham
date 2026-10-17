package mirai.sale.modules.quota.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.location.entity.Location;
import mirai.sale.modules.service.entity.MedicalService;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "service_quotas")
public class ServiceQuota {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MedicalService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private mirai.sale.modules.contract.entity.Contract contract;

    @Column(name = "apply_type", length = 50, nullable = false)
    private String applyType = "THEO_CA";

    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity = 0;

    @Column(name = "shift1_quantity")
    private Integer shift1Quantity = 0;

    @Column(name = "shift2_quantity")
    private Integer shift2Quantity = 0;

    @Nationalized
    @Column(name = "shift_details")
    private String shiftDetails;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "active", nullable = false)
    private boolean active = true;

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

    public ServiceQuota() {
    }

    public ServiceQuota(
        MedicalService service,
        Location location,
        String applyType,
        Integer maxQuantity,
        Integer shift1Quantity,
        Integer shift2Quantity,
        LocalDate validFrom,
        LocalDate validTo,
        boolean active,
        String author
    ) {
        this.service = service;
        this.location = location;
        this.applyType = applyType != null ? applyType : "THEO_CA";
        this.maxQuantity = maxQuantity != null ? maxQuantity : 0;
        this.shift1Quantity = shift1Quantity != null ? shift1Quantity : 0;
        this.shift2Quantity = shift2Quantity != null ? shift2Quantity : 0;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = active;
        this.createdBy = author;
        this.updatedBy = author;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MedicalService getService() {
        return service;
    }

    public void setService(MedicalService service) {
        this.service = service;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public Integer getShift1Quantity() {
        return shift1Quantity;
    }

    public void setShift1Quantity(Integer shift1Quantity) {
        this.shift1Quantity = shift1Quantity;
    }

    public Integer getShift2Quantity() {
        return shift2Quantity;
    }

    public void setShift2Quantity(Integer shift2Quantity) {
        this.shift2Quantity = shift2Quantity;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public mirai.sale.modules.contract.entity.Contract getContract() {
        return contract;
    }

    public void setContract(mirai.sale.modules.contract.entity.Contract contract) {
        this.contract = contract;
    }

    public String getShiftDetails() {
        return shiftDetails;
    }

    public void setShiftDetails(String shiftDetails) {
        this.shiftDetails = shiftDetails;
    }
}
