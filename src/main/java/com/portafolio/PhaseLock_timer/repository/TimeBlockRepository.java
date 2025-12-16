package com.portafolio.PhaseLock_timer.repository;

import com.portafolio.PhaseLock_timer.model.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {
}

