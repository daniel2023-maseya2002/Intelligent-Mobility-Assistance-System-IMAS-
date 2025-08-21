// Enhanced SessionTracker - Complete Implementation
(function() {
    'use strict';

    // Check if SessionTracker is already defined to prevent redeclaration
    if (typeof window !== 'undefined' && window.SessionTracker) {
        console.log('SessionTracker already exists, skipping redefinition');
        return;
    }

    class SessionTracker {
        constructor() {
            this.storage = {
                loginHistory: 'loginHistory',
                activeSessions: 'activeSessions',
                failedAttempts: 'failedAttempts',
                staffPhotos: 'staffPhotos',
                sessionData: 'sessionData',
                sessionLog: 'sessionLog' // New storage key for enhanced logging
            };

            // Enhanced session tracking properties
            this.sessionLog = [];
            this.maxLogEntries = 100;

            this.initializeStorage();
            this.loadExistingLog();
            this.setupPeriodicCleanup();
        }

        initializeStorage() {
            const storageKeys = Object.values(this.storage);
            storageKeys.forEach(key => {
                if (!this.getStorageItem(key)) {
                    this.setStorageItem(key, JSON.stringify(key === this.storage.sessionData ? {} : []));
                }
            });
        }

        // Enhanced method to load existing session log
        loadExistingLog() {
            try {
                const existingLog = this.getStorageItem(this.storage.sessionLog);
                if (existingLog) {
                    this.sessionLog = JSON.parse(existingLog);
                    if (!Array.isArray(this.sessionLog)) {
                        this.sessionLog = [];
                    }
                } else {
                    this.sessionLog = [];
                }
            } catch (e) {
                console.error('Error loading session log:', e);
                this.sessionLog = [];
            }
        }

        // Enhanced recordLogin method combining both approaches
        recordLogin(userData, success) {
            try {
                if (!userData || (!userData.email && !userData.staffId)) {
                    console.error('Invalid user data provided - missing email or staffId');
                    return false;
                }

                const staffId = userData.staffId || userData.id || userData.email;
                const email = userData.email;
                const fullName = userData.fullName || `${userData.firstName} ${userData.lastName}` || email;
                const role = userData.role || 'Staff';
                const sessionId = success ? this.generateSessionId() : null;

                // Get current data from storage
                const loginHistory = this.getStorageData(this.storage.loginHistory);
                const activeSessions = this.getStorageData(this.storage.activeSessions);
                const failedAttempts = this.getStorageData(this.storage.failedAttempts);
                const staffPhotos = this.getStorageData(this.storage.staffPhotos);

                const loginRecord = {
                    timestamp: new Date().toISOString(),
                    staffId: staffId,
                    email: email,
                    fullName: fullName,
                    role: role,
                    success: success,
                    deviceInfo: this.getDeviceInfo(),
                    sessionId: sessionId
                };

                // Enhanced logging entry
                const enhancedLogEntry = {
                    timestamp: new Date().toISOString(),
                    email: email || 'unknown',
                    staffId: staffId,
                    fullName: fullName,
                    role: role,
                    success: success,
                    userAgent: navigator.userAgent,
                    sessionId: sessionId,
                    deviceInfo: this.getDeviceInfo()
                };

                // Add to login history
                loginHistory.push(loginRecord);
                this.saveStorageData(this.storage.loginHistory, loginHistory);

                // Add to enhanced session log
                this.sessionLog.push(enhancedLogEntry);

                // Trim log if too large
                if (this.sessionLog.length > this.maxLogEntries) {
                    this.sessionLog = this.sessionLog.slice(-this.maxLogEntries);
                }

                // Save enhanced session log
                this.setStorageItem(this.storage.sessionLog, JSON.stringify(this.sessionLog));

                if (success) {
                    // Handle successful login
                    this.handleSuccessfulLogin(userData, loginRecord, activeSessions, staffPhotos);
                } else {
                    // Handle failed login
                    this.handleFailedLogin(loginRecord, failedAttempts);
                }

                console.log('Login recorded successfully:', {
                    staffId: staffId,
                    email: email,
                    success: success,
                    timestamp: loginRecord.timestamp,
                    sessionId: sessionId
                });

                return enhancedLogEntry; // Return the enhanced log entry
            } catch (error) {
                console.error('Error recording login:', error);
                return false;
            }
        }

        handleSuccessfulLogin(userData, loginRecord, activeSessions, staffPhotos) {
            const staffId = userData.staffId || userData.id || userData.email;

            // Remove existing active session for this user (prevent duplicates)
            const filteredSessions = activeSessions.filter(session => session.staffId !== staffId);

            // Create new session record
            const sessionRecord = {
                staffId: staffId,
                email: userData.email,
                fullName: userData.fullName || `${userData.firstName} ${userData.lastName}` || userData.email,
                role: userData.role || 'Staff',
                loginTime: loginRecord.timestamp,
                sessionId: loginRecord.sessionId,
                deviceInfo: loginRecord.deviceInfo,
                lastActivity: new Date().toISOString()
            };

            filteredSessions.push(sessionRecord);
            this.saveStorageData(this.storage.activeSessions, filteredSessions);

            // Store staff photo if available
            if (userData.photo) {
                const photoData = {
                    staffId: staffId,
                    photo: userData.photo,
                    updatedAt: new Date().toISOString()
                };

                // Remove existing photo data for this staff
                const filteredPhotos = staffPhotos.filter(photo => photo.staffId !== staffId);
                filteredPhotos.push(photoData);
                this.saveStorageData(this.storage.staffPhotos, filteredPhotos);
            }

            // Update session data
            this.updateSessionData(staffId, {
                email: userData.email,
                fullName: sessionRecord.fullName,
                role: userData.role,
                photo: userData.photo,
                loginTime: loginRecord.timestamp,
                sessionId: loginRecord.sessionId
            });
        }

        handleFailedLogin(loginRecord, failedAttempts) {
            const failedAttempt = {
                timestamp: loginRecord.timestamp,
                staffId: loginRecord.staffId,
                email: loginRecord.email,
                fullName: loginRecord.fullName,
                role: loginRecord.role,
                deviceInfo: loginRecord.deviceInfo
            };

            failedAttempts.push(failedAttempt);
            this.saveStorageData(this.storage.failedAttempts, failedAttempts);
        }

        // Enhanced method to get recent attempts with better filtering
        getRecentAttempts(email, minutes = 5) {
            const cutoffTime = new Date(Date.now() - minutes * 60 * 1000);
            return this.sessionLog.filter(entry =>
                entry.email === email &&
                new Date(entry.timestamp) > cutoffTime
            );
        }

        // New method to get recent failed attempts specifically
        getRecentFailedAttempts(email, minutes = 5) {
            return this.getRecentAttempts(email, minutes).filter(entry => !entry.success);
        }

        // New method to get recent successful attempts
        getRecentSuccessfulAttempts(email, minutes = 5) {
            return this.getRecentAttempts(email, minutes).filter(entry => entry.success);
        }

        updateSessionData(staffId, sessionInfo) {
            try {
                const sessionData = this.getStorageData(this.storage.sessionData) || {};
                sessionData[staffId] = {
                    ...sessionInfo,
                    lastUpdated: new Date().toISOString()
                };
                this.saveStorageData(this.storage.sessionData, sessionData);
            } catch (error) {
                console.error('Error updating session data:', error);
            }
        }

        getStaffPhoto(staffId) {
            try {
                const staffPhotos = this.getStorageData(this.storage.staffPhotos);
                const photoData = staffPhotos.find(photo => photo.staffId === staffId);
                return photoData ? photoData.photo : null;
            } catch (error) {
                console.error('Error getting staff photo:', error);
                return null;
            }
        }

        getCurrentSession(staffId) {
            try {
                const activeSessions = this.getStorageData(this.storage.activeSessions);
                return activeSessions.find(session => session.staffId === staffId) || null;
            } catch (error) {
                console.error('Error getting current session:', error);
                return null;
            }
        }

        updateLastActivity(staffId) {
            try {
                const activeSessions = this.getStorageData(this.storage.activeSessions);
                const sessionIndex = activeSessions.findIndex(session => session.staffId === staffId);

                if (sessionIndex !== -1) {
                    activeSessions[sessionIndex].lastActivity = new Date().toISOString();
                    this.saveStorageData(this.storage.activeSessions, activeSessions);
                    return true;
                }
                return false;
            } catch (error) {
                console.error('Error updating last activity:', error);
                return false;
            }
        }

        endSession(staffId) {
            try {
                const activeSessions = this.getStorageData(this.storage.activeSessions);
                const filteredSessions = activeSessions.filter(session => session.staffId !== staffId);
                this.saveStorageData(this.storage.activeSessions, filteredSessions);

                // Also remove from session data
                const sessionData = this.getStorageData(this.storage.sessionData) || {};
                delete sessionData[staffId];
                this.saveStorageData(this.storage.sessionData, sessionData);

                console.log('Session ended for staff:', staffId);
                return true;
            } catch (error) {
                console.error('Error ending session:', error);
                return false;
            }
        }

        getLoginStats() {
            try {
                const loginHistory = this.getStorageData(this.storage.loginHistory);
                const activeSessions = this.getStorageData(this.storage.activeSessions);
                const failedAttempts = this.getStorageData(this.storage.failedAttempts);

                const today = new Date();
                today.setHours(0, 0, 0, 0);

                const successfulLogins = loginHistory.filter(login => login.success);
                const todaySuccessfulLogins = successfulLogins.filter(login =>
                    new Date(login.timestamp) >= today
                );

                return {
                    totalUsers: new Set(successfulLogins.map(login => login.staffId)).size,
                    todayLogins: todaySuccessfulLogins.length,
                    activeSessions: activeSessions.length,
                    failedAttempts: failedAttempts.length,
                    totalLogins: loginHistory.length,
                    successfulLogins: successfulLogins.length,
                    roleDistribution: this.calculateRoleDistribution(successfulLogins),
                    recentActivity: this.getRecentActivity(loginHistory, 10),
                    todayFailedAttempts: failedAttempts.filter(attempt =>
                        new Date(attempt.timestamp) >= today
                    ).length,
                    enhancedLogEntries: this.sessionLog.length
                };
            } catch (error) {
                console.error('Error getting login stats:', error);
                return {
                    totalUsers: 0,
                    todayLogins: 0,
                    activeSessions: 0,
                    failedAttempts: 0,
                    totalLogins: 0,
                    successfulLogins: 0,
                    roleDistribution: {},
                    recentActivity: [],
                    todayFailedAttempts: 0,
                    enhancedLogEntries: 0
                };
            }
        }

        calculateRoleDistribution(loginHistory) {
            return loginHistory.reduce((acc, login) => {
                if (login.role) {
                    acc[login.role] = (acc[login.role] || 0) + 1;
                }
                return acc;
            }, {});
        }

        getRecentActivity(loginHistory, limit = 10) {
            return loginHistory
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                .slice(0, limit);
        }

        getActiveSessions() {
            return this.getStorageData(this.storage.activeSessions);
        }

        getLoginHistory(limit = 50) {
            const loginHistory = this.getStorageData(this.storage.loginHistory);
            return loginHistory
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                .slice(0, limit);
        }

        getFailedAttempts(limit = 20) {
            const failedAttempts = this.getStorageData(this.storage.failedAttempts);
            return failedAttempts
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                .slice(0, limit);
        }

        // Enhanced method to get session log with filtering options
        getSessionLog(limit = 50, filterOptions = {}) {
            let filteredLog = [...this.sessionLog];

            // Apply filters
            if (filterOptions.email) {
                filteredLog = filteredLog.filter(entry => entry.email === filterOptions.email);
            }
            if (filterOptions.success !== undefined) {
                filteredLog = filteredLog.filter(entry => entry.success === filterOptions.success);
            }
            if (filterOptions.fromDate) {
                const fromDate = new Date(filterOptions.fromDate);
                filteredLog = filteredLog.filter(entry => new Date(entry.timestamp) >= fromDate);
            }
            if (filterOptions.toDate) {
                const toDate = new Date(filterOptions.toDate);
                filteredLog = filteredLog.filter(entry => new Date(entry.timestamp) <= toDate);
            }

            return filteredLog
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                .slice(0, limit);
        }

        getDeviceInfo() {
            return {
                userAgent: navigator.userAgent,
                platform: navigator.platform,
                language: navigator.language,
                screenResolution: `${screen.width}x${screen.height}`,
                timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
                cookieEnabled: navigator.cookieEnabled
            };
        }

        generateSessionId() {
            const timestamp = Date.now();
            const random = Math.random().toString(36).substr(2, 9);
            return `session_${timestamp}_${random}`;
        }

        setupPeriodicCleanup() {
            // Clean expired sessions every 10 minutes
            setInterval(() => {
                this.clearExpiredSessions();
                this.cleanOldData();
            }, 10 * 60 * 1000);
        }

        clearExpiredSessions(maxAge = 24 * 60 * 60 * 1000) { // 24 hours
            try {
                const activeSessions = this.getStorageData(this.storage.activeSessions);
                const now = new Date();

                const validSessions = activeSessions.filter(session => {
                    const lastActivity = new Date(session.lastActivity || session.loginTime);
                    const sessionAge = now - lastActivity;
                    return sessionAge < maxAge;
                });

                if (validSessions.length !== activeSessions.length) {
                    this.saveStorageData(this.storage.activeSessions, validSessions);
                    console.log(`Cleaned ${activeSessions.length - validSessions.length} expired sessions`);
                }
            } catch (error) {
                console.error('Error clearing expired sessions:', error);
            }
        }

        cleanOldData(maxAge = 30 * 24 * 60 * 60 * 1000) { // 30 days
            try {
                const now = new Date();

                // Clean old login history
                const loginHistory = this.getStorageData(this.storage.loginHistory);
                const recentLoginHistory = loginHistory.filter(login => {
                    const loginDate = new Date(login.timestamp);
                    return (now - loginDate) < maxAge;
                });

                if (recentLoginHistory.length !== loginHistory.length) {
                    this.saveStorageData(this.storage.loginHistory, recentLoginHistory);
                }

                // Clean old failed attempts
                const failedAttempts = this.getStorageData(this.storage.failedAttempts);
                const recentFailedAttempts = failedAttempts.filter(attempt => {
                    const attemptDate = new Date(attempt.timestamp);
                    return (now - attemptDate) < maxAge;
                });

                if (recentFailedAttempts.length !== failedAttempts.length) {
                    this.saveStorageData(this.storage.failedAttempts, recentFailedAttempts);
                }

                // Clean old session log
                const recentSessionLog = this.sessionLog.filter(entry => {
                    const entryDate = new Date(entry.timestamp);
                    return (now - entryDate) < maxAge;
                });

                if (recentSessionLog.length !== this.sessionLog.length) {
                    this.sessionLog = recentSessionLog;
                    this.setStorageItem(this.storage.sessionLog, JSON.stringify(this.sessionLog));
                }
            } catch (error) {
                console.error('Error cleaning old data:', error);
            }
        }

        // Safe storage methods to handle memory storage fallback
        getStorageItem(key) {
            try {
                return localStorage.getItem(key);
            } catch (error) {
                console.warn('localStorage not available, using memory storage');
                return this.memoryStorage && this.memoryStorage[key];
            }
        }

        setStorageItem(key, value) {
            try {
                localStorage.setItem(key, value);
            } catch (error) {
                console.warn('localStorage not available, using memory storage');
                if (!this.memoryStorage) this.memoryStorage = {};
                this.memoryStorage[key] = value;
            }
        }

        getStorageData(key) {
            try {
                const data = this.getStorageItem(key);
                return data ? JSON.parse(data) : (key === this.storage.sessionData ? {} : []);
            } catch (error) {
                console.error(`Error parsing storage data for key ${key}:`, error);
                return key === this.storage.sessionData ? {} : [];
            }
        }

        saveStorageData(key, data) {
            try {
                this.setStorageItem(key, JSON.stringify(data));
            } catch (error) {
                console.error(`Error saving storage data for key ${key}:`, error);
            }
        }

        clearAllData() {
            Object.values(this.storage).forEach(key => {
                try {
                    localStorage.removeItem(key);
                } catch (error) {
                    if (this.memoryStorage) {
                        delete this.memoryStorage[key];
                    }
                }
            });
            this.sessionLog = [];
            this.initializeStorage();
            console.log('All session tracking data cleared');
        }

        exportData() {
            const data = {};
            Object.entries(this.storage).forEach(([name, key]) => {
                data[name] = this.getStorageData(key);
            });
            return data;
        }

        importData(data) {
            try {
                Object.entries(data).forEach(([name, value]) => {
                    if (this.storage[name]) {
                        this.saveStorageData(this.storage[name], value);
                    }
                });

                // Reload session log after import
                this.loadExistingLog();

                console.log('Data imported successfully');
                return true;
            } catch (error) {
                console.error('Error importing data:', error);
                return false;
            }
        }
    }

    // Expose SessionTracker to the global scope (browser environment)
    if (typeof window !== 'undefined') {
        window.SessionTracker = SessionTracker;

        // Initialize global instance if it doesn't exist
        if (!window.sessionTracker) {
            window.sessionTracker = new SessionTracker();
            console.log('SessionTracker initialized successfully');
        }
    }

    // Export for Node.js environments
    if (typeof module !== 'undefined' && module.exports) {
        module.exports = { SessionTracker };
    }

    // Return the class for other module systems
    return SessionTracker;

})();