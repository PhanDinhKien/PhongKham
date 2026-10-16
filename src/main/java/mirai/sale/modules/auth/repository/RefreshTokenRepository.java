/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.auth.repository;

import mirai.sale.modules.auth.entity.RefreshToken;
import mirai.sale.modules.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author macos
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID>{
    Optional<RefreshToken> findByTokenHash(String tokenHash); 
    

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now WHERE t.user = :user AND t.revokedAt IS NULL")
    int revokeAllByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);
}
