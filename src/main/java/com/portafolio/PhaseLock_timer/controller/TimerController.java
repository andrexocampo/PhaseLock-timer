package com.portafolio.PhaseLock_timer.controller;

import com.portafolio.PhaseLock_timer.dto.TimerStatusDTO;
import com.portafolio.PhaseLock_timer.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/timer")
public class TimerController {

    private final TimerService timerService;

    @Autowired
    public TimerController(TimerService timerService) {
        this.timerService = timerService;
    }

    /**
     * RF2.1: Iniciar un bloque
     * POST /api/timer/start/{blockId}
     */
    @PostMapping("/start/{blockId}")
    public ResponseEntity<TimerStatusDTO> startBlock(@PathVariable Long blockId) {
        TimerStatusDTO status = timerService.startBlock(blockId);
        return ResponseEntity.ok(status);
    }

    /**
     * RF2.2: Pausar el timer
     * POST /api/timer/{sessionId}/pause
     */
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<TimerStatusDTO> pauseTimer(@PathVariable Long sessionId) {
        TimerStatusDTO status = timerService.pauseTimer(sessionId);
        return ResponseEntity.ok(status);
    }

    /**
     * RF2.2: Reanudar el timer
     * POST /api/timer/{sessionId}/resume
     */
    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<TimerStatusDTO> resumeTimer(@PathVariable Long sessionId) {
        TimerStatusDTO status = timerService.resumeTimer(sessionId);
        return ResponseEntity.ok(status);
    }

    /**
     * RF2.3: Reiniciar el bloque
     * POST /api/timer/{sessionId}/restart
     */
    @PostMapping("/{sessionId}/restart")
    public ResponseEntity<TimerStatusDTO> restartBlock(@PathVariable Long sessionId) {
        TimerStatusDTO status = timerService.restartBlock(sessionId);
        return ResponseEntity.ok(status);
    }

    /**
     * RF2.4: Saltar a la siguiente fase
     * POST /api/timer/{sessionId}/skip
     */
    @PostMapping("/{sessionId}/skip")
    public ResponseEntity<TimerStatusDTO> skipToNextPhase(@PathVariable Long sessionId) {
        TimerStatusDTO status = timerService.skipToNextPhase(sessionId);
        return ResponseEntity.ok(status);
    }

    /**
     * RF2.5: Cancelar el bloque
     * DELETE /api/timer/{sessionId}
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelBlock(@PathVariable Long sessionId) {
        timerService.cancelBlock(sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener estado del timer
     * GET /api/timer/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<TimerStatusDTO> getTimerStatus(@PathVariable Long sessionId) {
        TimerStatusDTO status = timerService.getTimerStatus(sessionId);
        return ResponseEntity.ok(status);
    }

    /**
     * Obtener sesión activa
     * GET /api/timer/active
     */
    @GetMapping("/active")
    public ResponseEntity<TimerStatusDTO> getActiveSession() {
        Optional<TimerStatusDTO> active = timerService.getActiveSession();
        return active.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
    }
}

