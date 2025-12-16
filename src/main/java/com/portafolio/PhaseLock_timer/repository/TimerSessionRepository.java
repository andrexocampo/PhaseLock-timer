package com.portafolio.PhaseLock_timer.repository;

import com.portafolio.PhaseLock_timer.model.TimerSession;
import com.portafolio.PhaseLock_timer.model.TimerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimerSessionRepository extends JpaRepository<TimerSession, Long> {
    Optional<TimerSession> findFirstByStatusInOrderByStartedAtDesc(List<TimerStatus> statuses);
}

