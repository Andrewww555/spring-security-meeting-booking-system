package com.meetingbooking.service;

import com.meetingbooking.dto.BookingDto;
import com.meetingbooking.dto.CreateBookingRequest;
import com.meetingbooking.entity.Booking;
import com.meetingbooking.entity.BookingStatus;
import com.meetingbooking.entity.Room;
import com.meetingbooking.entity.User;
import com.meetingbooking.repository.BookingRepository;
import com.meetingbooking.repository.RoomRepository;
import com.meetingbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления бронированиями
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                         RoomRepository roomRepository,
                         UserRepository userRepository,
                         EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Создать новое бронирование
     */
    public BookingDto createBooking(CreateBookingRequest request, String username) {
        // Получаем пользователя
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Получаем комнату
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Комната не найдена"));

        // Проверяем, активна ли комната
        if (!room.getIsActive()) {
            throw new RuntimeException("Комната не активна");
        }

        // Проверяем права доступа к VIP комнатам
        if (room.isVipRoom() && user.getRole() != com.meetingbooking.entity.Role.VIP_USER
            && user.getRole() != com.meetingbooking.entity.Role.ADMIN) {
            throw new RuntimeException("У вас нет доступа к VIP комнатам");
        }

        // Валидируем время
        if (!request.isValidTimeRange()) {
            throw new RuntimeException("Некорректный временной диапазон");
        }

        // Проверяем, что время начала в будущем
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Время начала должно быть в будущем");
        }

        // Проверяем вместимость комнаты
        if (request.getParticipantsCount() > room.getCapacity()) {
            throw new RuntimeException("Количество участников превышает вместимость комнаты");
        }

        // Проверяем доступность комнаты
        if (!roomRepository.isRoomAvailable(room, request.getStartTime(), request.getEndTime())) {
            throw new RuntimeException("Комната не доступна в указанное время");
        }

        // Проверяем, нет ли пересекающихся бронирований у пользователя
        List<Booking> userBookings = bookingRepository.findActiveBookingsForUserInTimeRange(
                user, request.getStartTime(), request.getEndTime());
        if (!userBookings.isEmpty()) {
            throw new RuntimeException("У вас уже есть бронирование в это время");
        }

        // Создаем бронирование
        Booking booking = new Booking(
                user,
                room,
                request.getStartTime(),
                request.getEndTime(),
                request.getParticipantsCount()
        );

        Booking savedBooking = bookingRepository.save(booking);

        // Отправляем email с подтверждением
        try {
            emailService.sendBookingConfirmationEmail(
                    user.getEmail(),
                    room.getName(),
                    request.getStartTime().toString(),
                    request.getEndTime().toString()
            );
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем создание бронирования
            System.err.println("Ошибка отправки email: " + e.getMessage());
        }

        return convertToDto(savedBooking);
    }

    /**
     * Получить бронирования пользователя
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return bookingRepository.findByUser(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить активные бронирования пользователя
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getActiveUserBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return bookingRepository.findByUserAndStatus(user, BookingStatus.ACTIVE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить бронирование по ID
     */
    @Transactional(readOnly = true)
    public Optional<BookingDto> getBookingById(Long id, String username) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return Optional.empty();
        }

        // Проверяем права доступа: владелец или администратор
        boolean isOwner = booking.getUser().getUsername().equals(username);
        boolean isAdmin = userRepository.findByUsername(username)
                .map(User::getRole)
                .map(role -> role == com.meetingbooking.entity.Role.ADMIN)
                .orElse(false);

        if (!isOwner && !isAdmin) {
            return Optional.empty();
        }

        return Optional.of(convertToDto(booking));
    }

    /**
     * Отменить бронирование
     */
    public BookingDto cancelBooking(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        // Проверяем права доступа: владелец или администратор
        boolean isOwner = booking.getUser().getUsername().equals(username);
        boolean isAdmin = userRepository.findByUsername(username)
                .map(User::getRole)
                .map(role -> role == com.meetingbooking.entity.Role.ADMIN)
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("У вас нет прав для отмены этого бронирования");
        }

        // Проверяем, можно ли отменить бронирование
        if (!booking.canBeCancelled()) {
            throw new RuntimeException("Бронирование можно отменить не позднее чем за 1 час до начала");
        }

        // Отменяем бронирование
        booking.cancel();
        Booking cancelledBooking = bookingRepository.save(booking);

        // Отправляем email с уведомлением об отмене
        try {
            emailService.sendBookingCancellationEmail(
                    booking.getUser().getEmail(),
                    booking.getRoom().getName(),
                    booking.getStartTime().toString(),
                    booking.getEndTime().toString()
            );
        } catch (Exception e) {
            System.err.println("Ошибка отправки email: " + e.getMessage());
        }

        return convertToDto(cancelledBooking);
    }

    /**
     * Получить все бронирования (для администратора)
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить активные бронирования (для администратора)
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getActiveBookings() {
        return bookingRepository.findByStatus(BookingStatus.ACTIVE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить бронирования на указанный период
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findByStartTimeBetween(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Завершить бронирования, которые должны быть завершены
     */
    public void completeExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findBookingsToComplete(LocalDateTime.now());

        for (Booking booking : expiredBookings) {
            booking.complete();
        }

        if (!expiredBookings.isEmpty()) {
            bookingRepository.saveAll(expiredBookings);
        }
    }

    /**
     * Получить статистику по бронированиям
     */
    @Transactional(readOnly = true)
    public BookingStats getBookingStatistics() {
        long totalBookings = bookingRepository.count();
        long activeBookings = bookingRepository.findByStatus(BookingStatus.ACTIVE).size();
        long cancelledBookings = bookingRepository.findByStatus(BookingStatus.CANCELLED).size();
        long completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED).size();

        return new BookingStats(totalBookings, activeBookings, cancelledBookings, completedBookings);
    }

    /**
     * Проверить доступность комнаты
     */
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Комната не найдена"));

        return roomRepository.isRoomAvailable(room, startTime, endTime);
    }

    /**
     * Получить пересекающиеся бронирования
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getOverlappingBookings(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Комната не найдена"));

        return bookingRepository.findActiveBookingsForRoomInTimeRange(room, startTime, endTime)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Конвертировать Booking в BookingDto
     */
    private BookingDto convertToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser().getId());
        dto.setUsername(booking.getUser().getUsername());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomName(booking.getRoom().getName());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setParticipantsCount(booking.getParticipantsCount());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setCancelledAt(booking.getCancelledAt());
        return dto;
    }

    /**
     * Внутренний класс для статистики по бронированиям
     */
    public static class BookingStats {
        private final long totalBookings;
        private final long activeBookings;
        private final long cancelledBookings;
        private final long completedBookings;

        public BookingStats(long totalBookings, long activeBookings,
                          long cancelledBookings, long completedBookings) {
            this.totalBookings = totalBookings;
            this.activeBookings = activeBookings;
            this.cancelledBookings = cancelledBookings;
            this.completedBookings = completedBookings;
        }

        public long getTotalBookings() {
            return totalBookings;
        }

        public long getActiveBookings() {
            return activeBookings;
        }

        public long getCancelledBookings() {
            return cancelledBookings;
        }

        public long getCompletedBookings() {
            return completedBookings;
        }
    }
}