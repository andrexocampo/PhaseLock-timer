package com.portafolio.PhaseLock_timer.service;

import com.portafolio.PhaseLock_timer.dto.BlockSequenceDTO;
import com.portafolio.PhaseLock_timer.dto.PhaseSequenceItem;
import com.portafolio.PhaseLock_timer.dto.TimerStatusDTO;
import com.portafolio.PhaseLock_timer.model.Phase;
import com.portafolio.PhaseLock_timer.model.TimeBlock;
import com.portafolio.PhaseLock_timer.model.TimerSession;
import com.portafolio.PhaseLock_timer.model.TimerStatus;
import com.portafolio.PhaseLock_timer.repository.TimeBlockRepository;
import com.portafolio.PhaseLock_timer.repository.TimerSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class TimerService {

    private final TimerSessionRepository timerSessionRepository;
    private final TimeBlockRepository timeBlockRepository;
    private final BlockService blockService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Mapa para almacenar los schedulers activos por sesión
    private final Map<Long, ScheduledExecutorService> activeTimers = new ConcurrentHashMap<>();
    
    // Mapa para almacenar las secuencias calculadas por sesión
    private final Map<Long, BlockSequenceDTO> sessionSequences = new ConcurrentHashMap<>();

    @Autowired
    public TimerService(TimerSessionRepository timerSessionRepository,
                       TimeBlockRepository timeBlockRepository,
                       BlockService blockService,
                       SimpMessagingTemplate messagingTemplate) {
        this.timerSessionRepository = timerSessionRepository;
        this.timeBlockRepository = timeBlockRepository;
        this.blockService = blockService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * RF2.1: Iniciar un bloque configurado
     */
    public TimerStatusDTO startBlock(Long blockId) {
        // Verificar si hay una sesión activa
        Optional<TimerSession> activeSession = timerSessionRepository
            .findFirstByStatusInOrderByStartedAtDesc(
                Arrays.asList(TimerStatus.RUNNING, TimerStatus.PAUSED)
            );
        
        if (activeSession.isPresent()) {
            throw new IllegalStateException("An active session already exists. Cancel or complete the current session first.");
        }

        // Obtener el bloque
        TimeBlock timeBlock = timeBlockRepository.findById(blockId)
            .orElseThrow(() -> new IllegalArgumentException("TimeBlock not found with id: " + blockId));

        // Calcular la secuencia
        BlockSequenceDTO sequence = blockService.calculateSequence(
            timeBlock.getTotalDurationMinutes(),
            timeBlock.getPomodoroDurationMinutes(),
            timeBlock.getShortBreakDurationMinutes(),
            timeBlock.getLongBreakDurationMinutes()
        );

        // Crear nueva sesión
        TimerSession session = new TimerSession(timeBlock);
        session.setStatus(TimerStatus.RUNNING);
        session.setStartedAt(LocalDateTime.now());
        
        // Inicializar con la primera fase
        PhaseSequenceItem firstPhase = sequence.getSequence().get(0);
        session.setCurrentPhase(firstPhase.getPhase());
        session.setCurrentPhaseIndex(0);
        session.setRemainingSeconds(firstPhase.getDurationMinutes() * 60);
        session.setTotalElapsedSeconds(0);

        // Guardar sesión
        TimerSession saved = timerSessionRepository.save(session);
        
        // Guardar secuencia en memoria
        sessionSequences.put(saved.getId(), sequence);

        // Iniciar el timer
        startTimer(saved.getId());

        TimerStatusDTO dto = toDTO(saved, sequence);
        // Send initial update via WebSocket
        sendTimerUpdate(saved.getId());
        return dto;
    }

    /**
     * RF2.2: Pausar el timer
     */
    public TimerStatusDTO pauseTimer(Long sessionId) {
        TimerSession session = getSession(sessionId);
        
        if (session.getStatus() != TimerStatus.RUNNING) {
            throw new IllegalStateException("Timer is not running. Current status: " + session.getStatus());
        }

        // Detener el scheduler
        stopScheduler(sessionId);

        // Actualizar estado
        session.setStatus(TimerStatus.PAUSED);
        session.setPausedAt(LocalDateTime.now());
        timerSessionRepository.save(session);

        BlockSequenceDTO sequence = sessionSequences.get(sessionId);
        TimerStatusDTO dto = toDTO(session, sequence);
        sendTimerUpdate(sessionId);
        return dto;
    }

    /**
     * RF2.2: Reanudar el timer
     */
    public TimerStatusDTO resumeTimer(Long sessionId) {
        TimerSession session = getSession(sessionId);
        
        if (session.getStatus() != TimerStatus.PAUSED) {
            throw new IllegalStateException("Timer is not paused. Current status: " + session.getStatus());
        }

        // Reanudar el scheduler
        session.setStatus(TimerStatus.RUNNING);
        session.setPausedAt(null);
        timerSessionRepository.save(session);
        
        startTimer(sessionId);

        BlockSequenceDTO sequence = sessionSequences.get(sessionId);
        TimerStatusDTO dto = toDTO(session, sequence);
        sendTimerUpdate(sessionId);
        return dto;
    }

    /**
     * RF2.3: Reiniciar el bloque actual
     */
    public TimerStatusDTO restartBlock(Long sessionId) {
        TimerSession session = getSession(sessionId);
        
        // Detener el scheduler actual
        stopScheduler(sessionId);

        // Reiniciar a la primera fase
        BlockSequenceDTO sequence = sessionSequences.get(sessionId);
        PhaseSequenceItem firstPhase = sequence.getSequence().get(0);
        
        session.setStatus(TimerStatus.RUNNING);
        session.setCurrentPhase(firstPhase.getPhase());
        session.setCurrentPhaseIndex(0);
        session.setRemainingSeconds(firstPhase.getDurationMinutes() * 60);
        session.setTotalElapsedSeconds(0);
        session.setStartedAt(LocalDateTime.now());
        session.setPausedAt(null);
        session.setCompletedAt(null);
        
        timerSessionRepository.save(session);

        // Reiniciar el timer
        startTimer(sessionId);

        TimerStatusDTO dto = toDTO(session, sequence);
        sendTimerUpdate(sessionId);
        return dto;
    }

    /**
     * RF2.4: Saltar a la siguiente fase
     */
    public TimerStatusDTO skipToNextPhase(Long sessionId) {
        TimerSession session = getSession(sessionId);
        
        if (session.getStatus() != TimerStatus.RUNNING && session.getStatus() != TimerStatus.PAUSED) {
            throw new IllegalStateException("Cannot skip. Timer is not active.");
        }

        BlockSequenceDTO sequence = sessionSequences.get(sessionId);
        
        // Verificar si hay siguiente fase
        if (session.getCurrentPhaseIndex() >= sequence.getSequence().size() - 1) {
            // Ya estamos en la última fase, completar el bloque
            return completeBlock(sessionId);
        }

        // Detener el scheduler actual
        stopScheduler(sessionId);

        // Avanzar a la siguiente fase
        int nextIndex = session.getCurrentPhaseIndex() + 1;
        PhaseSequenceItem nextPhase = sequence.getSequence().get(nextIndex);
        
        session.setCurrentPhaseIndex(nextIndex);
        session.setCurrentPhase(nextPhase.getPhase());
        session.setRemainingSeconds(nextPhase.getDurationMinutes() * 60);
        
        // Si estaba pausado, mantener pausado; si estaba corriendo, continuar
        if (session.getStatus() == TimerStatus.PAUSED) {
            timerSessionRepository.save(session);
        } else {
            session.setStatus(TimerStatus.RUNNING);
            timerSessionRepository.save(session);
            startTimer(sessionId);
        }

        TimerStatusDTO dto = toDTO(session, sequence);
        sendTimerUpdate(sessionId);
        return dto;
    }

    /**
     * RF2.5: Cancelar el bloque actual
     */
    public void cancelBlock(Long sessionId) {
        TimerSession session = getSession(sessionId);
        
        // Detener el scheduler
        stopScheduler(sessionId);

        // Actualizar estado
        session.setStatus(TimerStatus.STOPPED);
        timerSessionRepository.save(session);

        // Limpiar de memoria
        sessionSequences.remove(sessionId);
    }

    /**
     * Obtener el estado actual del timer
     */
    @Transactional(readOnly = true)
    public TimerStatusDTO getTimerStatus(Long sessionId) {
        TimerSession session = getSession(sessionId);
        BlockSequenceDTO sequence = sessionSequences.get(sessionId);
        
        if (sequence == null) {
            // Recalcular si no está en memoria
            TimeBlock block = session.getTimeBlock();
            sequence = blockService.calculateSequence(
                block.getTotalDurationMinutes(),
                block.getPomodoroDurationMinutes(),
                block.getShortBreakDurationMinutes(),
                block.getLongBreakDurationMinutes()
            );
            sessionSequences.put(sessionId, sequence);
        }
        
        return toDTO(session, sequence);
    }

    /**
     * Obtener la sesión activa actual
     */
    @Transactional(readOnly = true)
    public Optional<TimerStatusDTO> getActiveSession() {
        Optional<TimerSession> activeSession = timerSessionRepository
            .findFirstByStatusInOrderByStartedAtDesc(
                Arrays.asList(TimerStatus.RUNNING, TimerStatus.PAUSED)
            );
        
        if (activeSession.isEmpty()) {
            return Optional.empty();
        }

        TimerSession session = activeSession.get();
        BlockSequenceDTO sequence = sessionSequences.get(session.getId());
        
        if (sequence == null) {
            TimeBlock block = session.getTimeBlock();
            sequence = blockService.calculateSequence(
                block.getTotalDurationMinutes(),
                block.getPomodoroDurationMinutes(),
                block.getShortBreakDurationMinutes(),
                block.getLongBreakDurationMinutes()
            );
            sessionSequences.put(session.getId(), sequence);
        }
        
        return Optional.of(toDTO(session, sequence));
    }

    /**
     * Iniciar el scheduler del timer
     */
    private void startTimer(Long sessionId) {
        // Detener cualquier scheduler existente
        stopScheduler(sessionId);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        activeTimers.put(sessionId, scheduler);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateTimer(sessionId);
            } catch (Exception e) {
                // Log error
                System.err.println("Error updating timer: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Actualizar el timer cada segundo
     */
    @Async
    private void updateTimer(Long sessionId) {
        TimerSession session = timerSessionRepository.findById(sessionId)
            .orElse(null);
        
        if (session == null || session.getStatus() != TimerStatus.RUNNING) {
            stopScheduler(sessionId);
            return;
        }

        // Decrementar segundos restantes
        int remaining = session.getRemainingSeconds() - 1;
        session.setRemainingSeconds(remaining);
        session.setTotalElapsedSeconds(session.getTotalElapsedSeconds() + 1);

        // Si la fase actual terminó, avanzar a la siguiente
        if (remaining <= 0) {
            BlockSequenceDTO sequence = sessionSequences.get(sessionId);
            
            if (session.getCurrentPhaseIndex() >= sequence.getSequence().size() - 1) {
                // Última fase completada
                completeBlock(sessionId);
            } else {
                // Avanzar a la siguiente fase
                int nextIndex = session.getCurrentPhaseIndex() + 1;
                PhaseSequenceItem nextPhase = sequence.getSequence().get(nextIndex);
                
                session.setCurrentPhaseIndex(nextIndex);
                session.setCurrentPhase(nextPhase.getPhase());
                session.setRemainingSeconds(nextPhase.getDurationMinutes() * 60);
            }
        }

        timerSessionRepository.save(session);
        
        // Send update via WebSocket
        sendTimerUpdate(sessionId);
    }

    /**
     * Send timer update via WebSocket
     */
    private void sendTimerUpdate(Long sessionId) {
        try {
            TimerSession session = timerSessionRepository.findById(sessionId).orElse(null);
            if (session != null) {
                BlockSequenceDTO sequence = sessionSequences.get(sessionId);
                if (sequence != null) {
                    TimerStatusDTO status = toDTO(session, sequence);
                    messagingTemplate.convertAndSend("/topic/timer/" + sessionId, status);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending WebSocket update: " + e.getMessage());
        }
    }

    /**
     * Completar el bloque
     */
    private TimerStatusDTO completeBlock(Long sessionId) {
        TimerSession session = getSession(sessionId);
        
        stopScheduler(sessionId);
        
        session.setStatus(TimerStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        timerSessionRepository.save(session);

        BlockSequenceDTO sequence = sessionSequences.get(sessionId);
        TimerStatusDTO dto = toDTO(session, sequence);
        
        // Send final update via WebSocket
        sendTimerUpdate(sessionId);
        
        // Limpiar de memoria después de un delay
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sessionSequences.remove(sessionId);
            }
        }, 5000); // 5 segundos después

        return dto;
    }

    /**
     * Detener el scheduler
     */
    private void stopScheduler(Long sessionId) {
        ScheduledExecutorService scheduler = activeTimers.remove(sessionId);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Obtener sesión o lanzar excepción
     */
    private TimerSession getSession(Long sessionId) {
        return timerSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("TimerSession not found with id: " + sessionId));
    }

    /**
     * Convertir a DTO
     */
    private TimerStatusDTO toDTO(TimerSession session, BlockSequenceDTO sequence) {
        TimerStatusDTO dto = new TimerStatusDTO();
        dto.setSessionId(session.getId());
        dto.setBlockId(session.getTimeBlock().getId());
        dto.setStatus(session.getStatus());
        dto.setCurrentPhase(session.getCurrentPhase());
        dto.setCurrentPhaseIndex(session.getCurrentPhaseIndex());
        dto.setRemainingSeconds(session.getRemainingSeconds());
        dto.setTotalElapsedSeconds(session.getTotalElapsedSeconds());
        dto.setTotalPhases(sequence.getSequence().size());
        dto.setFormattedRemainingTime(formatTime(session.getRemainingSeconds()));
        dto.setFormattedElapsedTime(formatElapsedTime(session.getTotalElapsedSeconds()));
        return dto;
    }

    /**
     * Formatear tiempo en MM:SS
     */
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Formatear tiempo transcurrido en HH:MM:SS
     */
    private String formatElapsedTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

