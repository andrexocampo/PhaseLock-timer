package com.portafolio.PhaseLock_timer.model;

public enum Phase {
    POMODORO("Pomodoro"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break");

    private final String displayName;

    Phase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

