// singleWindowValidator.js
(function () {
    'use strict';

    // Check if SingleWindowValidator is already defined
    if (typeof window !== 'undefined' && window.SingleWindowValidator) {
        console.log('SingleWindowValidator already exists, skipping redefinition');
        return;
    }

    class SingleWindowValidator {
        constructor() {
            // Configuration
            this.config = {
                storageKeys: {
                    activeWindowId: 'imas_active_window_id',
                    sessionToken: 'authToken',
                    userSession: 'userSession',
                    lastActiveTimestamp: 'imas_last_active'
                },
                heartbeatInterval: 2000, // Check every 2 seconds
                sessionTimeout: 30 * 60 * 1000, // 30 minutes
                loginUrl: '/login.html'
            };

            // Initialize properties
            this.windowId = this.generateWindowId();
            this.broadcastChannel = null;
            this.heartbeatTimer = null;
            this.isPrimaryWindow = false;

            // Initialize the validator
            this.initialize();
        }

        // Generate a unique window ID
        generateWindowId() {
            return 'window_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        }

        // Initialize the validator
        initialize() {
            // Check if running in a browser environment
            if (typeof window === 'undefined') {
                console.warn('SingleWindowValidator: Browser environment not detected');
                return;
            }

            // Initialize Broadcast Channel for inter-window communication
            if (typeof BroadcastChannel !== 'undefined') {
                this.broadcastChannel = new BroadcastChannel('imas_window_channel');
                this.setupBroadcastListeners();
            } else {
                console.warn('BroadcastChannel not supported, falling back to storage events');
            }

            // Set up storage event listener for localStorage changes
            window.addEventListener('storage', this.handleStorageChange.bind(this));

            // Check if this is the primary window
            this.checkPrimaryWindow();

            // Start heartbeat to maintain window exclusivity
            this.startHeartbeat();

            // Check session validity
            this.validateSession();

            // Handle page visibility changes
            document.addEventListener('visibilitychange', this.handleVisibilityChange.bind(this));

            // Handle window unload
            window.addEventListener('beforeunload', this.cleanup.bind(this));
        }

        // Set up Broadcast Channel listeners
        setupBroadcastListeners() {
            this.broadcastChannel.addEventListener('message', (event) => {
                const { type, windowId, timestamp } = event.data;

                switch (type) {
                    case 'HEARTBEAT':
                        if (windowId !== this.windowId && timestamp > (Date.now() - 5000)) {
                            // Another window is active, redirect to login
                            this.redirectToLogin('Another window is active');
                        }
                        break;
                    case 'WINDOW_CLOSED':
                        if (windowId !== this.windowId) {
                            this.checkPrimaryWindow();
                        }
                        break;
                }
            });
        }

        // Handle storage changes
        handleStorageChange(event) {
            if (event.key === this.config.storageKeys.activeWindowId) {
                const newWindowId = event.newValue;
                if (newWindowId && newWindowId !== this.windowId) {
                    // Another window has taken over
                    this.redirectToLogin('Another window has claimed the session');
                }
            }
        }

        // Check if this is the primary window
        checkPrimaryWindow() {
            const currentActiveWindowId = localStorage.getItem(this.config.storageKeys.activeWindowId);

            if (!currentActiveWindowId) {
                // No active window, claim it
                localStorage.setItem(this.config.storageKeys.activeWindowId, this.windowId);
                this.isPrimaryWindow = true;
                console.log(`Window ${this.windowId} is now the primary window`);
            } else if (currentActiveWindowId === this.windowId) {
                this.isPrimaryWindow = true;
            } else {
                // Another window is active
                this.redirectToLogin('Another window is already active');
            }
        }

        // Start heartbeat to maintain window exclusivity
        startHeartbeat() {
            this.heartbeatTimer = setInterval(() => {
                if (this.isPrimaryWindow) {
                    // Update last active timestamp
                    localStorage.setItem(this.config.storageKeys.lastActiveTimestamp, Date.now().toString());

                    // Send heartbeat to other windows
                    if (this.broadcastChannel) {
                        this.broadcastChannel.postMessage({
                            type: 'HEARTBEAT',
                            windowId: this.windowId,
                            timestamp: Date.now()
                        });
                    }
                }
            }, this.config.heartbeatInterval);
        }

        // Validate current session
        validateSession() {
            const sessionData = localStorage.getItem(this.config.storageKeys.userSession);
            const authToken = localStorage.getItem(this.config.storageKeys.sessionToken);
            const lastActive = localStorage.getItem(this.config.storageKeys.lastActiveTimestamp);

            if (!sessionData || !authToken) {
                this.redirectToLogin('No active session found');
                return;
            }

            if (lastActive) {
                const timeSinceLastActive = Date.now() - parseInt(lastActive);
                if (timeSinceLastActive > this.config.sessionTimeout) {
                    this.redirectToLogin('Session has expired');
                }
            }
        }

        // Handle visibility changes
        handleVisibilityChange() {
            if (document.visibilityState === 'visible' && this.isPrimaryWindow) {
                this.validateSession();
            }
        }

        // Redirect to login page
        redirectToLogin(reason) {
            if (window.location.pathname !== this.config.loginUrl) {
                console.warn(`Redirecting to login: ${reason}`);
                this.cleanup();

                // Show error message using the page's showMessage if available
                if (typeof showMessage === 'function') {
                    showMessage(reason, 'error');
                } else {
                    alert(reason);
                }

                // Clear session data
                localStorage.removeItem(this.config.storageKeys.sessionToken);
                localStorage.removeItem(this.config.storageKeys.userSession);

                // Redirect to login
                setTimeout(() => {
                    window.location.href = this.config.loginUrl;
                }, 1000);
            } else {
                // If already on login page, show the message
                if (typeof showMessage === 'function') {
                    showMessage(reason, 'error');
                } else {
                    alert(reason);
                }
            }
        }

        // Cleanup on window close
        cleanup() {
            if (this.isPrimaryWindow) {
                localStorage.removeItem(this.config.storageKeys.activeWindowId);
                if (this.broadcastChannel) {
                    this.broadcastChannel.postMessage({
                        type: 'WINDOW_CLOSED',
                        windowId: this.windowId
                    });
                }
            }
            if (this.heartbeatTimer) {
                clearInterval(this.heartbeatTimer);
            }
            if (this.broadcastChannel) {
                this.broadcastChannel.close();
            }
        }

        // Force single window mode
        enforceSingleWindow() {
            this.checkPrimaryWindow();
            this.validateSession();
        }
    }

    // Initialize on page load
    document.addEventListener('DOMContentLoaded', () => {
        const validator = new SingleWindowValidator();
        window.singleWindowValidator = validator;

        // Integrate with navigation
        document.querySelectorAll('a[href]').forEach(link => {
            link.addEventListener('click', (e) => {
                const href = link.getAttribute('href');
                if (href && !href.startsWith('#') && !href.includes('://')) {
                    e.preventDefault();
                    validator.validateSession();
                    if (validator.isPrimaryWindow) {
                        window.location.href = href;
                    }
                }
            });
        });
    });

    // Expose to global scope
    if (typeof window !== 'undefined') {
        window.SingleWindowValidator = SingleWindowValidator;
    }

    // Export for Node.js environments
    if (typeof module !== 'undefined' && module.exports) {
        module.exports = { SingleWindowValidator };
    }

    return SingleWindowValidator;
})();