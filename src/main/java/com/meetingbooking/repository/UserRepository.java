package com.meetingbooking.repository;

import com.meetingbooking.entity.Role;
import com.meetingbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по username
     */
    Optional<User> findByUsername(String username);

    /**
     * Найти пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Найти пользователя по username или email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Проверить существует ли пользователь с таким username
     */
    boolean existsByUsername(String username);

    /**
     * Проверить существует ли пользователь с таким email
     */
    boolean existsByEmail(String email);

    /**
     * Найти всех пользователей по роли
     */
    List<User> findByRole(Role role);

    /**
     * Найти всех активных пользователей
     */
    List<User> findByEnabledTrue();

    /**
     * Найти всех пользователей, созданных после указанной даты
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Найти пользователей по роли и активности
     */
    List<User> findByRoleAndEnabled(Role role, Boolean enabled);

    /**
     * Найти пользователей по паттерну в username или email
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:search% OR u.email LIKE %:search%")
    List<User> findByUsernameOrEmailContaining(@Param("search") String search);

    /**
     * Подсчитать количество пользователей по роли
     */
    long countByRole(Role role);

    /**
     * Найти пользователей, которые не подтвердили email (enabled = false)
     * и были созданы более N часов назад
     */
    @Query("SELECT u FROM User u WHERE u.enabled = false AND u.createdAt < :cutoffTime")
    List<User> findUnverifiedUsersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}