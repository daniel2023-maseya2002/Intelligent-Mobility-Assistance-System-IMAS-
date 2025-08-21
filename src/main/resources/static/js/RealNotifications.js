// Système de notifications en temps réel

// Variables globales pour les notifications
let notificationPollingInterval;
let lastNotificationCheck = new Date();
let currentNotificationCount = 0;

// Initialiser le système de notifications
function initializeNotifications() {
    console.log('🔔 Initializing real-time notifications system...');

    if (currentUser && currentUser.id) {
        // Charger les notifications initiales
        loadNotifications();

        // Démarrer le polling pour les nouvelles notifications
        startNotificationPolling();

        // Écouter les events de focus pour rafraîchir
        window.addEventListener('focus', () => {
            console.log('🔄 Window focused, checking for new notifications...');
            checkForNewNotifications();
        });

        console.log('✅ Notification system initialized for user:', currentUser.id);
    } else {
        console.warn('⚠️ Cannot initialize notifications: No current user');
    }
}

// Démarrer le polling des notifications
function startNotificationPolling() {
    // Arrêter tout polling existant
    if (notificationPollingInterval) {
        clearInterval(notificationPollingInterval);
    }

    // Polling toutes les 30 secondes
    notificationPollingInterval = setInterval(() => {
        checkForNewNotifications();
    }, 30000);

    console.log('🔄 Started notification polling (every 30 seconds)');
}

// Arrêter le polling des notifications
function stopNotificationPolling() {
    if (notificationPollingInterval) {
        clearInterval(notificationPollingInterval);
        notificationPollingInterval = null;
        console.log('⏹️ Stopped notification polling');
    }
}

// Vérifier les nouvelles notifications
async function checkForNewNotifications() {
    if (!currentUser || !currentUser.id) return;

    try {
        const response = await fetch(`${API_BASE}/notifications/user/${currentUser.id}/unread`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const newNotifications = await response.json();

            // Filtrer les notifications vraiment nouvelles
            const trulyNewNotifications = newNotifications.filter(notif => {
                const notifTime = new Date(notif.timestamp || notif.time);
                return notifTime > lastNotificationCheck;
            });

            if (trulyNewNotifications.length > 0) {
                console.log(`🔔 Found ${trulyNewNotifications.length} new notifications`);

                // Mettre à jour le badge
                updateNotificationBadge(newNotifications.length);

                // Afficher les nouvelles notifications
                trulyNewNotifications.forEach(notification => {
                    showRealtimeNotification(notification);
                    addNotificationToList(notification);
                });

                // Jouer un son si l'utilisateur le souhaite
                playNotificationSound();
            }

            // Mettre à jour le badge avec le total
            updateNotificationBadge(newNotifications.length);
            lastNotificationCheck = new Date();

        } else {
            console.warn('❌ Failed to check notifications:', response.status);
        }
    } catch (error) {
        console.error('❌ Error checking for new notifications:', error);
    }
}

// Afficher une notification en temps réel
function showRealtimeNotification(notification) {
    // Créer l'élément de notification
    const notifElement = document.createElement('div');
    notifElement.className = 'realtime-notification';
    notifElement.innerHTML = `
        <div class="realtime-notification-content">
            <div class="realtime-notification-header">
                <i class="${getNotificationIcon(notification.type)}"></i>
                <span class="realtime-notification-title">${getNotificationTitle(notification.type)}</span>
                <button class="realtime-notification-close" onclick="this.parentElement.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="realtime-notification-message">${notification.message}</div>
            <div class="realtime-notification-time">${formatTimeAgo(new Date(notification.timestamp || notification.time))}</div>
        </div>
    `;

    // Ajouter les styles si ils n'existent pas
    addNotificationStyles();

    // Ajouter au container de notifications
    let container = document.getElementById('realtimeNotificationContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'realtimeNotificationContainer';
        container.className = 'realtime-notification-container';
        document.body.appendChild(container);
    }

    container.appendChild(notifElement);

    // Animation d'entrée
    setTimeout(() => {
        notifElement.style.transform = 'translateX(0)';
        notifElement.style.opacity = '1';
    }, 100);

    // Auto-suppression après 8 secondes
    setTimeout(() => {
        if (notifElement.parentElement) {
            notifElement.style.transform = 'translateX(100%)';
            notifElement.style.opacity = '0';
            setTimeout(() => {
                if (notifElement.parentElement) {
                    notifElement.remove();
                }
            }, 300);
        }
    }, 8000);

    // Rendre cliquable pour aller aux notifications
    notifElement.addEventListener('click', () => {
        showSection('notifications');
        notifElement.remove();
    });
}

