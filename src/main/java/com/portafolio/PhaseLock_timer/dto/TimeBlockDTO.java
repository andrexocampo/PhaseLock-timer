package com.portafolio.PhaseLock_timer.dto;

import java.time.LocalDateTime;

public class TimeBlockDTO {
    private Long id;
    private Integer totalDurationMinutes;
    private Integer pomodoroDurationMinutes;
    private Integer shortBreakDurationMinutes;
    private Integer longBreakDurationMinutes;
    private Integer numberOfPomodoros;
    private LocalDateTime createdAt;
    private BlockSequenceDTO sequence;

    public TimeBlockDTO() {
    }

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

    public BlockSequenceDTO getSequence() {
        return sequence;
    }

    public void setSequence(BlockSequenceDTO sequence) {
        this.sequence = sequence;
    }
}

