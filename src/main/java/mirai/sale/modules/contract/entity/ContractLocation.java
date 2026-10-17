package mirai.sale.modules.contract.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import mirai.sale.modules.location.entity.Location;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "contract_locations", uniqueConstraints = {
    @UniqueConstraint(name = "UC_CONTRACT_LOCATIONS", columnNames = {"contract_id", "location_id"})
})
public class ContractLocation {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Nationalized
    @Column(name = "shift_config", length = 255)
    private String shiftConfig;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ContractLocation() {
    }

    public ContractLocation(Contract contract, Location location, String createdBy) {
        this.contract = contract;
        this.location = location;
        this.createdBy = createdBy;
        this.active = true;
    }

    public ContractLocation(Contract contract, Location location, boolean active, String shiftConfig, String createdBy) {
        this.contract = contract;
        this.location = location;
        this.active = active;
        this.shiftConfig = shiftConfig;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getShiftConfig() {
        return shiftConfig;
    }

    public void setShiftConfig(String shiftConfig) {
        this.shiftConfig = shiftConfig;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
