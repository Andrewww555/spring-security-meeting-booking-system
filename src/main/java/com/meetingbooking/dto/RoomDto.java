package com.meetingbooking.dto;

import com.meetingbooking.entity.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для комнаты
 */
public class RoomDto {

    private Long id;

    @NotBlank(message = "Название комнаты обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    private String name;

    @Min(value = 1, message = "Вместимость должна быть минимум 1 человек")
    private Integer capacity;

    private List<String> equipment;

    private RoomType roomType;

    private Boolean isActive;

    private LocalDateTime createdAt;

    // Конструкторы
    public RoomDto() {}

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Вспомогательные методы
    public boolean isVipRoom() {
        return RoomType.VIP.equals(this.roomType);
    }

    public boolean isRegularRoom() {
        return RoomType.REGULAR.equals(this.roomType);
    }

    @Override
    public String toString() {
        return "RoomDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", roomType=" + roomType +
                ", isActive=" + isActive +
                ", equipment=" + equipment +
                '}';
    }
}