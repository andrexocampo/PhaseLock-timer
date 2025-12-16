package com.portafolio.PhaseLock_timer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_blocks")
public class TimeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(value = 1, message = "Total duration must be at least 1 minute")
    @Column(nullable = false)
    private Integer totalDurationMinutes; // Duración total del bloque en minutos

    @NotNull
    @Min(value = 1, message = "Pomodoro duration must be at least 1 minute")
    @Column(nullable = false)
    private Integer pomodoroDurationMinutes; // Duración de cada pomodoro

    @NotNull
    @Min(value = 0, message = "Short break duration cannot be negative")
    @Column(nullable = false)
    private Integer shortBreakDurationMinutes; // Duración de los descansos cortos

    @NotNull
    @Min(value = 0, message = "Long break duration cannot be negative")
    @Column(nullable = false)
    private Integer longBreakDurationMinutes; // Duración del descanso largo final

    @Column(nullable = false)
    private Integer numberOfPomodoros; // Número calculado de pomodoros

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public TimeBlock() {
    }

    public TimeBlock(Integer totalDurationMinutes, Integer pomodoroDurationMinutes, 
                    Integer shortBreakDurationMinutes, Integer longBreakDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
        this.pomodoroDurationMinutes = pomodoroDurationMinutes;
        this.shortBreakDurationMinutes = shortBreakDurationMinutes;
        this.longBreakDurationMinutes = longBreakDurationMinutes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public Integer getPomodoroDurationMinutes() {
        return pomodoroDurationMinutes;
    }

    public void setPomodoroDurationMinutes(Integer pomodoroDurationMinutes) {
        this.pomodoroDurationMinutes = pomodoroDurationMinutes;
    }

    public Integer getShortBreakDurationMinutes() {
        return shortBreakDurationMinutes;
    }

    public void setShortBreakDurationMinutes(Integer shortBreakDurationMinutes) {
        this.shortBreakDurationMinutes = shortBreakDurationMinutes;
    }

    public Integer getLongBreakDurationMinutes() {
        return longBreakDurationMinutes;
    }

    public void setLongBreakDurationMinutes(Integer longBreakDurationMinutes) {
        this.longBreakDurationMinutes = longBreakDurationMinutes;
    }

    public Integer getNumberOfPomodoros() {
        return numberOfPomodoros;
    }

    public void setNumberOfPomodoros(Integer numberOfPomodoros) {
        this.numberOfPomodoros = numberOfPomodoros;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

