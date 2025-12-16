// Audio Manager for PhaseLock Timer
class AudioManager {
    constructor() {
        this.sounds = {};
        this.settings = this.loadSettings();
        this.enabled = this.settings.enabled !== false; // Default: enabled
        this.initializeSounds();
    }

    // Load sound settings from localStorage
    loadSettings() {
        try {
            const saved = localStorage.getItem('phaselock_sound_settings');
            if (saved) {
                return JSON.parse(saved);
            }
        } catch (error) {
            console.error('Error loading sound settings:', error);
        }
        
        // Default settings
        return {
            enabled: true,
            pomodoroStart: 'default',
            breakStart: 'default',
            timerComplete: 'default',
            volume: 0.7
        };
    }

    // Save sound settings to localStorage
    saveSettings() {
        try {
            localStorage.setItem('phaselock_sound_settings', JSON.stringify(this.settings));
        } catch (error) {
            console.error('Error saving sound settings:', error);
        }
    }

    // Initialize default sounds (using Web Audio API for simple tones)
    initializeSounds() {
        // Create audio context
        this.audioContext = null;
        try {
            this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
        } catch (error) {
            console.warn('Web Audio API not supported, using fallback');
        }

        // Preload default sounds
        this.createDefaultSounds();
    }

    // Create default sounds using Web Audio API
    createDefaultSounds() {
        if (!this.audioContext) return;

        // Pomodoro start sound - ascending melody (energetic, ~2 seconds)
        this.sounds.pomodoroStart = () => this.playMelody([
            { freq: 523, duration: 0.3 },  // C
            { freq: 659, duration: 0.3 },  // E
            { freq: 784, duration: 0.3 },  // G
            { freq: 988, duration: 0.3 }, // B
            { freq: 1175, duration: 0.4 }, // D (high)
            { freq: 1319, duration: 0.4 }  // E (high)
        ]);
        
        // Break start sound - descending melody (relaxing, ~2 seconds)
        this.sounds.breakStart = () => this.playMelody([
            { freq: 880, duration: 0.4 },  // A
            { freq: 784, duration: 0.3 },   // G
            { freq: 659, duration: 0.3 },   // E
            { freq: 587, duration: 0.3 },  // D
            { freq: 523, duration: 0.4 },   // C
            { freq: 440, duration: 0.3 }    // A (low)
        ]);
        
        // Timer complete sound - celebration melody (~2 seconds)
        this.sounds.timerComplete = () => {
            // First phrase
            this.playMelody([
                { freq: 523, duration: 0.2 },  // C
                { freq: 659, duration: 0.2 },  // E
                { freq: 784, duration: 0.3 }   // G
            ]);
            // Second phrase (slightly delayed)
            setTimeout(() => {
                this.playMelody([
                    { freq: 659, duration: 0.2 },  // E
                    { freq: 784, duration: 0.2 },  // G
                    { freq: 988, duration: 0.3 },   // B
                    { freq: 1175, duration: 0.4 }  // D (high)
                ], 0.7);
            }, 700);
        };
    }

    // Play a tone using Web Audio API
    playTone(frequency, duration, type = 'sine') {
        if (!this.audioContext || !this.enabled) return;

        try {
            const oscillator = this.audioContext.createOscillator();
            const gainNode = this.audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(this.audioContext.destination);

            oscillator.frequency.value = frequency;
            oscillator.type = type;

            gainNode.gain.setValueAtTime(0, this.audioContext.currentTime);
            gainNode.gain.linearRampToValueAtTime(
                this.settings.volume || 0.7,
                this.audioContext.currentTime + 0.01
            );
            gainNode.gain.exponentialRampToValueAtTime(
                0.01,
                this.audioContext.currentTime + duration
            );

            oscillator.start(this.audioContext.currentTime);
            oscillator.stop(this.audioContext.currentTime + duration);
        } catch (error) {
            console.error('Error playing tone:', error);
        }
    }

