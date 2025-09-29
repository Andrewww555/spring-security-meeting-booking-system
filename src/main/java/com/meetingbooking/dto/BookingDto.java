package com.meetingbooking.dto;

import com.meetingbooking.entity.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO для бронирования
 */
public class BookingDto {

    private Long id;

    private Long userId;

    private String username;

    private Long roomId;

    private String roomName;

    @NotNull(message = "Время начала обязательно")
    @Future(message = "Время начала должно быть в будущем")
    private LocalDateTime startTime;

    @NotNull(message = "Время окончания обязательно")
    @Future(message = "Время окончания должно быть в будущем")
    private LocalDateTime endTime;

    @Min(value = 1, message = "Количество участников должно быть минимум 1")
    private Integer participantsCount;

    private BookingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;

    // Конструкторы
    public BookingDto() {}

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    // Вспомогательные методы
    public boolean isActive() {
        return BookingStatus.ACTIVE.equals(this.status);
    }

    public boolean isCancelled() {
        return BookingStatus.CANCELLED.equals(this.status);
    }

    public boolean isCompleted() {
        return BookingStatus.COMPLETED.equals(this.status);
    }

    /**
     * Получить продолжительность бронирования в минутах
     */
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "BookingDto{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", participantsCount=" + participantsCount +
                ", status=" + status +
                '}';
    }
}