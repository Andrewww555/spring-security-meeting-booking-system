package com.meetingbooking.controller;

import com.meetingbooking.dto.RoomDto;
import com.meetingbooking.entity.RoomType;
import com.meetingbooking.service.RoomService;
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
 * Контроллер для управления комнатами
 */
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Получить все доступные комнаты
     */
    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms(Authentication authentication) {
        boolean isVipUser = hasVipAccess(authentication);

        List<RoomDto> rooms = roomService.getAllActiveRooms();
        // Фильтруем VIP комнаты для обычных пользователей
        if (!isVipUser) {
            rooms = rooms.stream()
                    .filter(room -> !RoomType.VIP.equals(room.getRoomType()))
                    .toList();
        }

        return ResponseEntity.ok(rooms);
    }

    /**
     * Получить комнату по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id, Authentication authentication) {
        boolean isVipUser = hasVipAccess(authentication);

        return roomService.getRoomById(id)
                .filter(room -> isVipUser || !RoomType.VIP.equals(room.getRoomType()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Поиск комнат
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoomDto>> searchRooms(
            @RequestParam(required = false) String query,
            Authentication authentication) {

        boolean isVipUser = hasVipAccess(authentication);
        List<RoomDto> rooms = roomService.searchRooms(query, isVipUser);

        return ResponseEntity.ok(rooms);
    }

    /**
     * Получить комнаты по типу
     */
    @GetMapping("/type/{roomType}")
    public ResponseEntity<List<RoomDto>> getRoomsByType(
            @PathVariable RoomType roomType,
            Authentication authentication) {

        boolean isVipUser = hasVipAccess(authentication);
        List<RoomDto> rooms = roomService.getRoomsByType(roomType, isVipUser);

        return ResponseEntity.ok(rooms);
    }

    /**
     * Получить комнаты по оборудованию
     */
    @GetMapping("/equipment/{equipment}")
    public ResponseEntity<List<RoomDto>> getRoomsByEquipment(
            @PathVariable String equipment,
            Authentication authentication) {

        boolean isVipUser = hasVipAccess(authentication);
        List<RoomDto> rooms = roomService.findRoomsByEquipment(equipment, isVipUser);

        return ResponseEntity.ok(rooms);
    }

    /**
     * Получить доступные комнаты в указанный период
     */
    @GetMapping("/available")
    public ResponseEntity<List<RoomDto>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) Integer capacity,
            Authentication authentication) {

        boolean isVipUser = hasVipAccess(authentication);
        List<RoomDto> rooms = roomService.getAvailableRooms(startTime, endTime, roomType, capacity, isVipUser);

        return ResponseEntity.ok(rooms);
    }

    /**
     * Создать новую комнату (только для администраторов)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomDto roomDto) {
        try {
            RoomDto createdRoom = roomService.createRoom(roomDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Обновить комнату (только для администраторов)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDto roomDto) {
        try {
            RoomDto updatedRoom = roomService.updateRoom(id, roomDto);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Удалить комнату (только для администраторов)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok(new SuccessResponse("Комната успешно удалена"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Получить статистику по комнатам (только для администраторов)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomService.RoomStats> getRoomStatistics() {
        RoomService.RoomStats stats = roomService.getRoomStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Проверить, есть ли у пользователя доступ к VIP комнатам
     */
    private boolean hasVipAccess(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_VIP_USER") ||
                    authority.getAuthority().equals("ROLE_ADMIN"));
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

    /**
     * Класс для ответа об успехе
     */
    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}