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

        // Pomodoro start sound (higher pitch, shorter)
        this.sounds.pomodoroStart = () => this.playTone(800, 0.2, 'sine');
        
        // Break start sound (medium pitch, medium length)
        this.sounds.breakStart = () => this.playTone(600, 0.3, 'sine');
        
        // Timer complete sound (chord, longer)
        this.sounds.timerComplete = () => {
            this.playTone(523, 0.3, 'sine'); // C
            setTimeout(() => this.playTone(659, 0.3, 'sine'), 100); // E
            setTimeout(() => this.playTone(784, 0.4, 'sine'), 200); // G
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
            this.playTone(800, 0.2, 'sine');
        } else if (type === 'SHORT_BREAK' || type === 'LONG_BREAK') {
            this.playTone(600, 0.3, 'sine');
        } else if (type === 'COMPLETED') {
            this.playTone(523, 0.3, 'sine');
            setTimeout(() => this.playTone(659, 0.3, 'sine'), 100);
            setTimeout(() => this.playTone(784, 0.4, 'sine'), 200);
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

