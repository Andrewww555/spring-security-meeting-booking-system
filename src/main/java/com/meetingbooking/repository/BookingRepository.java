package com.meetingbooking.repository;

import com.meetingbooking.entity.Booking;
import com.meetingbooking.entity.BookingStatus;
import com.meetingbooking.entity.Room;
import com.meetingbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с бронированиями
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Найти все бронирования пользователя
     */
    List<Booking> findByUser(User user);

    /**
     * Найти бронирования пользователя по статусу
     */
    List<Booking> findByUserAndStatus(User user, BookingStatus status);

    /**
     * Найти все бронирования комнаты
     */
    List<Booking> findByRoom(Room room);

    /**
     * Найти бронирования комнаты по статусу
     */
    List<Booking> findByRoomAndStatus(Room room, BookingStatus status);

    /**
     * Найти активные бронирования комнаты в указанный период
     */
    List<Booking> findByRoomAndStatusAndStartTimeBetween(
            Room room, BookingStatus status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Найти все бронирования по статусу
     */
    List<Booking> findByStatus(BookingStatus status);

    /**
     * Найти бронирования по дате создания
     */
    List<Booking> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Найти бронирования по периоду проведения
     */
    List<Booking> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Найти активные бронирования на указанное время
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findActiveBookingsInTimeRange(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * Найти активные бронирования комнаты на указанное время
     */
    @Query("SELECT b FROM Booking b WHERE b.room = :room AND b.status = 'ACTIVE' " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findActiveBookingsForRoomInTimeRange(@Param("room") Room room,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * Найти активные бронирования пользователя на указанное время
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = 'ACTIVE' " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findActiveBookingsForUserInTimeRange(@Param("user") User user,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * Найти пересекающиеся бронирования (кроме указанного)
     */
    @Query("SELECT b FROM Booking b WHERE b.id != :bookingId AND b.status = 'ACTIVE' " +
           "AND b.room = :room " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findOverlappingBookings(@Param("bookingId") Long bookingId,
                                          @Param("room") Room room,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * Подсчитать количество бронирований пользователя
     */
    long countByUser(User user);

    /**
     * Подсчитать количество активных бронирований пользователя
     */
    long countByUserAndStatus(User user, BookingStatus status);

    /**
     * Подсчитать количество бронирований комнаты
     */
    long countByRoom(Room room);

    /**
     * Найти бронирования, которые должны быть завершены (endTime в прошлом)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' AND b.endTime < :now")
    List<Booking> findBookingsToComplete(@Param("now") LocalDateTime now);

    /**
     * Найти бронирования, которые можно отменить (за 1 час до начала)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' " +
           "AND b.startTime > :oneHourFromNow")
    List<Booking> findCancellableBookings(@Param("oneHourFromNow") LocalDateTime oneHourFromNow);

    /**
     * Найти недавние бронирования пользователя (за последние N дней)
     */
    @Query("SELECT b FROM Booking b WHERE b.user = :user " +
           "AND b.createdAt >= :since " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookingsForUser(@Param("user") User user,
                                            @Param("since") LocalDateTime since);

    /**
     * Найти бронирования комнаты за указанный период
     */
    @Query("SELECT b FROM Booking b WHERE b.room = :room " +
           "AND b.startTime >= :startDate AND b.endTime <= :endDate " +
           "ORDER BY b.startTime")
    List<Booking> findBookingsForRoomInDateRange(@Param("room") Room room,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Проверить, доступна ли комната в указанное время
     */
    @Query("SELECT COUNT(b) = 0 FROM Booking b WHERE b.room = :room " +
           "AND b.status = 'ACTIVE' " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    boolean isRoomAvailable(@Param("room") Room room,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    /**
     * Найти популярные комнаты (по количеству бронирований)
     */
    @Query("SELECT b.room, COUNT(b) as bookingCount FROM Booking b " +
           "WHERE b.createdAt >= :since " +
           "GROUP BY b.room " +
           "ORDER BY bookingCount DESC")
    List<Object[]> findMostPopularRooms(@Param("since") LocalDateTime since);
}