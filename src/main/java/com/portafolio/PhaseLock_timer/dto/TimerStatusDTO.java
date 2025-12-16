package com.portafolio.PhaseLock_timer.dto;

import com.portafolio.PhaseLock_timer.model.Phase;
import com.portafolio.PhaseLock_timer.model.TimerStatus;

public class TimerStatusDTO {
    private Long sessionId;
    private Long blockId;
    private TimerStatus status;
    private Phase currentPhase;
    private Integer currentPhaseIndex;
    private Integer remainingSeconds;
    private Integer totalElapsedSeconds;
    private Integer totalPhases;
    private String formattedRemainingTime; // MM:SS
    private String formattedElapsedTime;   // HH:MM:SS

    public TimerStatusDTO() {
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public TimerStatus getStatus() {
        return status;
    }

    public void setStatus(TimerStatus status) {
        this.status = status;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Phase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Integer getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    public void setCurrentPhaseIndex(Integer currentPhaseIndex) {
        this.currentPhaseIndex = currentPhaseIndex;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public Integer getTotalElapsedSeconds() {
        return totalElapsedSeconds;
    }

    public void setTotalElapsedSeconds(Integer totalElapsedSeconds) {
        this.totalElapsedSeconds = totalElapsedSeconds;
    }

    public Integer getTotalPhases() {
        return totalPhases;
    }

    public void setTotalPhases(Integer totalPhases) {
        this.totalPhases = totalPhases;
    }

    public String getFormattedRemainingTime() {
        return formattedRemainingTime;
    }

    public void setFormattedRemainingTime(String formattedRemainingTime) {
        this.formattedRemainingTime = formattedRemainingTime;
    }

    public String getFormattedElapsedTime() {
        return formattedElapsedTime;
    }

    public void setFormattedElapsedTime(String formattedElapsedTime) {
        this.formattedElapsedTime = formattedElapsedTime;
    }
}

