package mirai.sale.modules.user.repository;

import mirai.sale.modules.user.entity.User;
import mirai.sale.modules.user.repository.query.IUserQuery;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>, IUserQuery {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, UUID id); 
    
    boolean existsById( UUID id); 
}