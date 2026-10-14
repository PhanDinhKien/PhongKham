/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.role.entity;

import mirai.sale.modules.user.entity.UserRole;
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
@Table(name="roles")
public class Role {
    
    @Id
    @UuidGenerator
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uniqueidentifier")
    private UUID id ; 
    
    @Column(name = "code" , unique = true, nullable = false, length = 50)
    private String code; 
    
    @Nationalized
    @Column(name = "name" , nullable = false, length = 100)
    private String name; 
    
    @Nationalized
    @Column(name = "description" , length = 250)
    private String description; 
    
    @Column(nullable = false)
    private boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable  = false)
    private LocalDateTime createdAt; 
    
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
   
    public UUID getId(){
        return this.id; 
    }
    
    public void setName(String name){
        this.name = name; 
    }
    
    public String getName(){
        return this.name; 
    }
    
    public void setCode(String code){
        this.code = code; 
    }
    
    public String getCode(){
        return this.code; 
    }
    
     public void setDescription(String description){
        this.description = description; 
    }
    
    public String getDescription(){
        return this.description; 
    }
    
    
    public void setActive(boolean active){
        this.active = active; 
    }
    
    public boolean getActive(){
        return this.active; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt; 
    }
    
    public LocalDateTime getCreatedAt(){
        return this.createdAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt; 
    }
    
    public LocalDateTime getUpdatedAt(){
        return this.updatedAt; 
    }
    
    @OneToMany(mappedBy = "role")
    private List<RolePermission> rolePermissions = new ArrayList<>();

    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles = new ArrayList<>();

    public List<RolePermission> getRolePermissions() {
        return rolePermissions;
    }
}
