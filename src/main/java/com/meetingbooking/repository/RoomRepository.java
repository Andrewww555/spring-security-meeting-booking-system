package com.meetingbooking.repository;

import com.meetingbooking.entity.Room;
import com.meetingbooking.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с комнатами
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Найти все активные комнаты
     */
    List<Room> findByIsActiveTrue();

    /**
     * Найти комнаты по типу
     */
    List<Room> findByRoomType(RoomType roomType);

    /**
     * Найти активные комнаты по типу
     */
    List<Room> findByRoomTypeAndIsActiveTrue(RoomType roomType);

    /**
     * Найти комнаты по вместимости (больше или равно)
     */
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);

    /**
     * Найти активные комнаты по вместимости
     */
    List<Room> findByCapacityGreaterThanEqualAndIsActiveTrue(Integer capacity);

    /**
     * Найти комнаты по оборудованию
     */
    List<Room> findByEquipmentContaining(String equipment);

    /**
     * Найти активные комнаты по оборудованию
     */
    List<Room> findByEquipmentContainingAndIsActiveTrue(String equipment);

    /**
     * Найти комнаты по паттерну в названии
     */
    List<Room> findByNameContainingIgnoreCase(String name);

    /**
     * Найти активные комнаты по паттерну в названии
     */
    List<Room> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    /**
     * Проверить существует ли комната с таким названием
     */
    boolean existsByName(String name);

    /**
     * Подсчитать количество комнат по типу
     */
    long countByRoomType(RoomType roomType);

    /**
     * Подсчитать количество активных комнат по типу
     */
    long countByRoomTypeAndIsActiveTrue(RoomType roomType);

    /**
     * Найти комнаты, доступные в указанный период времени
     * (без активных бронирований в это время)
     */
    @Query("SELECT r FROM Room r WHERE r.isActive = true " +
           "AND r.id NOT IN (" +
           "    SELECT b.room.id FROM Booking b " +
           "    WHERE b.status = 'ACTIVE' " +
           "    AND ((b.startTime <= :endTime AND b.endTime >= :startTime))" +
           ")")
    List<Room> findAvailableRooms(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * Найти комнаты доступные в указанный период времени для определенного типа
     */
    @Query("SELECT r FROM Room r WHERE r.isActive = true " +
           "AND r.roomType = :roomType " +
           "AND r.id NOT IN (" +
           "    SELECT b.room.id FROM Booking b " +
           "    WHERE b.status = 'ACTIVE' " +
           "    AND ((b.startTime <= :endTime AND b.endTime >= :startTime))" +
           ")")
    List<Room> findAvailableRoomsByType(@Param("roomType") RoomType roomType,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * Найти комнаты доступные в указанный период времени с определенной вместимостью
     */
    @Query("SELECT r FROM Room r WHERE r.isActive = true " +
           "AND r.capacity >= :capacity " +
           "AND r.id NOT IN (" +
           "    SELECT b.room.id FROM Booking b " +
           "    WHERE b.status = 'ACTIVE' " +
           "    AND ((b.startTime <= :endTime AND b.endTime >= :startTime))" +
           ")")
    List<Room> findAvailableRoomsByCapacity(@Param("capacity") Integer capacity,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * Проверить, доступна ли конкретная комната в указанное время
     */
    @Query("SELECT COUNT(b) = 0 FROM Booking b WHERE b.room = :room " +
           "AND b.status = 'ACTIVE' " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    boolean isRoomAvailable(@Param("room") Room room,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);
}