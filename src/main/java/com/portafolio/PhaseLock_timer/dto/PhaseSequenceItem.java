package com.portafolio.PhaseLock_timer.dto;

import com.portafolio.PhaseLock_timer.model.Phase;

public class PhaseSequenceItem {
    private Phase phase;
    private Integer durationMinutes;
    private Integer sequenceNumber; // Número de orden en la secuencia

    public PhaseSequenceItem() {
    }

    public PhaseSequenceItem(Phase phase, Integer durationMinutes, Integer sequenceNumber) {
        this.phase = phase;
        this.durationMinutes = durationMinutes;
        this.sequenceNumber = sequenceNumber;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}

