package com.meetingbooking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Модель бронирования комнаты
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Пользователь обязателен")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Комната обязательна")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Время начала обязательно")
    @Future(message = "Время начала должно быть в будущем")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "Время окончания обязательно")
    @Future(message = "Время окончания должно быть в будущем")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Min(value = 1, message = "Количество участников должно быть минимум 1")
    @Column(name = "participants_count", nullable = false)
    private Integer participantsCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Конструкторы
    public Booking() {}

    public Booking(User user, Room room, LocalDateTime startTime, LocalDateTime endTime,
                   Integer participantsCount) {
        this.user = user;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participantsCount = participantsCount;
        this.status = BookingStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = BookingStatus.COMPLETED;
    }

    /**
     * Проверяет, пересекается ли данное бронирование с другим по времени
     */
    public boolean overlapsWith(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    /**
     * Проверяет, может ли пользователь отменить бронирование
     * (например, не позднее чем за 1 час до начала)
     */
    public boolean canBeCancelled() {
        if (!isActive()) {
            return false;
        }
        return startTime.isAfter(LocalDateTime.now().plusHours(1));
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", room=" + (room != null ? room.getName() : "null") +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", participantsCount=" + participantsCount +
                ", status=" + status +
                '}';
    }
}