package com.portafolio.PhaseLock_timer.dto;

import java.util.List;

public class BlockSequenceDTO {
    private Integer totalDurationMinutes;
    private Integer numberOfPomodoros;
    private List<PhaseSequenceItem> sequence;
    private Integer totalPhases;

    public BlockSequenceDTO() {
    }

    public BlockSequenceDTO(Integer totalDurationMinutes, Integer numberOfPomodoros, 
                           List<PhaseSequenceItem> sequence) {
        this.totalDurationMinutes = totalDurationMinutes;
        this.numberOfPomodoros = numberOfPomodoros;
        this.sequence = sequence;
        this.totalPhases = sequence != null ? sequence.size() : 0;
    }

    public Integer getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(Integer totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public Integer getNumberOfPomodoros() {
        return numberOfPomodoros;
    }

    public void setNumberOfPomodoros(Integer numberOfPomodoros) {
        this.numberOfPomodoros = numberOfPomodoros;
    }

    public List<PhaseSequenceItem> getSequence() {
        return sequence;
    }

    public void setSequence(List<PhaseSequenceItem> sequence) {
        this.sequence = sequence;
        this.totalPhases = sequence != null ? sequence.size() : 0;
    }

    public Integer getTotalPhases() {
        return totalPhases;
    }

    public void setTotalPhases(Integer totalPhases) {
        this.totalPhases = totalPhases;
    }
}

