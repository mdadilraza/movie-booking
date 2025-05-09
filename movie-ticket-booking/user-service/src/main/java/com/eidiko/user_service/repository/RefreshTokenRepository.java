package com.eidiko.user_service.repository;

import com.eidiko.user_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    @Modifying
    @Transactional
    void deleteByUser_Id( Long userId);

    RefreshToken findByUserId(Long id);
    RefreshToken findByUser_Id(Long id);
    @Modifying
    @Transactional
    void deleteByUserId(long userId);
    @Modifying
    @Transactional
    @Query("""
    delete from RefreshToken r
    where r.user.id = :userId
""")
    void deleteByUserIdByQuery(long userId);
}
