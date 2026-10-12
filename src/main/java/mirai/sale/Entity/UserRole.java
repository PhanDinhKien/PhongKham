package mirai.sale.Entity;

import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_roles", uniqueConstraints = @UniqueConstraint(
  name = "uq_user_roles_user_role", 
  columnNames = {"user_id", "role_id"}
))
public class UserRole {
    
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id; 
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn( name = "user_id", nullable = false)
    private User user; 

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn( name = "role_id", nullable = false)
    private Role role; 
    
    public UUID getId() {
        return id;
    }

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }
    
}
