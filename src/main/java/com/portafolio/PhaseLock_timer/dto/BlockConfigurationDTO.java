package com.portafolio.PhaseLock_timer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BlockConfigurationDTO {

    @NotNull(message = "Total duration is required")
    @Min(value = 1, message = "Total duration must be at least 1 minute")
    private Integer totalDurationMinutes;

    @NotNull(message = "Pomodoro duration is required")
    @Min(value = 1, message = "Pomodoro duration must be at least 1 minute")
    private Integer pomodoroDurationMinutes;

    @NotNull(message = "Short break duration is required")
    @Min(value = 0, message = "Short break duration cannot be negative")
    private Integer shortBreakDurationMinutes;

    @NotNull(message = "Long break duration is required")
    @Min(value = 0, message = "Long break duration cannot be negative")
    private Integer longBreakDurationMinutes;

    // Constructors
    public BlockConfigurationDTO() {
    }

    public BlockConfigurationDTO(Integer totalDurationMinutes, Integer pomodoroDurationMinutes,
                                Integer shortBreakDurationMinutes, Integer longBreakDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
        this.pomodoroDurationMinutes = pomodoroDurationMinutes;
        this.shortBreakDurationMinutes = shortBreakDurationMinutes;
        this.longBreakDurationMinutes = longBreakDurationMinutes;
    }

    // Getters and Setters
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
}

