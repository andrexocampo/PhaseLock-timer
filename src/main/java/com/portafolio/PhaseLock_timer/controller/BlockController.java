package com.portafolio.PhaseLock_timer.controller;

import com.portafolio.PhaseLock_timer.dto.BlockConfigurationDTO;
import com.portafolio.PhaseLock_timer.dto.BlockSequenceDTO;
import com.portafolio.PhaseLock_timer.dto.TimeBlockDTO;
import com.portafolio.PhaseLock_timer.service.BlockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
@Validated
public class BlockController {

    private final BlockService blockService;

    @Autowired
    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    /**
     * RF1.1: Crear un bloque con configuración personalizada
     * POST /api/blocks
     */
    @PostMapping
    public ResponseEntity<TimeBlockDTO> createBlock(@Valid @RequestBody BlockConfigurationDTO configDTO) {
        TimeBlockDTO created = blockService.createBlock(configDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * RF1.1: Crear un bloque con valores por defecto
     * POST /api/blocks/default
     */
    @PostMapping("/default")
    public ResponseEntity<TimeBlockDTO> createBlockWithDefaults() {
        TimeBlockDTO created = blockService.createBlockWithDefaults();
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * RF1.3: Obtener la secuencia calculada de un bloque existente
     * GET /api/blocks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TimeBlockDTO> getBlock(@PathVariable Long id) {
        TimeBlockDTO block = blockService.getBlockById(id);
        return ResponseEntity.ok(block);
    }

    /**
     * Obtener todos los bloques
     * GET /api/blocks
     */
    @GetMapping
    public ResponseEntity<List<TimeBlockDTO>> getAllBlocks() {
        List<TimeBlockDTO> blocks = blockService.getAllBlocks();
        return ResponseEntity.ok(blocks);
    }

    /**
     * RF1.3: Preview de la secuencia sin crear el bloque
     * POST /api/blocks/preview
     */
    @PostMapping("/preview")
    public ResponseEntity<BlockSequenceDTO> previewSequence(@Valid @RequestBody BlockConfigurationDTO configDTO) {
        BlockSequenceDTO sequence = blockService.previewSequence(configDTO);
        return ResponseEntity.ok(sequence);
    }

    /**
     * Obtener valores por defecto
     * GET /api/blocks/defaults
     */
    @GetMapping("/defaults")
    public ResponseEntity<BlockConfigurationDTO> getDefaults() {
        BlockConfigurationDTO defaults = new BlockConfigurationDTO(
            120, // 2 horas
            25,  // 25 minutos
            5,   // 5 minutos
            30   // 30 minutos
        );
        return ResponseEntity.ok(defaults);
    }
}

