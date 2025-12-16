package com.portafolio.PhaseLock_timer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "phaselock.block.defaults")
public class BlockConfiguration {

    private Integer totalDurationMinutes = 120; // 2 horas por defecto
    private Integer pomodoroDurationMinutes = 25; // 25 minutos por defecto
    private Integer shortBreakDurationMinutes = 5; // 5 minutos por defecto
    private Integer longBreakDurationMinutes = 30; // 30 minutos por defecto

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

