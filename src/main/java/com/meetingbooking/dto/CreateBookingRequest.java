package com.meetingbooking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO для запроса создания бронирования
 */
public class CreateBookingRequest {

    @NotNull(message = "ID комнаты обязателен")
    private Long roomId;

    @NotNull(message = "Время начала обязательно")
    @Future(message = "Время начала должно быть в будущем")
    private LocalDateTime startTime;

    @NotNull(message = "Время окончания обязательно")
    @Future(message = "Время окончания должно быть в будущем")
    private LocalDateTime endTime;

    @Min(value = 1, message = "Количество участников должно быть минимум 1")
    private Integer participantsCount;

    // Конструкторы
    public CreateBookingRequest() {}

    public CreateBookingRequest(Long roomId, LocalDateTime startTime,
                               LocalDateTime endTime, Integer participantsCount) {
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participantsCount = participantsCount;
    }

    // Геттеры и сеттеры
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
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

    // Вспомогательные методы
    /**
     * Получить продолжительность бронирования в минутах
     */
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Проверить, что время окончания после времени начала
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && endTime.isAfter(startTime);
    }

    @Override
    public String toString() {
        return "CreateBookingRequest{" +
                "roomId=" + roomId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", participantsCount=" + participantsCount +
                '}';
    }
}