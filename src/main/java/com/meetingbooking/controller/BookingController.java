package com.meetingbooking.controller;

import com.meetingbooking.dto.BookingDto;
import com.meetingbooking.dto.CreateBookingRequest;
import com.meetingbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Контроллер для управления бронированиями
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Получить мои бронирования
     */
    @GetMapping("/my")
    public ResponseEntity<List<BookingDto>> getMyBookings(Authentication authentication) {
        String username = authentication.getName();
        List<BookingDto> bookings = bookingService.getUserBookings(username);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Получить мои активные бронирования
     */
    @GetMapping("/my/active")
    public ResponseEntity<List<BookingDto>> getMyActiveBookings(Authentication authentication) {
        String username = authentication.getName();
        List<BookingDto> bookings = bookingService.getActiveUserBookings(username);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Получить бронирование по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        return bookingService.getBookingById(id, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создать новое бронирование
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                          Authentication authentication) {
        try {
            String username = authentication.getName();
            BookingDto booking = bookingService.createBooking(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Отменить бронирование
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            BookingDto cancelledBooking = bookingService.cancelBooking(id, username);
            return ResponseEntity.ok(cancelledBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Получить все бронирования (только для администраторов)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Получить активные бронирования (только для администраторов)
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDto>> getActiveBookings() {
        List<BookingDto> bookings = bookingService.getActiveBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Получить бронирования за период времени (только для администраторов)
     */
    @GetMapping("/period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDto>> getBookingsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<BookingDto> bookings = bookingService.getBookingsInDateRange(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Проверить доступность комнаты
     */
    @GetMapping("/availability")
    public ResponseEntity<Boolean> checkRoomAvailability(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        boolean available = bookingService.isRoomAvailable(roomId, startTime, endTime);
        return ResponseEntity.ok(available);
    }

    /**
     * Получить пересекающиеся бронирования
     */
    @GetMapping("/overlapping")
    public ResponseEntity<List<BookingDto>> getOverlappingBookings(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<BookingDto> bookings = bookingService.getOverlappingBookings(roomId, startTime, endTime);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Получить статистику по бронированиям (только для администраторов)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingService.BookingStats> getBookingStatistics() {
        BookingService.BookingStats stats = bookingService.getBookingStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Класс для ответа об ошибке
     */
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}