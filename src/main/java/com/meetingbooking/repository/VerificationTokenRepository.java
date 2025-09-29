package com.meetingbooking.repository;

import com.meetingbooking.entity.User;
import com.meetingbooking.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с токенами верификации email
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Найти токен по значению
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Найти все токены пользователя
     */
    List<VerificationToken> findByUser(User user);

    /**
     * Найти активный (неиспользованный и неистекший) токен пользователя
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.user = :user " +
           "AND vt.usedAt IS NULL AND vt.expiresAt > :now")
    Optional<VerificationToken> findActiveTokenByUser(@Param("user") User user,
                                                      @Param("now") LocalDateTime now);

    /**
     * Найти все неиспользованные токены
     */
    List<VerificationToken> findByUsedAtIsNull();

    /**
     * Найти все истекшие токены
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.expiresAt < :now")
    List<VerificationToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Найти все истекшие и неиспользованные токены
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.expiresAt < :now AND vt.usedAt IS NULL")
    List<VerificationToken> findExpiredAndUnusedTokens(@Param("now") LocalDateTime now);

    /**
     * Проверить существует ли активный токен для пользователя
     */
    @Query("SELECT COUNT(vt) > 0 FROM VerificationToken vt WHERE vt.user = :user " +
           "AND vt.usedAt IS NULL AND vt.expiresAt > :now")
    boolean hasActiveToken(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Удалить все истекшие токены пользователя
     */
    void deleteByUserAndExpiresAtBefore(User user, LocalDateTime expiryDate);

    /**
     * Удалить все токены пользователя
     */
    void deleteByUser(User user);

    /**
     * Найти токены, созданные в указанный период
     */
    List<VerificationToken> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Подсчитать количество токенов пользователя
     */
    long countByUser(User user);

    /**
     * Найти пользователей с истекшими токенами верификации
     */
    @Query("SELECT DISTINCT vt.user FROM VerificationToken vt WHERE vt.usedAt IS NULL " +
           "AND vt.expiresAt < :now")
    List<User> findUsersWithExpiredTokens(@Param("now") LocalDateTime now);
}