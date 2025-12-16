package com.portafolio.PhaseLock_timer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "timer_sessions")
public class TimerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_block_id", nullable = false)
    private TimeBlock timeBlock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Phase currentPhase;

    @Column(nullable = false)
    private Integer currentPhaseIndex; // Índice en la secuencia (0-based)

    @Column(nullable = false)
    private Integer remainingSeconds; // Segundos restantes en la fase actual

    @Column(nullable = false)
    private Integer totalElapsedSeconds; // Tiempo total transcurrido del bloque

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null && status == TimerStatus.RUNNING) {
            startedAt = LocalDateTime.now();
        }
    }

    // Constructors
    public TimerSession() {
    }

    public TimerSession(TimeBlock timeBlock) {
        this.timeBlock = timeBlock;
        this.status = TimerStatus.STOPPED;
        this.currentPhase = Phase.POMODORO;
        this.currentPhaseIndex = 0;
        this.totalElapsedSeconds = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimeBlock getTimeBlock() {
        return timeBlock;
    }

    public void setTimeBlock(TimeBlock timeBlock) {
        this.timeBlock = timeBlock;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(LocalDateTime pausedAt) {
        this.pausedAt = pausedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

