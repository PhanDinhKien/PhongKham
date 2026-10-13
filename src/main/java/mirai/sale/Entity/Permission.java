package mirai.sale.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.*; 

@Entity
@Table(name = "permissions")
public class Permission {
    
    @Id
    @GeneratedValue
    @UuidGenerator()
    @Column(name = "id")
    private UUID id ; 
    
    @Column(name = "name", length = 255)
    @Nationalized
    private String name; 

    @Column(name = "code", length = 200, unique = true)
    private String code; 
    
    @Column(name = "module", length = 50)
    private String module; 
    
    @Column(name = "description", length = 255)
    @Nationalized
    private String description; 
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt ; 
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt; 
    
     public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getModule() {
        return module;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @OneToMany(mappedBy = "permission")
    private List<RolePermission> rolePermissions = new ArrayList<>();
}   
