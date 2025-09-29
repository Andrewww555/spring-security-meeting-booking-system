package com.meetingbooking.service;

import com.meetingbooking.dto.UserDto;
import com.meetingbooking.entity.Role;
import com.meetingbooking.entity.User;
import com.meetingbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления пользователями (только для администраторов)
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Получить всех пользователей
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить пользователей по роли
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить пользователя по ID
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * Получить пользователя по username
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    /**
     * Поиск пользователей
     */
    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }

        return userRepository.findByUsernameOrEmailContaining(searchTerm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Создать пользователя (для администратора)
     */
    public UserDto createUser(UserDto userDto) {
        // Проверяем, существует ли пользователь с таким username
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Пользователь с таким username уже существует");
        }

        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode("temp")); // Временный пароль (закодирован)
        user.setRole(userDto.getRole());
        user.setEnabled(userDto.getEnabled());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    /**
     * Обновить пользователя
     */
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем, не изменился ли username и существует ли пользователь с новым username
        if (!user.getUsername().equals(userDto.getUsername()) &&
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Пользователь с таким username уже существует");
        }

        // Проверяем, не изменился ли email и существует ли пользователь с новым email
        if (!user.getEmail().equals(userDto.getEmail()) &&
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setEnabled(userDto.getEnabled());

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * Удалить пользователя
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем, есть ли активные бронирования
        long activeBookings = user.getBookings().stream()
                .mapToLong(booking -> booking.isActive() ? 1 : 0)
                .sum();

        if (activeBookings > 0) {
            throw new RuntimeException("Нельзя удалить пользователя с активными бронированиями");
        }

        userRepository.delete(user);
    }

    /**
     * Заблокировать пользователя
     */
    public UserDto blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setEnabled(false);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * Разблокировать пользователя
     */
    public UserDto unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setEnabled(true);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * Изменить роль пользователя
     */
    public UserDto changeUserRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    /**
     * Получить статистику по пользователям
     */
    @Transactional(readOnly = true)
    public UserStats getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByEnabledTrue().size();
        long blockedUsers = totalUsers - activeUsers;
        long adminUsers = userRepository.countByRole(Role.ADMIN);
        long regularUsers = userRepository.countByRole(Role.USER);
        long vipUsers = userRepository.countByRole(Role.VIP_USER);

        return new UserStats(totalUsers, activeUsers, blockedUsers,
                           adminUsers, regularUsers, vipUsers);
    }

    /**
     * Получить пользователей, которые не подтвердили email более N часов
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUnverifiedUsersOlderThanHours(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        return userRepository.findUnverifiedUsersOlderThan(cutoffTime)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Очистить неактивных пользователей (не подтвердивших email)
     */
    public int cleanupUnverifiedUsers(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        List<User> usersToDelete = userRepository.findUnverifiedUsersOlderThan(cutoffTime);

        userRepository.deleteAll(usersToDelete);
        return usersToDelete.size();
    }

    /**
     * Конвертировать User в UserDto
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /**
     * Внутренний класс для статистики по пользователям
     */
    public static class UserStats {
        private final long totalUsers;
        private final long activeUsers;
        private final long blockedUsers;
        private final long adminUsers;
        private final long regularUsers;
        private final long vipUsers;

        public UserStats(long totalUsers, long activeUsers, long blockedUsers,
                        long adminUsers, long regularUsers, long vipUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.blockedUsers = blockedUsers;
            this.adminUsers = adminUsers;
            this.regularUsers = regularUsers;
            this.vipUsers = vipUsers;
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public long getActiveUsers() {
            return activeUsers;
        }

        public long getBlockedUsers() {
            return blockedUsers;
        }

        public long getAdminUsers() {
            return adminUsers;
        }

        public long getRegularUsers() {
            return regularUsers;
        }

        public long getVipUsers() {
            return vipUsers;
        }
    }
}