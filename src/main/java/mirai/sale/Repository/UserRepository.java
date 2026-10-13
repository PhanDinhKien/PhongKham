package mirai.sale.Repository;

import java.util.Optional;
import java.util.UUID;
import mirai.sale.Entity.User;
import mirai.sale.Repository.QueryDSL.Interface.IUserQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>, IUserQuery {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, UUID id); 
    
    boolean existsById( UUID id); 
}