    // Play a melody (sequence of tones)
    playMelody(notes, startTime = 0) {
        if (!this.audioContext || !this.enabled) return;

        let currentTime = this.audioContext.currentTime + startTime;
        const volume = this.settings.volume || 0.7;

        notes.forEach((note, index) => {
            try {
                const oscillator = this.audioContext.createOscillator();
                const gainNode = this.audioContext.createGain();

                oscillator.connect(gainNode);
                gainNode.connect(this.audioContext.destination);

                oscillator.frequency.value = note.freq;
                oscillator.type = 'sine';

                // Smooth attack and release for each note
                gainNode.gain.setValueAtTime(0, currentTime);
                gainNode.gain.linearRampToValueAtTime(
                    volume * 0.8, // Slightly lower for melody to avoid harshness
                    currentTime + 0.05
                );
                gainNode.gain.linearRampToValueAtTime(
                    volume * 0.8,
                    currentTime + note.duration - 0.1
                );
                gainNode.gain.exponentialRampToValueAtTime(
                    0.01,
                    currentTime + note.duration
                );

                oscillator.start(currentTime);
                oscillator.stop(currentTime + note.duration);

                currentTime += note.duration;
            } catch (error) {
                console.error('Error playing melody note:', error);
            }
        });
    }

    // Play sound based on type
    playSound(type) {
        if (!this.enabled) return;

        // Resume audio context if suspended (browser autoplay policy)
        if (this.audioContext && this.audioContext.state === 'suspended') {
            this.audioContext.resume();
        }

        const soundKey = this.getSoundKey(type);
        const soundFunction = this.sounds[soundKey];
        
        if (soundFunction) {
            soundFunction();
        } else {
            // Fallback to default based on type
            this.playDefaultSound(type);
        }
    }

    // Get sound key based on phase type
    getSoundKey(type) {
        if (type === 'POMODORO') {
            return 'pomodoroStart';
        } else if (type === 'SHORT_BREAK' || type === 'LONG_BREAK') {
            return 'breakStart';
        } else if (type === 'COMPLETED') {
            return 'timerComplete';
        }
        return 'pomodoroStart'; // Default
    }

    // Play default sound based on type
    playDefaultSound(type) {
        if (type === 'POMODORO') {
            this.playMelody([
                { freq: 523, duration: 0.3 },  // C
                { freq: 659, duration: 0.3 },  // E
                { freq: 784, duration: 0.3 },  // G
                { freq: 988, duration: 0.3 }, // B
                { freq: 1175, duration: 0.4 }, // D (high)
                { freq: 1319, duration: 0.4 }  // E (high)
            ]);
        } else if (type === 'SHORT_BREAK' || type === 'LONG_BREAK') {
            this.playMelody([
                { freq: 880, duration: 0.4 },  // A
                { freq: 784, duration: 0.3 },   // G
                { freq: 659, duration: 0.3 },   // E
                { freq: 587, duration: 0.3 },  // D
                { freq: 523, duration: 0.4 },   // C
                { freq: 440, duration: 0.3 }    // A (low)
            ]);
        } else if (type === 'COMPLETED') {
            this.playMelody([
                { freq: 523, duration: 0.2 },  // C
                { freq: 659, duration: 0.2 },  // E
                { freq: 784, duration: 0.3 }   // G
            ]);
            setTimeout(() => {
                this.playMelody([
                    { freq: 659, duration: 0.2 },  // E
                    { freq: 784, duration: 0.2 },  // G
                    { freq: 988, duration: 0.3 },   // B
                    { freq: 1175, duration: 0.4 }  // D (high)
                ], 0.7);
            }, 700);
        }
    }

    // Update settings
    updateSettings(newSettings) {
        this.settings = { ...this.settings, ...newSettings };
        this.enabled = this.settings.enabled !== false;
        this.saveSettings();
    }

    // Get current settings
    getSettings() {
        return { ...this.settings };
    }

    // Enable/disable sounds
    setEnabled(enabled) {
        this.enabled = enabled;
        this.settings.enabled = enabled;
        this.saveSettings();
    }

    // Set volume (0.0 to 1.0)
    setVolume(volume) {
        this.settings.volume = Math.max(0, Math.min(1, volume));
        this.saveSettings();
    }
}

// Create global instance
const audioManager = new AudioManager();