// Ajouter les styles pour les notifications en temps réel
function addNotificationStyles() {
    if (document.getElementById('realtimeNotificationStyles')) return;

    const styles = document.createElement('style');
    styles.id = 'realtimeNotificationStyles';
    styles.textContent = `
        .realtime-notification-container {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            pointer-events: none;
        }

        .realtime-notification {
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            margin-bottom: 12px;
            max-width: 350px;
            min-width: 280px;
            transform: translateX(100%);
            opacity: 0;
            transition: all 0.3s ease;
            pointer-events: all;
            cursor: pointer;
            border-left: 4px solid var(--primary-blue);
            overflow: hidden;
            position: relative;
        }

        .realtime-notification:hover {
            box-shadow: 0 6px 25px rgba(0, 0, 0, 0.2);
            transform: translateX(-5px) !important;
        }

        .realtime-notification-content {
            padding: 12px 16px;
        }

        .realtime-notification-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 6px;
        }

        .realtime-notification-header i {
            color: var(--primary-blue);
            font-size: 16px;
        }

        .realtime-notification-title {
            font-weight: 600;
            color: var(--dark);
            font-size: 14px;
            flex: 1;
        }

        .realtime-notification-close {
            background: none;
            border: none;
            color: var(--gray-medium);
            cursor: pointer;
            padding: 2px;
            border-radius: 4px;
            transition: all 0.2s ease;
        }

        .realtime-notification-close:hover {
            background: var(--gray-light);
            color: var(--dark);
        }

        .realtime-notification-message {
            color: var(--gray-dark);
            font-size: 13px;
            line-height: 1.4;
            margin-bottom: 6px;
        }

        .realtime-notification-time {
            color: var(--gray-medium);
            font-size: 11px;
        }

        .realtime-notification[data-type="TICKET_PURCHASE"] {
            border-left-color: var(--green);
        }

        .realtime-notification[data-type="DEPARTURE_REMINDER"] {
            border-left-color: var(--gold);
        }

        .realtime-notification[data-type="BOARDING_SUCCESS"] {
            border-left-color: var(--green);
        }

        .realtime-notification[data-type="error"] {
            border-left-color: var(--secondary-red);
        }
    `;

    document.head.appendChild(styles);
}

// Obtenir l'icône pour un type de notification
function getNotificationIcon(type) {
    const icons = {
        'TICKET_PURCHASE': 'fas fa-ticket-alt',
        'DEPARTURE_REMINDER': 'fas fa-clock',
        'BOARDING_SUCCESS': 'fas fa-bus',
        'PASSENGER_BOARDED': 'fas fa-user-check',
        'error': 'fas fa-exclamation-triangle',
        'system': 'fas fa-cog'
    };
    return icons[type] || 'fas fa-bell';
}

// Obtenir le titre pour un type de notification
function getNotificationTitle(type) {
    const titles = {
        'TICKET_PURCHASE': 'Ticket Purchased',
        'DEPARTURE_REMINDER': 'Departure Reminder',
        'BOARDING_SUCCESS': 'Boarding Confirmed',
        'PASSENGER_BOARDED': 'Passenger Update',
        'error': 'Alert',
        'system': 'System'
    };
    return titles[type] || 'Notification';
}

// Jouer un son de notification
function playNotificationSound() {
    // Son simple avec Web Audio API
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
        oscillator.frequency.setValueAtTime(600, audioContext.currentTime + 0.1);

        gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3);

        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.3);
    } catch (error) {
        console.log('Could not play notification sound:', error);
    }
}

// Ajouter une notification à la liste
function addNotificationToList(notification) {
    notification.id = notification.id || `notif_${Date.now()}_${Math.random()}`;
    notification.read = false;

    // Ajouter au début de la liste
    notifications.unshift(notification);

    // Garder seulement les 50 dernières
    if (notifications.length > 50) {
        notifications = notifications.slice(0, 50);
    }

    // Rafraîchir l'affichage si on est sur la page notifications
    if (document.getElementById('notificationsList')) {
        displayNotifications();
    }
}

// Mettre à jour le badge de notification
function updateNotificationBadge(count = null) {
    const badge = document.getElementById('notificationCount');
    if (!badge) return;

    if (count === null) {
        count = notifications.filter(n => !n.read).length;
    }

    currentNotificationCount = count;
    badge.textContent = count;
    badge.style.display = count > 0 ? 'flex' : 'none';

    // Animation du badge si nouvelle notification
    if (count > 0) {
        badge.style.transform = 'scale(1.3)';
        setTimeout(() => {
            badge.style.transform = 'scale(1)';
        }, 200);
    }
}

// Fonction appelée quand on achète un ticket (à ajouter après createTicket)
function onTicketPurchased(ticket) {
    console.log('🎫 Ticket purchased, checking for notifications...');

    // Attendre un peu pour que le backend ait le temps de créer la notification
    setTimeout(() => {
        checkForNewNotifications();
    }, 2000);
}

// Fonction appelée quand on scan un QR code (à ajouter après scanQrCode)
function onTicketScanned(ticket) {
    console.log('📱 Ticket scanned, checking for notifications...');

    setTimeout(() => {
        checkForNewNotifications();
    }, 1000);
}

// Nettoyer lors de la déconnexion
function cleanupNotifications() {
    stopNotificationPolling();

    const container = document.getElementById('realtimeNotificationContainer');
    if (container) {
        container.remove();
    }

    console.log('🧹 Notification system cleaned up');
}

// Initialiser automatiquement quand la page se charge
document.addEventListener('DOMContentLoaded', () => {
    // Attendre que currentUser soit défini
    const checkUser = setInterval(() => {
        if (currentUser && currentUser.id) {
            clearInterval(checkUser);
            initializeNotifications();
        }
    }, 1000);

    // Timeout après 10 secondes
    setTimeout(() => {
        clearInterval(checkUser);
    }, 10000);
});

// Nettoyer quand la page se décharge
window.addEventListener('beforeunload', cleanupNotifications);