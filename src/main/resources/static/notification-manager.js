// Notification Manager for PhaseLock Timer
class NotificationManager {
    constructor() {
        this.permission = null;
        this.checkPermission();
    }

    // Check current notification permission
    checkPermission() {
        if (!('Notification' in window)) {
            console.warn('This browser does not support notifications');
            this.permission = 'unsupported';
            return;
        }

        this.permission = Notification.permission;
        return this.permission;
    }

    // Request notification permission from user
    async requestPermission() {
        if (!('Notification' in window)) {
            return false;
        }

        if (this.permission === 'granted') {
            return true;
        }

        if (this.permission === 'denied') {
            console.warn('Notification permission denied');
            return false;
        }

        try {
            const permission = await Notification.requestPermission();
            this.permission = permission;
            return permission === 'granted';
        } catch (error) {
            console.error('Error requesting notification permission:', error);
            return false;
        }
    }

    // Check if page is currently visible
    isPageVisible() {
        return !document.hidden;
    }

    // Show browser notification
    showNotification(title, message, icon = null, tag = 'phaselock-timer') {
        if (!('Notification' in window)) {
            return;
        }

        if (this.permission !== 'granted') {
            return;
        }

        // Only show if page is not visible
        if (this.isPageVisible()) {
            return;
        }

        try {
            const options = {
                body: message,
                icon: icon || '/favicon.ico',
                tag: tag,
                requireInteraction: false,
                silent: false
            };

            const notification = new Notification(title, options);

            // Auto-close after 5 seconds
            setTimeout(() => {
                notification.close();
            }, 5000);

            // Handle click
            notification.onclick = () => {
                window.focus();
                notification.close();
            };

            return notification;
        } catch (error) {
            console.error('Error showing notification:', error);
        }
    }

    // Show phase change notification
    showPhaseChangeNotification(newPhase, phaseNumber, totalPhases) {
        const phaseNames = {
            'POMODORO': 'Pomodoro',
            'SHORT_BREAK': 'Short Break',
            'LONG_BREAK': 'Long Break'
        };

        const phaseName = phaseNames[newPhase] || newPhase;
        const title = `${phaseName} Started`;
        const message = `Phase ${phaseNumber} of ${totalPhases}`;

        return this.showNotification(title, message);
    }

    // Show timer completed notification
    showTimerCompleteNotification() {
        const title = 'Block Completed!';
        const message = 'Great job! Your time block is complete.';

        return this.showNotification(title, message, null, 'phaselock-complete');
    }

    // Get permission status
    getPermission() {
        return this.permission;
    }

    // Check if notifications are supported
    isSupported() {
        return 'Notification' in window;
    }
}

// Create global instance
const notificationManager = new NotificationManager();

