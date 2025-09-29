package com.meetingbooking.service;

import com.meetingbooking.dto.RoomDto;
import com.meetingbooking.entity.Room;
import com.meetingbooking.entity.RoomType;
import com.meetingbooking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления комнатами
 */
@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Получить все активные комнаты
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAllActiveRooms() {
        return roomRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить комнаты по типу (с учетом роли пользователя)
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getRoomsByType(RoomType roomType, boolean isVipUser) {
        List<Room> rooms;

        if (roomType == null) {
            rooms = roomRepository.findByIsActiveTrue();
        } else if (roomType == RoomType.VIP && !isVipUser) {
            // Обычным пользователям не показываем VIP комнаты
            return List.of();
        } else {
            rooms = roomRepository.findByRoomTypeAndIsActiveTrue(roomType);
        }

        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить комнату по ID
     */
    @Transactional(readOnly = true)
    public Optional<RoomDto> getRoomById(Long id) {
        return roomRepository.findById(id)
                .filter(Room::getIsActive)
                .map(this::convertToDto);
    }

    /**
     * Создать новую комнату
     */
    public RoomDto createRoom(RoomDto roomDto) {
        // Проверяем, существует ли комната с таким названием
        if (roomRepository.existsByName(roomDto.getName())) {
            throw new RuntimeException("Комната с таким названием уже существует");
        }

        Room room = new Room();
        room.setName(roomDto.getName());
        room.setCapacity(roomDto.getCapacity());
        room.setEquipment(roomDto.getEquipment());
        room.setRoomType(roomDto.getRoomType());
        room.setIsActive(true);

        Room savedRoom = roomRepository.save(room);
        return convertToDto(savedRoom);
    }

    /**
     * Обновить комнату
     */
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Комната не найдена"));

        // Проверяем, не изменилось ли название и существует ли комната с новым названием
        if (!room.getName().equals(roomDto.getName()) &&
            roomRepository.existsByName(roomDto.getName())) {
            throw new RuntimeException("Комната с таким названием уже существует");
        }

        room.setName(roomDto.getName());
        room.setCapacity(roomDto.getCapacity());
        room.setEquipment(roomDto.getEquipment());
        room.setRoomType(roomDto.getRoomType());

        Room updatedRoom = roomRepository.save(room);
        return convertToDto(updatedRoom);
    }

    /**
     * Удалить комнату (мягкое удаление)
     */
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Комната не найдена"));

        // Проверяем, есть ли активные бронирования
        if (!room.getBookings().isEmpty() &&
            room.getBookings().stream().anyMatch(booking -> booking.isActive())) {
            throw new RuntimeException("Нельзя удалить комнату с активными бронированиями");
        }

        room.setIsActive(false);
        roomRepository.save(room);
    }

    /**
     * Восстановить комнату
     */
    public void restoreRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Комната не найдена"));

        room.setIsActive(true);
        roomRepository.save(room);
    }

    /**
     * Получить доступные комнаты в указанный период времени
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime,
                                          RoomType roomType, Integer capacity, boolean isVipUser) {
        List<Room> availableRooms;

        if (roomType != null) {
            if (roomType == RoomType.VIP && !isVipUser) {
                return List.of();
            }
            availableRooms = roomRepository.findAvailableRoomsByType(roomType, startTime, endTime);
        } else if (capacity != null) {
            availableRooms = roomRepository.findAvailableRoomsByCapacity(capacity, startTime, endTime);
        } else {
            availableRooms = roomRepository.findAvailableRooms(startTime, endTime);
        }

        return availableRooms.stream()
                .filter(Room::getIsActive)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Поиск комнат по названию
     */
    @Transactional(readOnly = true)
    public List<RoomDto> searchRooms(String searchTerm, boolean isVipUser) {
        List<Room> rooms;

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            rooms = roomRepository.findByIsActiveTrue();
        } else {
            rooms = roomRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(searchTerm);
        }

        return rooms.stream()
                .filter(room -> isVipUser || !room.isVipRoom()) // Фильтруем VIP комнаты для обычных пользователей
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Поиск комнат по оборудованию
     */
    @Transactional(readOnly = true)
    public List<RoomDto> findRoomsByEquipment(String equipment, boolean isVipUser) {
        List<Room> rooms = roomRepository.findByEquipmentContainingAndIsActiveTrue(equipment);

        return rooms.stream()
                .filter(room -> isVipUser || !room.isVipRoom())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить статистику по комнатам
     */
    @Transactional(readOnly = true)
    public RoomStats getRoomStatistics() {
        long totalRooms = roomRepository.count();
        long activeRooms = roomRepository.findByIsActiveTrue().size();
        long regularRooms = roomRepository.countByRoomTypeAndIsActiveTrue(RoomType.REGULAR);
        long vipRooms = roomRepository.countByRoomTypeAndIsActiveTrue(RoomType.VIP);

        return new RoomStats(totalRooms, activeRooms, regularRooms, vipRooms);
    }

    /**
     * Конвертировать Room в RoomDto
     */
    private RoomDto convertToDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCapacity(room.getCapacity());
        dto.setEquipment(room.getEquipment());
        dto.setRoomType(room.getRoomType());
        dto.setIsActive(room.getIsActive());
        dto.setCreatedAt(room.getCreatedAt());
        return dto;
    }

    /**
     * Внутренний класс для статистики по комнатам
     */
    public static class RoomStats {
        private final long totalRooms;
        private final long activeRooms;
        private final long regularRooms;
        private final long vipRooms;

        public RoomStats(long totalRooms, long activeRooms, long regularRooms, long vipRooms) {
            this.totalRooms = totalRooms;
            this.activeRooms = activeRooms;
            this.regularRooms = regularRooms;
            this.vipRooms = vipRooms;
        }

        public long getTotalRooms() {
            return totalRooms;
        }

        public long getActiveRooms() {
            return activeRooms;
        }

        public long getRegularRooms() {
            return regularRooms;
        }

        public long getVipRooms() {
            return vipRooms;
        }
    }
}