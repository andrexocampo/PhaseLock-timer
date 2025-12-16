const API_BASE = '/api';
let currentSessionId = null;
let currentBlockId = null;
let updateInterval = null;
let stompClient = null;
let websocketConnected = false;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

// Track previous phase for change detection
let previousPhase = null;
let previousPhaseIndex = null;
let previousStatus = null;

// Initialize on page load
window.addEventListener('DOMContentLoaded', () => {
    checkActiveSession();
    setupForm();
    setupNotifications();
});

// Setup notifications permission on load
async function setupNotifications() {
    if (notificationManager.isSupported()) {
        await notificationManager.requestPermission();
    }
}

// Setup form
function setupForm() {
    document.getElementById('blockForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        await createBlock();
    });
}

// Check if there's an active session
async function checkActiveSession() {
    try {
        const response = await fetch(`${API_BASE}/timer/active`);
        if (response.ok) {
            const status = await response.json();
            currentSessionId = status.sessionId;
            currentBlockId = status.blockId;
            updateTimerDisplay(status);
            startPolling(); // This will try WebSocket first
            updateControlButtons(status.status);
        } else {
            resetTimerDisplay();
        }
    } catch (error) {
        console.error('Error checking active session:', error);
    }
}

// Create block from form
async function createBlock() {
    const formData = {
        totalDurationMinutes: parseInt(document.getElementById('totalDuration').value),
        pomodoroDurationMinutes: parseInt(document.getElementById('pomodoroDuration').value),
        shortBreakDurationMinutes: parseInt(document.getElementById('shortBreakDuration').value),
        longBreakDurationMinutes: parseInt(document.getElementById('longBreakDuration').value)
    };

    try {
        const response = await fetch(`${API_BASE}/blocks`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Error creating block');
        }

        const block = await response.json();
        currentBlockId = block.id;
        showMessage(`Block created successfully (ID: ${block.id})`, 'success');
        
        // Optional: start automatically
        // await startTimerWithBlock(block.id);
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Create block with default values
async function createDefaultBlock() {
    try {
        const response = await fetch(`${API_BASE}/blocks/default`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Error creating default block');
        }

        const block = await response.json();
        currentBlockId = block.id;
        showMessage(`Default block created (ID: ${block.id})`, 'success');
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Preview sequence
async function previewSequence() {
    const formData = {
        totalDurationMinutes: parseInt(document.getElementById('totalDuration').value),
        pomodoroDurationMinutes: parseInt(document.getElementById('pomodoroDuration').value),
        shortBreakDurationMinutes: parseInt(document.getElementById('shortBreakDuration').value),
        longBreakDurationMinutes: parseInt(document.getElementById('longBreakDuration').value)
    };

    try {
        const response = await fetch(`${API_BASE}/blocks/preview`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            throw new Error('Error calculating sequence');
        }

        const sequence = await response.json();
        displayPreview(sequence);
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Display preview
function displayPreview(sequence) {
    const previewPanel = document.getElementById('previewPanel');
    const previewContent = document.getElementById('previewContent');
    
    let html = `
        <div class="sequence-info">
            <p><strong>Total Duration:</strong> ${sequence.totalDurationMinutes} minutes</p>
            <p><strong>Number of Pomodoros:</strong> ${sequence.numberOfPomodoros}</p>
            <p><strong>Total Phases:</strong> ${sequence.totalPhases}</p>
        </div>
        <div class="sequence-list">
            <h3>Sequence:</h3>
            <ol class="phase-sequence">
    `;

    sequence.sequence.forEach((item, index) => {
        const phaseClass = item.phase.toLowerCase().replace('_', '-');
        const phaseName = item.phase === 'POMODORO' ? 'Pomodoro' :
                         item.phase === 'SHORT_BREAK' ? 'Short Break' :
                         'Long Break';
        
        html += `
            <li class="phase-item phase-${phaseClass}">
                <span class="phase-name">${phaseName}</span>
                <span class="phase-duration">${item.durationMinutes} min</span>
            </li>
        `;
    });

    html += `
            </ol>
        </div>
    `;

    previewContent.innerHTML = html;
    previewPanel.style.display = 'block';
    previewPanel.scrollIntoView({ behavior: 'smooth' });
}

// Start timer
async function startTimer() {
    if (!currentBlockId) {
        showMessage('You must create a block first', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/timer/start/${currentBlockId}`, {
            method: 'POST'
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Error starting timer');
        }

        const status = await response.json();
        currentSessionId = status.sessionId;
        updateTimerDisplay(status);
        startPolling();
        updateControlButtons(status.status);
        showMessage('Timer started', 'success');
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Pause timer
async function pauseTimer() {
    if (!currentSessionId) return;

    try {
        const response = await fetch(`${API_BASE}/timer/${currentSessionId}/pause`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Error pausing timer');
        }

        const status = await response.json();
        updateTimerDisplay(status);
        updateControlButtons(status.status);
        showMessage('Timer paused', 'info');
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Resume timer
async function resumeTimer() {
    if (!currentSessionId) return;

    try {
        const response = await fetch(`${API_BASE}/timer/${currentSessionId}/resume`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Error resuming timer');
        }

        const status = await response.json();
        updateTimerDisplay(status);
        startPolling();
        updateControlButtons(status.status);
        showMessage('Timer resumed', 'success');
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Restart timer
async function restartTimer() {
    if (!currentSessionId) return;

    try {
        const response = await fetch(`${API_BASE}/timer/${currentSessionId}/restart`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Error restarting timer');
        }

        const status = await response.json();
        updateTimerDisplay(status);
        startPolling();
        updateControlButtons(status.status);
        showMessage('Timer restarted', 'info');
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Skip phase
async function skipPhase() {
    if (!currentSessionId) return;

    try {
        const response = await fetch(`${API_BASE}/timer/${currentSessionId}/skip`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Error skipping phase');
        }

        const status = await response.json();
        updateTimerDisplay(status);
        updateControlButtons(status.status);
        showMessage('Phase skipped', 'info');
        
        if (status.status === 'COMPLETED') {
            stopPolling();
            showMessage('Block completed!', 'success');
        }
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Cancel timer
async function cancelTimer() {
    if (!currentSessionId) return;

    if (!confirm('Are you sure you want to cancel the timer?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/timer/${currentSessionId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Error canceling timer');
        }

        stopPolling();
        resetTimerDisplay();
        currentSessionId = null;
        showMessage('Timer canceled', 'info');
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

// Update timer display
function updateTimerDisplay(status) {
    // Detect phase change
    const phaseChanged = previousPhase !== status.currentPhase || 
                         previousPhaseIndex !== status.currentPhaseIndex;
    const statusChanged = previousStatus !== status.status;
    
    // Update remaining time
    document.getElementById('remainingTime').textContent = status.formattedRemainingTime;
    
    // Update elapsed time
    document.getElementById('elapsedTime').textContent = status.formattedElapsedTime;
    
    // Update phase
    const phaseLabel = document.getElementById('phaseLabel');
    const phaseNames = {
        'POMODORO': 'Pomodoro',
        'SHORT_BREAK': 'Short Break',
        'LONG_BREAK': 'Long Break'
    };
    phaseLabel.textContent = phaseNames[status.currentPhase] || status.currentPhase;
    
    // Update phase progress
    document.getElementById('phaseProgress').textContent = 
        `${status.currentPhaseIndex + 1} / ${status.totalPhases}`;
    
    // Update status
    const statusText = {
        'RUNNING': 'Running',
        'PAUSED': 'Paused',
        'STOPPED': 'Stopped',
        'COMPLETED': 'Completed'
    };
    document.getElementById('timerStatus').textContent = statusText[status.status] || status.status;
    
    // Update progress bar
    updateProgressBar(status);
    
    // Update colors based on phase
    const colorChanged = updatePhaseColors(status.currentPhase);
    
    // Update browser title
    updateBrowserTitle(status);
    
    // Handle phase change notifications and effects
    if (phaseChanged && previousPhase !== null) {
        handlePhaseChange(status);
    }
    
    // Handle status change (especially completion)
    if (statusChanged && status.status === 'COMPLETED' && previousStatus !== 'COMPLETED') {
        handleTimerComplete();
    }
    
    // Update previous values
    previousPhase = status.currentPhase;
    previousPhaseIndex = status.currentPhaseIndex;
    previousStatus = status.status;
}

// Handle phase change
function handlePhaseChange(status) {
    // Play sound
    audioManager.playSound(status.currentPhase);
    
    // Show browser notification (if page is not visible)
    notificationManager.showPhaseChangeNotification(
        status.currentPhase,
        status.currentPhaseIndex + 1,
        status.totalPhases
    );
    
    // Trigger visual effect
    triggerPhaseChangeEffect();
}

// Handle timer completion
function handleTimerComplete() {
    // Play completion sound
    audioManager.playSound('COMPLETED');
    
    // Show completion notification
    notificationManager.showTimerCompleteNotification();
    
    // Trigger completion effect
    triggerCompletionEffect();
}

// Trigger visual effect for phase change
function triggerPhaseChangeEffect() {
    const timerPanel = document.getElementById('timerPanel');
    const timeDisplay = document.querySelector('.time-display');
    
    // Add blink effect
    timerPanel.classList.add('phase-change-blink');
    timeDisplay.classList.add('phase-change-highlight');
    
    setTimeout(() => {
        timerPanel.classList.remove('phase-change-blink');
        timeDisplay.classList.remove('phase-change-highlight');
    }, 2000);
}

// Trigger visual effect for completion
function triggerCompletionEffect() {
    const timerPanel = document.getElementById('timerPanel');
    
    timerPanel.classList.add('timer-complete-effect');
    
    setTimeout(() => {
        timerPanel.classList.remove('timer-complete-effect');
    }, 3000);
}

// Update progress bar
function updateProgressBar(status) {
    const progressFill = document.getElementById('progressFill');
    const totalPhases = status.totalPhases;
    const currentPhase = status.currentPhaseIndex + 1;
    const progress = (currentPhase / totalPhases) * 100;
    progressFill.style.width = `${progress}%`;
}

// Update colors based on phase
function updatePhaseColors(phase) {
    const timerDisplay = document.querySelector('.time-display');
    const phaseIndicator = document.querySelector('.phase-indicator');
    
    const previousPhase = timerDisplay.classList.contains('phase-pomodoro') ? 'POMODORO' :
                         timerDisplay.classList.contains('phase-short-break') ? 'SHORT_BREAK' :
                         timerDisplay.classList.contains('phase-long-break') ? 'LONG_BREAK' : null;
    
    // Remove previous classes
    timerDisplay.classList.remove('phase-pomodoro', 'phase-short-break', 'phase-long-break');
    phaseIndicator.classList.remove('phase-pomodoro', 'phase-short-break', 'phase-long-break');
    
    // Add class based on phase
    if (phase === 'POMODORO') {
        timerDisplay.classList.add('phase-pomodoro');
        phaseIndicator.classList.add('phase-pomodoro');
    } else if (phase === 'SHORT_BREAK') {
        timerDisplay.classList.add('phase-short-break');
        phaseIndicator.classList.add('phase-short-break');
    } else if (phase === 'LONG_BREAK') {
        timerDisplay.classList.add('phase-long-break');
        phaseIndicator.classList.add('phase-long-break');
    }
    
    // Return true if phase actually changed
    return previousPhase !== phase;
}

// Update browser title
function updateBrowserTitle(status) {
    const phaseNames = {
        'POMODORO': 'Pomodoro',
        'SHORT_BREAK': 'Break',
        'LONG_BREAK': 'Long Break'
    };
    const phaseName = phaseNames[status.currentPhase] || 'Timer';
    document.title = `${status.formattedRemainingTime} - ${phaseName} | PhaseLock Timer`;
}

// Update control buttons
function updateControlButtons(status) {
    const startBtn = document.getElementById('startBtn');
    const pauseBtn = document.getElementById('pauseBtn');
    const resumeBtn = document.getElementById('resumeBtn');
    
    if (status === 'RUNNING') {
        startBtn.style.display = 'none';
        pauseBtn.style.display = 'inline-block';
        resumeBtn.style.display = 'none';
    } else if (status === 'PAUSED') {
        startBtn.style.display = 'none';
        pauseBtn.style.display = 'none';
        resumeBtn.style.display = 'inline-block';
    } else {
        startBtn.style.display = 'inline-block';
        pauseBtn.style.display = 'none';
        resumeBtn.style.display = 'none';
    }
}

// Reset timer display
function resetTimerDisplay() {
    document.getElementById('remainingTime').textContent = '00:00';
    document.getElementById('elapsedTime').textContent = '00:00:00';
    document.getElementById('phaseLabel').textContent = '-';
    document.getElementById('phaseProgress').textContent = '- / -';
    document.getElementById('timerStatus').textContent = 'Stopped';
    document.getElementById('progressFill').style.width = '0%';
    updateControlButtons('STOPPED');
    
    // Remove phase classes
    const timerDisplay = document.querySelector('.time-display');
    const phaseIndicator = document.querySelector('.phase-indicator');
    timerDisplay.classList.remove('phase-pomodoro', 'phase-short-break', 'phase-long-break');
    phaseIndicator.classList.remove('phase-pomodoro', 'phase-short-break', 'phase-long-break');
    
    // Reset previous phase tracking
    previousPhase = null;
    previousPhaseIndex = null;
    previousStatus = null;
    
    document.title = 'PhaseLock Timer';
}

// WebSocket connection management
function connectWebSocket() {
    if (stompClient && stompClient.connected) {
        return; // Already connected
    }

    if (!currentSessionId) {
        return; // No active session
    }

    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        
        // Disable debug logging
        stompClient.debug = () => {};
        
        stompClient.connect({}, function(frame) {
            console.log('WebSocket connected: ' + frame);
            websocketConnected = true;
            reconnectAttempts = 0;
            
            // Subscribe to timer updates
            stompClient.subscribe('/topic/timer/' + currentSessionId, function(message) {
                try {
                    const status = JSON.parse(message.body);
                    updateTimerDisplay(status);
                    
                    if (status.status === 'COMPLETED') {
                        disconnectWebSocket();
                        showMessage('Block completed!', 'success');
                    } else if (status.status === 'STOPPED') {
                        disconnectWebSocket();
                        resetTimerDisplay();
                    }
                } catch (error) {
                    console.error('Error processing WebSocket message:', error);
                }
            });
        }, function(error) {
            console.error('WebSocket connection error:', error);
            websocketConnected = false;
            handleWebSocketError();
        });
    } catch (error) {
        console.error('Error creating WebSocket connection:', error);
        handleWebSocketError();
    }
}

// Handle WebSocket connection errors - fallback to polling
function handleWebSocketError() {
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        console.log(`Attempting to reconnect WebSocket (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})...`);
        setTimeout(() => {
            if (currentSessionId) {
                connectWebSocket();
            }
        }, 2000 * reconnectAttempts); // Exponential backoff
    } else {
        console.log('WebSocket connection failed, falling back to polling');
        startPolling(); // Fallback to polling
    }
}

// Disconnect WebSocket
function disconnectWebSocket() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect();
    }
    stompClient = null;
    websocketConnected = false;
    reconnectAttempts = 0;
}

// Start real-time updates (WebSocket with polling fallback)
function startPolling() {
    stopPolling(); // Ensure no multiple intervals
    
    // Try WebSocket first
    if (currentSessionId) {
        connectWebSocket();
        // If WebSocket fails, polling will start automatically
    }
    
    // Fallback polling (will only run if WebSocket is not connected)
    updateInterval = setInterval(async () => {
        if (!currentSessionId) {
            stopPolling();
            return;
        }
        
        // If WebSocket is connected, skip polling
        if (websocketConnected) {
            return;
        }
        
        try {
            const response = await fetch(`${API_BASE}/timer/${currentSessionId}`);
            if (response.ok) {
                const status = await response.json();
                updateTimerDisplay(status);
                
                if (status.status === 'COMPLETED') {
                    stopPolling();
                    showMessage('Block completed!', 'success');
                } else if (status.status === 'STOPPED') {
                    stopPolling();
                    resetTimerDisplay();
                }
            } else {
                stopPolling();
            }
        } catch (error) {
            console.error('Error polling timer:', error);
        }
    }, 1000); // Update every second
}

// Stop polling and WebSocket
function stopPolling() {
    disconnectWebSocket();
    if (updateInterval) {
        clearInterval(updateInterval);
        updateInterval = null;
    }
}

// Show message
function showMessage(text, type = 'success') {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;
    messageDiv.className = `message message-${type}`;
    messageDiv.style.display = 'block';
    
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

// Settings Panel Functions
function toggleSettings() {
    const settingsPanel = document.getElementById('settingsPanel');
    if (settingsPanel.style.display === 'none' || !settingsPanel.style.display) {
        openSettings();
    } else {
        closeSettings();
    }
}

function openSettings() {
    const settingsPanel = document.getElementById('settingsPanel');
    settingsPanel.style.display = 'block';
    settingsPanel.scrollIntoView({ behavior: 'smooth' });
    loadSoundSettings();
    updateNotificationStatus();
}

function closeSettings() {
    const settingsPanel = document.getElementById('settingsPanel');
    settingsPanel.style.display = 'none';
}

function loadSoundSettings() {
    const settings = audioManager.getSettings();
    
    document.getElementById('soundsEnabled').checked = settings.enabled !== false;
    document.getElementById('soundVolume').value = (settings.volume || 0.7) * 100;
    document.getElementById('volumeValue').textContent = Math.round((settings.volume || 0.7) * 100) + '%';
    document.getElementById('pomodoroSound').value = settings.pomodoroStart || 'default';
    document.getElementById('breakSound').value = settings.breakStart || 'default';
    document.getElementById('completeSound').value = settings.timerComplete || 'default';
    
    // Update volume display on change
    const volumeSlider = document.getElementById('soundVolume');
    volumeSlider.removeEventListener('input', updateVolumeDisplay);
    volumeSlider.addEventListener('input', updateVolumeDisplay);
}

function updateVolumeDisplay(e) {
    document.getElementById('volumeValue').textContent = e.target.value + '%';
}

function saveSoundSettings() {
    const settings = {
        enabled: document.getElementById('soundsEnabled').checked,
        volume: document.getElementById('soundVolume').value / 100,
        pomodoroStart: document.getElementById('pomodoroSound').value,
        breakStart: document.getElementById('breakSound').value,
        timerComplete: document.getElementById('completeSound').value
    };
    
    audioManager.updateSettings(settings);
    showMessage('Sound settings saved!', 'success');
}

function testSound(type) {
    if (type === 'pomodoro') {
        audioManager.playSound('POMODORO');
    } else if (type === 'break') {
        audioManager.playSound('SHORT_BREAK');
    } else if (type === 'complete') {
        audioManager.playSound('COMPLETED');
    }
}

async function requestNotificationPermission() {
    const granted = await notificationManager.requestPermission();
    updateNotificationStatus();
    
    if (granted) {
        showMessage('Notifications enabled!', 'success');
    } else {
        showMessage('Notification permission denied', 'error');
    }
}

function updateNotificationStatus() {
    const statusElement = document.getElementById('notificationStatus');
    const button = document.getElementById('requestNotificationBtn');
    
    if (!notificationManager.isSupported()) {
        statusElement.textContent = 'Status: Not supported in this browser';
        button.style.display = 'none';
        return;
    }
    
    const permission = notificationManager.getPermission();
    if (permission === 'granted') {
        statusElement.textContent = 'Status: Enabled';
        button.textContent = 'Notifications Enabled';
        button.disabled = true;
        button.style.opacity = '0.6';
    } else if (permission === 'denied') {
        statusElement.textContent = 'Status: Denied (check browser settings)';
        button.textContent = 'Permission Denied';
        button.disabled = true;
        button.style.opacity = '0.6';
    } else {
        statusElement.textContent = 'Status: Not enabled';
        button.textContent = 'Enable Notifications';
        button.disabled = false;
        button.style.opacity = '1';
    }
}
