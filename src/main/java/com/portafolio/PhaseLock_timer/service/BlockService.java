package com.portafolio.PhaseLock_timer.service;

import com.portafolio.PhaseLock_timer.config.BlockConfiguration;
import com.portafolio.PhaseLock_timer.dto.BlockConfigurationDTO;
import com.portafolio.PhaseLock_timer.dto.BlockSequenceDTO;
import com.portafolio.PhaseLock_timer.dto.PhaseSequenceItem;
import com.portafolio.PhaseLock_timer.dto.TimeBlockDTO;
import com.portafolio.PhaseLock_timer.model.Phase;
import com.portafolio.PhaseLock_timer.model.TimeBlock;
import com.portafolio.PhaseLock_timer.repository.TimeBlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlockService {

    private final TimeBlockRepository timeBlockRepository;
    private final BlockConfiguration defaultConfig;

    @Autowired
    public BlockService(TimeBlockRepository timeBlockRepository, BlockConfiguration defaultConfig) {
        this.timeBlockRepository = timeBlockRepository;
        this.defaultConfig = defaultConfig;
    }

    /**
     * Calcula el número de pomodoros que caben en la duración total
     * Fórmula: (totalDuration - longBreak) / (pomodoro + shortBreak)
     */
    public Integer calculateNumberOfPomodoros(Integer totalDurationMinutes, 
                                               Integer pomodoroDurationMinutes,
                                               Integer shortBreakDurationMinutes,
                                               Integer longBreakDurationMinutes) {
        int cycleDuration = pomodoroDurationMinutes + shortBreakDurationMinutes;
        int availableTime = totalDurationMinutes - longBreakDurationMinutes;
        
        if (availableTime < pomodoroDurationMinutes) {
            return 1; // Al menos un pomodoro
        }
        
        return availableTime / cycleDuration;
    }

    /**
     * Genera la secuencia completa de fases del bloque
     * Ejemplo: [Pomodoro(25) -> ShortBreak(5) -> Pomodoro(25) -> ShortBreak(5) -> Pomodoro(25) -> LongBreak(30)]
     */
    public BlockSequenceDTO calculateSequence(Integer totalDurationMinutes,
                                              Integer pomodoroDurationMinutes,
                                              Integer shortBreakDurationMinutes,
                                              Integer longBreakDurationMinutes) {
        Integer numberOfPomodoros = calculateNumberOfPomodoros(
            totalDurationMinutes, pomodoroDurationMinutes, 
            shortBreakDurationMinutes, longBreakDurationMinutes
        );

        List<PhaseSequenceItem> sequence = new ArrayList<>();
        int sequenceNumber = 1;

        // Agregar pomodoros con sus breaks cortos
        for (int i = 0; i < numberOfPomodoros; i++) {
            // Pomodoro
            sequence.add(new PhaseSequenceItem(
                Phase.POMODORO, 
                pomodoroDurationMinutes, 
                sequenceNumber++
            ));

            // Short break después de cada pomodoro (excepto el último)
            if (i < numberOfPomodoros - 1) {
                sequence.add(new PhaseSequenceItem(
                    Phase.SHORT_BREAK, 
                    shortBreakDurationMinutes, 
                    sequenceNumber++
                ));
            }
        }

        // Agregar el break largo final
        if (longBreakDurationMinutes > 0) {
            sequence.add(new PhaseSequenceItem(
                Phase.LONG_BREAK, 
                longBreakDurationMinutes, 
                sequenceNumber
            ));
        }

        return new BlockSequenceDTO(totalDurationMinutes, numberOfPomodoros, sequence);
    }

    /**
     * Crea un nuevo TimeBlock con la configuración proporcionada
     */
    public TimeBlockDTO createBlock(BlockConfigurationDTO configDTO) {
        // Calcular número de pomodoros
        Integer numberOfPomodoros = calculateNumberOfPomodoros(
            configDTO.getTotalDurationMinutes(),
            configDTO.getPomodoroDurationMinutes(),
            configDTO.getShortBreakDurationMinutes(),
            configDTO.getLongBreakDurationMinutes()
        );

        // Crear entidad
        TimeBlock timeBlock = new TimeBlock(
            configDTO.getTotalDurationMinutes(),
            configDTO.getPomodoroDurationMinutes(),
            configDTO.getShortBreakDurationMinutes(),
            configDTO.getLongBreakDurationMinutes()
        );
        timeBlock.setNumberOfPomodoros(numberOfPomodoros);

        // Guardar en BD
        TimeBlock saved = timeBlockRepository.save(timeBlock);

        // Generar secuencia
        BlockSequenceDTO sequence = calculateSequence(
            saved.getTotalDurationMinutes(),
            saved.getPomodoroDurationMinutes(),
            saved.getShortBreakDurationMinutes(),
            saved.getLongBreakDurationMinutes()
        );

        // Convertir a DTO
        return toDTO(saved, sequence);
    }

    /**
     * Crea un TimeBlock con valores por defecto
     */
    public TimeBlockDTO createBlockWithDefaults() {
        BlockConfigurationDTO defaultDTO = new BlockConfigurationDTO(
            defaultConfig.getTotalDurationMinutes(),
            defaultConfig.getPomodoroDurationMinutes(),
            defaultConfig.getShortBreakDurationMinutes(),
            defaultConfig.getLongBreakDurationMinutes()
        );
        return createBlock(defaultDTO);
    }

    /**
     * Obtiene un TimeBlock por ID
     */
    @Transactional(readOnly = true)
    public TimeBlockDTO getBlockById(Long id) {
        TimeBlock timeBlock = timeBlockRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("TimeBlock not found with id: " + id));

        BlockSequenceDTO sequence = calculateSequence(
            timeBlock.getTotalDurationMinutes(),
            timeBlock.getPomodoroDurationMinutes(),
            timeBlock.getShortBreakDurationMinutes(),
            timeBlock.getLongBreakDurationMinutes()
        );

        return toDTO(timeBlock, sequence);
    }

    /**
     * Obtiene todos los TimeBlocks
     */
    @Transactional(readOnly = true)
    public List<TimeBlockDTO> getAllBlocks() {
        return timeBlockRepository.findAll().stream()
            .map(block -> {
                BlockSequenceDTO sequence = calculateSequence(
                    block.getTotalDurationMinutes(),
                    block.getPomodoroDurationMinutes(),
                    block.getShortBreakDurationMinutes(),
                    block.getLongBreakDurationMinutes()
                );
                return toDTO(block, sequence);
            })
            .collect(Collectors.toList());
    }

    /**
     * Calcula solo la secuencia sin crear un bloque (para preview)
     */
    @Transactional(readOnly = true)
    public BlockSequenceDTO previewSequence(BlockConfigurationDTO configDTO) {
        return calculateSequence(
            configDTO.getTotalDurationMinutes(),
            configDTO.getPomodoroDurationMinutes(),
            configDTO.getShortBreakDurationMinutes(),
            configDTO.getLongBreakDurationMinutes()
        );
    }

    /**
     * Convierte TimeBlock a DTO
     */
    private TimeBlockDTO toDTO(TimeBlock timeBlock, BlockSequenceDTO sequence) {
        TimeBlockDTO dto = new TimeBlockDTO();
        dto.setId(timeBlock.getId());
        dto.setTotalDurationMinutes(timeBlock.getTotalDurationMinutes());
        dto.setPomodoroDurationMinutes(timeBlock.getPomodoroDurationMinutes());
        dto.setShortBreakDurationMinutes(timeBlock.getShortBreakDurationMinutes());
        dto.setLongBreakDurationMinutes(timeBlock.getLongBreakDurationMinutes());
        dto.setNumberOfPomodoros(timeBlock.getNumberOfPomodoros());
        dto.setCreatedAt(timeBlock.getCreatedAt());
        dto.setSequence(sequence);
        return dto;
    }
}

