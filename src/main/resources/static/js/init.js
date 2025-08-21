// init.js - Combine this with your HTML or include as a separate file
document.addEventListener('DOMContentLoaded', function() {
    // Initialize SessionTracker first
    try {
        if (typeof SessionTracker !== 'undefined') {
            window.sessionTracker = new SessionTracker();
            console.log('SessionTracker initialized successfully');
        } else {
            console.error('SessionTracker class is not defined');
        }
    } catch (error) {
        console.error('Failed to initialize SessionTracker:', error);
    }

    // Initialize UserSessionHandler next
    try {
        if (typeof UserSessionHandler !== 'undefined') {
            window.userSessionHandler = UserSessionHandler.getInstance();
            console.log('UserSessionHandler initialized successfully');
        } else {
            console.error('UserSessionHandler class is not defined');
        }
    } catch (error) {
        console.error('Failed to initialize UserSessionHandler:', error);
    }

    // Try to initialize SuperAdminDashboard only if it exists
    try {
        if (typeof SuperAdminDashboard !== 'undefined') {
            window.dashboard = new SuperAdminDashboard();
            console.log('SuperAdminDashboard initialized successfully');
        }
    } catch (error) {
        console.error('Failed to initialize SuperAdminDashboard:', error);
    }

    // Check if user is authenticated (for pages that require login)
    try {
        if (window.userSessionHandler &&
            !window.location.pathname.includes('login.html') &&
            !window.userSessionHandler.isAuthenticated()) {
            console.log('User not authenticated, redirecting to login page');
            window.location.href = 'login.html';
            return;
        }
    } catch (error) {
        console.error('Authentication check failed:', error);
    }

    // Update UI with profile information if available
    try {
        if (window.userSessionHandler) {
            window.userSessionHandler.updateProfileUI();
        }
    } catch (error) {
        console.error('Failed to update profile UI:', error);
    }

    // Setup logout button functionality
    const logoutBtn = document.querySelector('.logout-btn');
    if (logoutBtn && window.userSessionHandler) {
        logoutBtn.addEventListener('click', () => {
            window.userSessionHandler.clearSession();
            window.location.href = 'login.html';
        });
    }
});