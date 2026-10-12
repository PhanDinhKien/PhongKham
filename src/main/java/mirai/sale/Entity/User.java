package mirai.sale.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name="users")
public class User {
    
    @Id
    @GeneratedValue
    @UuidGenerator()
    @Column(name = "id")
    private UUID id; 
    
    @Nationalized
    @Column(name = "name" , length = 250,  nullable = false)
    private String name; 
    
    @Column(name = "email" , length = 100, nullable = false, unique = true)
    private String email; 
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; 
    
    @Column(name = "last_login_at", nullable = true)
    private LocalDateTime lastLoginAt; 
    
    @Column(name = "locked_until", nullable = true)
    private LocalDateTime lockedUntil; 
    
    @Column(name = "failed_login_count")
    private int failedLoginCount = 0; 
      
    @CreationTimestamp
    @Column(name = "created_at", updatable  = false)
    private LocalDateTime createdAt; 
    
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(int failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }
    
    public UUID getId(){
        return this.id; 
    }
    
    public void setName(String name){
        this.name = name; 
    }
    
    public String getName(){
        return this.name; 
    }
    
    public void setEmail(String email){
        this.email = email; 
    }
    
    public String getEmail(){
        return this.email; 
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
    
    public void setPasswordHash(String passwordHash){
        this.passwordHash = passwordHash; 
    }
    
    public String getPasswordHash(){
        return this.passwordHash; 
    }
    
    public User(){
        
    }
    
    public User(String name, String email, boolean active ){
        this.name = name; 
        this.email = email; 
        this.active = active; 
    }
}
