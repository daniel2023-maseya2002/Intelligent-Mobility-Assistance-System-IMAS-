// ============================================================================
// PDF & FILE UPLOAD SYSTEM - COMPLETE VERSION
// ============================================================================

// Global variables
let currentPdfDoc = null;
let currentPageNum = 1;
let currentZoom = 1.0;
let currentChatPartner = null;
let currentUploadXHR = null;

// ============================================================================
// ELEMENT REFERENCES
// ============================================================================
const emojiBtn = document.getElementById('emoji-btn');
const attachBtn = document.getElementById('attach-btn');
const emojiPicker = document.getElementById('emoji-picker');
const fileUploadModal = document.getElementById('file-upload-modal');
const fileUploadInput = document.getElementById('file-upload-input');
// ============================================================================
// IMPROVED SESSION HANDLER
// ============================================================================
const AuthenticationManager = {
    getAuthToken: function() {
        try {
            // Priority 1: UserSessionHandler if available
            if (typeof window.UserSessionHandler !== 'undefined') {
                const sessionHandler = window.UserSessionHandler.getInstance();
                const token = sessionHandler.getAuthToken();
                if (token) {
                    console.log('Token retrieved via UserSessionHandler');
                    return token;
                }
            }

            // Priority 2: localStorage with different keys
            const tokenKeys = ['jwt_token', 'authToken', 'token', 'access_token'];
            for (const key of tokenKeys) {
                const token = localStorage.getItem(key);
                if (token) {
                    console.log(`Token retrieved via localStorage.${key}`);
                    return token;
                }
            }

            // Priority 3: sessionStorage
            for (const key of tokenKeys) {
                try {
                    const token = sessionStorage.getItem(key);
                    if (token) {
                        console.log(`Token retrieved via sessionStorage.${key}`);
                        return token;
                    }
                } catch (e) {
                    // sessionStorage might not be available
                }
            }

            console.warn('No authentication token found');
            return null;
        } catch (error) {
            console.error('Error retrieving token:', error);
            return null;
        }
    },

    isAuthenticated: function() {
        const token = this.getAuthToken();
        if (!token) {
            console.log('No token - user not authenticated');
            return false;
        }

        // If UserSessionHandler is available, use its method
        if (typeof window.UserSessionHandler !== 'undefined') {
            try {
                const sessionHandler = window.UserSessionHandler.getInstance();
                const isAuth = sessionHandler.isAuthenticated();
                console.log('Authentication status via UserSessionHandler:', isAuth);
                return isAuth;
            } catch (error) {
                console.warn('Error with UserSessionHandler:', error);
            }
        }

        // Fallback: consider authenticated if token exists
        console.log('Token found - considered authenticated');
        return true;
    },

    getCurrentUser: function() {
        try {
            // Priority 1: UserSessionHandler
            if (typeof window.UserSessionHandler !== 'undefined') {
                const sessionHandler = window.UserSessionHandler.getInstance();
                const user = sessionHandler.getCurrentUser();
                if (user) {
                    console.log('User retrieved via UserSessionHandler:', user);
                    return user;
                }
            }

            // Priority 2: localStorage with different keys
            const userKeys = ['staff_info', 'userSession', 'user_data', 'currentUser'];
            for (const key of userKeys) {
                try {
                    const userData = localStorage.getItem(key);
                    if (userData) {
                        const parsed = JSON.parse(userData);
                        if (parsed && (parsed.email || parsed.staffId || parsed.id)) {
                            console.log(`User retrieved via localStorage.${key}:`, parsed);
                            return parsed;
                        }
                    }
                } catch (e) {
                    console.warn(`Error parsing ${key}:`, e);
                }
            }

            console.warn('No user information found');
            return null;
        } catch (error) {
            console.error('Error retrieving user:', error);
            return null;
        }
    },

    refreshToken: async function() {
        try {
            if (typeof window.UserSessionHandler !== 'undefined') {
                const sessionHandler = window.UserSessionHandler.getInstance();
                if (typeof sessionHandler.refreshSession === 'function') {
                    const success = await sessionHandler.refreshSession();
                    console.log('Token refresh attempt:', success);
                    return success;
                }
            }
            return false;
        } catch (error) {
            console.error('Error refreshing token:', error);
            return false;
        }
    },

    clearSession: function() {
        try {
            // Clear all possible token storage
            const tokenKeys = ['jwt_token', 'authToken', 'token', 'access_token'];
            tokenKeys.forEach(key => {
                localStorage.removeItem(key);
                sessionStorage.removeItem(key);
            });

            // Clear user data
            const userKeys = ['staff_info', 'userSession', 'user_data', 'currentUser'];
            userKeys.forEach(key => {
                localStorage.removeItem(key);
                sessionStorage.removeItem(key);
            });

            // Call UserSessionHandler if available
            if (typeof window.UserSessionHandler !== 'undefined') {
                const sessionHandler = window.UserSessionHandler.getInstance();
                if (typeof sessionHandler.clearSession === 'function') {
                    sessionHandler.clearSession();
                }
            }

            console.log('Session cleared');
        } catch (error) {
            console.error('Error clearing session:', error);
        }
    }
};

// ============================================================================
// CHAT PARTNER DETECTION
// ============================================================================
const ChatPartnerManager = {
    getCurrentChatPartner: function() {
        if (currentChatPartner) {
            return currentChatPartner;
        }

        // Method 1: URL parameters
        const urlParams = new URLSearchParams(window.location.search);
        const chatId = urlParams.get('chat') || urlParams.get('chatId') ||
                      urlParams.get('id') || urlParams.get('recipient');

        if (chatId) {
            console.log('Chat partner detected via URL:', chatId);
            return { id: chatId, email: chatId, source: 'url' };
        }

        // Method 2: URL path
        const pathParts = window.location.pathname.split('/');
        const chatIndex = pathParts.indexOf('chat');
        if (chatIndex !== -1 && pathParts[chatIndex + 1]) {
            const partnerId = pathParts[chatIndex + 1];
            console.log('Chat partner detected via path:', partnerId);
            return { id: partnerId, email: partnerId, source: 'path' };
        }

        // Method 3: DOM elements with data attributes
        const selectors = [
            '[data-chat-id]',
            '[data-recipient-id]',
            '[data-user-id]',
            '[data-partner-id]',
            '.chat-header [data-id]',
            '.conversation-header [data-id]',
            '.active-chat [data-id]'
        ];

        for (const selector of selectors) {
            const element = document.querySelector(selector);
            if (element) {
                const id = element.getAttribute('data-chat-id') ||
                          element.getAttribute('data-recipient-id') ||
                          element.getAttribute('data-user-id') ||
                          element.getAttribute('data-partner-id') ||
                          element.getAttribute('data-id');
                if (id) {
                    console.log('Chat partner detected via DOM:', id);
                    return { id: id, email: id, source: 'dom' };
                }
            }
        }

        // Method 4: Header text
        const headerSelectors = [
            '.chat-header h1',
            '.chat-header h2',
            '.chat-header .title',
            '.conversation-header .name',
            '.chat-title',
            '.active-chat .name'
        ];

        for (const selector of headerSelectors) {
            const element = document.querySelector(selector);
            if (element && element.textContent.trim()) {
                const name = element.textContent.trim();
                console.log('Chat partner detected via header:', name);
                return { id: name, email: name, name: name, source: 'header' };
            }
        }

        // Method 5: Global variables
        const globalVars = ['currentChat', 'activeChat', 'selectedContact', 'chatPartner'];
        for (const varName of globalVars) {
            if (typeof window[varName] !== 'undefined' && window[varName]) {
                console.log('Chat partner detected via global variable:', varName);
                return window[varName];
            }
        }

        // Method 6: Current user as fallback
        const currentUser = AuthenticationManager.getCurrentUser();
        if (currentUser) {
            console.log('Using current user as chat partner');
            return {
                id: currentUser.staffId || currentUser.email || currentUser.id,
                email: currentUser.email,
                name: currentUser.fullName,
                source: 'currentUser'
            };
        }

        console.warn('No chat partner detected');
        return null;
    },

    setCurrentChatPartner: function(partner) {
        if (partner && (partner.id || partner.email)) {
            currentChatPartner = partner;
            console.log('Chat partner set:', partner);
        } else {
            console.warn('Invalid chat partner:', partner);
        }
    },

    ensureChatPartner: function() {
        if (!currentChatPartner) {
            currentChatPartner = this.getCurrentChatPartner();
        }
        return currentChatPartner;
    }
};

// ============================================================================
// NOTIFICATION SYSTEM
// ============================================================================
const NotificationManager = {
    showError: function(message) {
        console.error('Error:', message);
        this.createNotification(message, 'error');
    },

    showSuccess: function(message) {
        console.log('Success:', message);
        this.createNotification(message, 'success');
    },

    showInfo: function(message) {
        console.log('Info:', message);
        this.createNotification(message, 'info');
    },

    showWarning: function(message) {
        console.warn('Warning:', message);
        this.createNotification(message, 'warning');
    },

    createNotification: function(message, type) {
        // Try to use existing notification systems
        if (typeof showNotification === 'function') {
            showNotification(message, type);
            return;
        }

        if (typeof toastr !== 'undefined') {
            toastr[type](message);
            return;
        }

        // Create custom notification
        this.createCustomNotification(message, type);
    },

    createCustomNotification: function(message, type) {
        // Remove existing notifications
        const existing = document.querySelectorAll('.pfs-notification');
        existing.forEach(n => n.remove());

        const notification = document.createElement('div');
        notification.className = `pfs-notification pfs-notification-${type}`;

        const colors = {
            error: '#dc3545',
            success: '#28a745',
            info: '#17a2b8',
            warning: '#ffc107'
        };

        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 25px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            max-width: 400px;
            word-wrap: break-word;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            background-color: ${colors[type] || '#6c757d'};
            animation: slideIn 0.3s ease-out;
        `;

        // Add CSS animation
        if (!document.getElementById('pfs-notification-styles')) {
            const style = document.createElement('style');
            style.id = 'pfs-notification-styles';
            style.textContent = `
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes slideOut {
                    from { transform: translateX(0); opacity: 1; }
                    to { transform: translateX(100%); opacity: 0; }
                }
            `;
            document.head.appendChild(style);
        }

        notification.textContent = message;
        document.body.appendChild(notification);

        // Auto-remove
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 5000);
    }
};

// ============================================================================
// FILE VALIDATION UTILITIES
// ============================================================================
const FileManager = {
    validateFile: function(file) {
        if (!file) {
            throw new Error('No file selected');
        }

        // Max size (25MB for flexibility)
        const maxSize = 25 * 1024 * 1024;
        if (file.size > maxSize) {
            throw new Error(`File size exceeds limit of ${this.formatFileSize(maxSize)}`);
        }

        // Allowed file types
        const validTypes = [
            'application/pdf',
            'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'application/vnd.ms-excel',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'application/vnd.ms-powerpoint',
            'application/vnd.openxmlformats-officedocument.presentationml.presentation',
            'text/plain',
            'text/csv',
            'application/json',
            'application/xml',
            'image/jpeg',
            'image/jpg',
            'image/png',
            'image/gif',
            'image/webp',
            'image/bmp',
            'image/svg+xml',
            'video/mp4',
            'video/webm',
            'video/ogg',
            'video/avi',
            'video/mov',
            'audio/mp3',
            'audio/wav',
            'audio/ogg',
            'audio/m4a',
            'application/zip',
            'application/x-rar-compressed'
        ];

        if (!validTypes.includes(file.type)) {
            throw new Error(`File type not allowed: ${file.type}. Allowed types: PDF, Word, Excel, PowerPoint, images, videos, audio, archives.`);
        }

        return true;
    },

    formatFileSize: function(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    getFileIcon: function(fileType) {
        const iconMap = {
            'application/pdf': '📄',
            'application/msword': '📝',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '📝',
            'application/vnd.ms-excel': '📊',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '📊',
            'text/plain': '📄',
            'text/csv': '📊',
            'image/jpeg': '🖼️',
            'image/jpg': '🖼️',
            'image/png': '🖼️',
            'image/gif': '🖼️',
            'video/mp4': '🎥',
            'video/webm': '🎥',
            'audio/mp3': '🎵',
            'audio/wav': '🎵'
        };
        return iconMap[fileType] || '📎';
    }
};




// ============================================================================
// EMOJI PICKER FUNCTIONS
// ============================================================================
function toggleEmojiPicker() {
    if (!emojiPicker) return;
    emojiPicker.style.display = emojiPicker.style.display === 'none' ? 'block' : 'none';
}

function insertEmoji(emoji) {
    const input = document.getElementById('message-input');
    if (!input) return;

    const start = input.selectionStart;
    const end = input.selectionEnd;

    input.value = input.value.substring(0, start) + emoji + input.value.substring(end);
    input.focus();
    input.setSelectionRange(start + emoji.length, start + emoji.length);

    // Close picker after selection
    if (emojiPicker) {
        emojiPicker.style.display = 'none';
    }
}

// ============================================================================
// FILE UPLOAD FUNCTIONS
// ============================================================================
function openFileUploadModal() {
    if (fileUploadModal) {
        fileUploadModal.style.display = 'block';
    }
}

function closeFileUploadModal() {
    if (fileUploadModal) {
        fileUploadModal.style.display = 'none';
    }
}

function openFileSelector(type) {
    if (!fileUploadInput) return;

    switch(type) {
        case 'image':
            fileUploadInput.accept = 'image/*';
            break;
        case 'video':
            fileUploadInput.accept = 'video/*';
            break;
        case 'document':
            fileUploadInput.accept = '.pdf,.doc,.docx,.txt';
            break;
        default:
            fileUploadInput.accept = '*';
    }

    fileUploadInput.onchange = function(e) {
        if (e.target.files && e.target.files.length > 0) {
            FileUploadManager.handleFileUpload(e.target.files[0]);
            closeFileUploadModal();
        }
    };

    fileUploadInput.click();
}


// ============================================================================
// EVENT LISTENERS
// ============================================================================
document.addEventListener('DOMContentLoaded', function() {
    // Emoji picker
    if (emojiBtn) {
        emojiBtn.addEventListener('click', toggleEmojiPicker);
    }

    // Close emoji picker when clicking outside
    document.addEventListener('click', (e) => {
        if (emojiPicker && emojiPicker.style.display !== 'none') {
            if (emojiBtn && !emojiBtn.contains(e.target) &&
                emojiPicker && !emojiPicker.contains(e.target)) {
                emojiPicker.style.display = 'none';
            }
        }
    });

    // File upload
    if (attachBtn) {
        attachBtn.addEventListener('click', openFileUploadModal);
    }

    // Close upload modal when clicking outside
    if (fileUploadModal) {
        fileUploadModal.addEventListener('click', (e) => {
            if (e.target === fileUploadModal) {
                closeFileUploadModal();
            }
        });
    }
});
async function uploadMedia(file) {
    if (!file || !currentChatPartner) return;

    const token = sessionHandler.getAuthToken();
    if (!token) {
        redirectToLogin();
        return;
    }

    // Determine message type
    let messageType = 'DOCUMENT';
    if (file.type.startsWith('image/')) messageType = 'IMAGE';
    if (file.type.startsWith('video/')) messageType = 'VIDEO';

    // Show upload progress
    const progressId = 'upload-' + Date.now();
    showUploadProgress(progressId, file.name, file.size);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('recipientId', currentChatPartner.email || currentChatPartner.staffId);
    formData.append('type', messageType);

    try {
        const response = await fetch('/api/chat/upload-media', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error(`Upload failed with status: ${response.status}`);
        }

        const result = await response.json();

        // Create a temporary message
        const tempMessage = {
            id: 'temp-' + Date.now(),
            senderId: currentUser.email,
            recipientId: currentChatPartner.email || currentChatPartner.staffId,
            content: result.fileUrl,
            type: messageType,
            fileName: file.name,
            fileSize: file.size,
            timestamp: new Date().toISOString(),
            status: 'SENDING'
        };

        // Add to UI immediately
        addMessageToUI(tempMessage);

        showSuccess('File sent successfully');
    } catch (error) {
        console.error('Error uploading file:', error);
        showError('Failed to send file. Please try again.');
    } finally {
        hideUploadProgress(progressId);
    }
}


function showUploadProgress(id, filename, filesize) {
    const progressElement = document.createElement('div');
    progressElement.id = id;
    progressElement.className = 'upload-progress';
    progressElement.innerHTML = `
        <div class="upload-progress-container">
            <div class="file-info">
                <i class="fas fa-file-upload"></i>
                <div>
                    <div class="filename">${filename}</div>
                    <div class="filesize">${formatFileSize(filesize)}</div>
                </div>
            </div>
            <div class="progress-bar">
                <div class="progress-fill" style="width: 0%"></div>
            </div>
            <div class="progress-text">0%</div>
        </div>
    `;

    document.body.appendChild(progressElement);
}

function updateUploadProgress(id, percent) {
    const progressElement = document.getElementById(id);
    if (progressElement) {
        const fill = progressElement.querySelector('.progress-fill');
        const text = progressElement.querySelector('.progress-text');

        if (fill) fill.style.width = `${percent}%`;
        if (text) text.textContent = `${percent}%`;
    }
}

function hideUploadProgress(id) {
    const progressElement = document.getElementById(id);
    if (progressElement) {
        progressElement.remove();
    }
}

function showUploadProgress(filename, filesize) {
    const progressElement = document.createElement('div');
    progressElement.id = 'upload-progress';
    progressElement.innerHTML = `
        <div class="progress-container">
            <div class="file-info">
                <i class="fas fa-file-upload"></i>
                <div>
                    <div class="filename">${filename}</div>
                    <div class="filesize">${formatFileSize(filesize)}</div>
                </div>
            </div>
            <div class="progress-bar">
                <div class="progress-fill" style="width: 0%"></div>
            </div>
            <div class="progress-text">0%</div>
        </div>
    `;

    document.body.appendChild(progressElement);
}

function updateUploadProgress(percent) {
    const progressElement = document.getElementById('upload-progress');
    if (progressElement) {
        const fill = progressElement.querySelector('.progress-fill');
        const text = progressElement.querySelector('.progress-text');

        if (fill) fill.style.width = `${percent}%`;
        if (text) text.textContent = `${percent}%`;
    }
}

function hideUploadProgress() {
    const progressElement = document.getElementById('upload-progress');
    if (progressElement) {
        progressElement.remove();
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}



function openMediaViewer(src, type) {
    const modal = document.createElement('div');
    modal.className = 'media-viewer-modal';
    modal.innerHTML = `
        <div class="media-viewer-content">
            <span class="media-viewer-close" onclick="this.parentElement.parentElement.remove()">
                <i class="fas fa-times"></i>
            </span>
            ${type === 'image' ?
                `<img src="${src}" alt="Full size image" class="media-viewer-image">` :
                `<video controls autoplay class="media-viewer-video">
                    <source src="${src}" type="${type === 'video' ? 'video/mp4' : 'audio/mp3'}">
                </video>`
            }
            <div class="media-viewer-actions">
                <button onclick="downloadMedia('${src}')">
                    <i class="fas fa-download"></i> Download
                </button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
}

function downloadMedia(url) {
    const link = document.createElement('a');
    link.href = url;
    link.download = url.split('/').pop() || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
// ============================================================================
// FILE UPLOAD MANAGER
// ============================================================================
const FileUploadManager = {
    handleFileUpload: async function(file) {
        try {
            // File validation
            FileManager.validateFile(file);

            // Authentication check
            if (!AuthenticationManager.isAuthenticated()) {
                NotificationManager.showError('You must be logged in to upload files');

                // Try to refresh token
                const refreshed = await AuthenticationManager.refreshToken();
                if (!refreshed) {
                    NotificationManager.showError('Session expired. Please log in again.');
                    return;
                }
            }

            // Get token
            const token = AuthenticationManager.getAuthToken();
            if (!token) {
                NotificationManager.showError('Missing authentication token. Please log in again.');
                return;
            }

            // Ensure we have a chat partner
            let chatPartner = ChatPartnerManager.ensureChatPartner();

            if (!chatPartner) {
                // Ask user confirmation
                const userConfirmed = confirm(
                    'Unable to automatically detect recipient. Do you want to continue with the upload?'
                );

                if (!userConfirmed) {
                    NotificationManager.showInfo('Upload cancelled by user.');
                    return;
                }

                // Use current user as fallback recipient
                const currentUser = AuthenticationManager.getCurrentUser();
                chatPartner = {
                    id: currentUser?.staffId || currentUser?.email || 'self',
                    email: currentUser?.email || 'self@system.local',
                    source: 'fallback'
                };
            }

            // Show upload progress modal
            this.showUploadModal(file);

            // Prepare form data
            const formData = new FormData();
            formData.append('file', file);
            formData.append('recipientId', chatPartner.email || chatPartner.id);
            formData.append('senderId', AuthenticationManager.getCurrentUser()?.email || 'unknown');
            formData.append('context', window.location.pathname);
            formData.append('chatPartner', JSON.stringify(chatPartner));

            // Request configuration
            const uploadConfig = {
                method: 'POST',
                body: formData,
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            };

            // Possible upload URLs
            const uploadUrls = [
                '/api/chat/upload-media',
                '/api/upload',
                '/api/files/upload',
                '/upload',
                '/api/chat/upload'
            ];

            let uploadSuccess = false;
            let lastError = null;

            // Try upload with different URLs
            for (const url of uploadUrls) {
                try {
                    console.log(`Attempting upload to: ${url}`);

                    const xhr = new XMLHttpRequest();
                    currentUploadXHR = xhr;

                    const uploadPromise = new Promise((resolve, reject) => {
                        xhr.upload.onprogress = (e) => {
                            if (e.lengthComputable) {
                                const percent = Math.round((e.loaded / e.total) * 100);
                                this.updateUploadProgress(percent);
                            }
                        };

                        xhr.onload = () => {
                            if (xhr.status >= 200 && xhr.status < 300) {
                                try {
                                    const response = JSON.parse(xhr.responseText);
                                    resolve(response);
                                } catch (e) {
                                    resolve({ success: true, message: 'File uploaded successfully' });
                                }
                            } else {
                                reject(new Error(`HTTP ${xhr.status}: ${xhr.statusText}`));
                            }
                        };

                        xhr.onerror = () => reject(new Error('Network error'));
                        xhr.onabort = () => reject(new Error('Upload cancelled'));

                        xhr.open('POST', url);
                        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
                        xhr.send(formData);
                    });

                    const response = await uploadPromise;

                    // Upload successful
                    uploadSuccess = true;
                    this.hideUploadModal();
                    NotificationManager.showSuccess(`File "${file.name}" uploaded successfully!`);

                    // Handle response
                    this.handleUploadResponse(response, file);
                    break;

                } catch (error) {
                    console.warn(`Upload failed to ${url}:`, error);
                    lastError = error;

                    // If not the last URL, continue
                    if (url !== uploadUrls[uploadUrls.length - 1]) {
                        continue;
                    }
                }
            }

            // If all upload attempts failed
            if (!uploadSuccess) {
                throw lastError || new Error('All upload attempts failed');
            }

        } catch (error) {
            console.error('Upload error:', error);
            this.hideUploadModal();

            let errorMessage = 'Error uploading file';

            if (error.message.includes('401') || error.message.includes('Authentication')) {
                errorMessage = 'Authentication error. Please log in again.';
            } else if (error.message.includes('413') || error.message.includes('size')) {
                errorMessage = 'File too large.';
            } else if (error.message.includes('type')) {
                errorMessage = 'File type not allowed.';
            } else if (error.message) {
                errorMessage = error.message;
            }

            NotificationManager.showError(errorMessage);
        } finally {
            currentUploadXHR = null;
        }
    },

    showUploadModal: function(file) {
        const existingModal = document.getElementById('upload-progress-modal');
        if (existingModal) {
            existingModal.remove();
        }

        const modal = document.createElement('div');
        modal.id = 'upload-progress-modal';
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 10000;
        `;

        const content = document.createElement('div');
        content.style.cssText = `
            background: white;
            padding: 30px;
            border-radius: 12px;
            min-width: 400px;
            max-width: 500px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        `;

        content.innerHTML = `
            <div style="text-align: center;">
                <h3 style="margin: 0 0 20px 0; color: #333;">Upload in progress</h3>
                <div style="margin-bottom: 15px;">
                    <strong>${FileManager.getFileIcon(file.type)} ${file.name}</strong>
                </div>
                <div style="margin-bottom: 15px; color: #666;">
                    Size: ${FileManager.formatFileSize(file.size)}
                </div>
                <div style="background: #f0f0f0; height: 20px; border-radius: 10px; overflow: hidden; margin-bottom: 15px;">
                    <div id="upload-progress-bar" style="background: #007bff; height: 100%; width: 0%; transition: width 0.3s;"></div>
                </div>
                <div id="upload-progress-text" style="margin-bottom: 20px; font-weight: bold; color: #007bff;">0%</div>
                <button id="upload-cancel-btn" style="background: #dc3545; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer;">
                    Cancel
                </button>
            </div>
        `;

        modal.appendChild(content);
        document.body.appendChild(modal);

        // Cancel handler
        document.getElementById('upload-cancel-btn').onclick = () => {
            this.cancelUpload();
        };

        // Close on outside click
        modal.onclick = (e) => {
            if (e.target === modal) {
                this.cancelUpload();
            }
        };
    },

    updateUploadProgress: function(percent) {
        const progressBar = document.getElementById('upload-progress-bar');
        const progressText = document.getElementById('upload-progress-text');

        if (progressBar) {
            progressBar.style.width = percent + '%';
        }
        if (progressText) {
            progressText.textContent = percent + '%';
        }
    },

    hideUploadModal: function() {
        const modal = document.getElementById('upload-progress-modal');
        if (modal) {
            modal.remove();
        }
    },

    cancelUpload: function() {
        if (currentUploadXHR) {
            currentUploadXHR.abort();
            currentUploadXHR = null;
        }
        this.hideUploadModal();
        NotificationManager.showInfo('Upload cancelled');
    },

    handleUploadResponse: function(response, file) {
        console.log('Upload response:', response);

        // Try to call existing handler functions
        const handlers = [
            'addFileMessageToChat',
            'addMessageToChat',
            'handleFileUploadComplete',
            'onFileUploaded'
        ];

        let handlerFound = false;

        for (const handlerName of handlers) {
            if (typeof window[handlerName] === 'function') {
                try {
                    window[handlerName](response, file);
                    handlerFound = true;
                    console.log(`Handler found: ${handlerName}`);
                    break;
                } catch (error) {
                    console.warn(`Error with handler ${handlerName}:`, error);
                }
            }
        }

        // If no handler found, dispatch custom event
        if (!handlerFound) {
            const event = new CustomEvent('fileUploaded', {
                detail: { response, file }
            });
            document.dispatchEvent(event);
            console.log('fileUploaded event dispatched');
        }
    }
};

// ============================================================================
// PDF MANAGER
// ============================================================================
const PDFManager = {
    openPdfViewer: function(pdfUrl, fileName) {
        try {
            this.createPdfViewerModal();

            const modal = document.getElementById('pdf-viewer-modal');
            const title = document.getElementById('pdf-viewer-title');
            const frame = document.getElementById('pdf-viewer-frame');

            if (!modal || !title || !frame) {
                throw new Error('PDF viewer elements not found');
            }

            title.textContent = fileName || 'PDF Document';
            frame.src = pdfUrl;
            modal.style.display = 'block';

            // Initialize PDF.js if available
            if (typeof pdfjsLib !== 'undefined') {
                this.initializePdfJs(pdfUrl);
            }

            NotificationManager.showSuccess('PDF opened in viewer');
        } catch (error) {
            console.error('Error opening PDF:', error);
            NotificationManager.showError('Failed to open PDF');
        }
    },

    createPdfViewerModal: function() {
        if (document.getElementById('pdf-viewer-modal')) {
            return;
        }

        const modal = document.createElement('div');
        modal.id = 'pdf-viewer-modal';
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.9);
            z-index: 10000;
            display: none;
        `;

        modal.innerHTML = `
            <div style="height: 100%; display: flex; flex-direction: column;">
                <div style="background: #333; padding: 15px; display: flex; justify-content: space-between; align-items: center;">
                    <h3 id="pdf-viewer-title" style="color: white; margin: 0;">PDF Document</h3>
                    <div>
                        <button id="pdf-prev-page" style="margin: 0 5px; padding: 8px 12px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">◀ Previous</button>
                        <span id="current-page" style="color: white; margin: 0 10px;">1</span>
                        <span style="color: white;">of</span>
                        <span id="total-pages" style="color: white; margin: 0 10px;">1</span>
                        <button id="pdf-next-page" style="margin: 0 5px; padding: 8px 12px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">Next ▶</button>
                        <button id="pdf-zoom-out" style="margin: 0 5px; padding: 8px 12px; background: #6c757d; color: white; border: none; border-radius: 4px; cursor: pointer;">−</button>
                        <span id="current-zoom" style="color: white; margin: 0 10px;">100%</span>
                        <button id="pdf-zoom-in" style="margin: 0 5px; padding: 8px 12px; background: #6c757d; color: white; border: none; border-radius: 4px; cursor: pointer;">+</button>
                        <button id="pdf-download" style="margin: 0 5px; padding: 8px 12px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer;">⬇ Download</button>
                        <button id="pdf-close" style="margin: 0 5px; padding: 8px 12px; background: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer;">✕ Close</button>
                    </div>
                </div>
                <div style="flex: 1; display: flex; justify-content: center; align-items: center; overflow: auto;">
                    <iframe id="pdf-viewer-frame" style="width: 90%; height: 90%; border: none; background: white;" loading="lazy"></iframe>
                    <canvas id="pdf-canvas" style="display: none; max-width: 90%; max-height: 90%; box-shadow: 0 0 20px rgba(255,255,255,0.1);"></canvas>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Event listeners
        this.attachPdfViewerEventListeners();
    },

    attachPdfViewerEventListeners: function() {
        // Close
        document.getElementById('pdf-close').onclick = () => {
            this.closePdfViewer();
        };

        // Page navigation
        document.getElementById('pdf-prev-page').onclick = () => {
            this.navigatePdfPage(-1);
        };

        document.getElementById('pdf-next-page').onclick = () => {
            this.navigatePdfPage(1);
        };

        // Zoom
        document.getElementById('pdf-zoom-in').onclick = () => {
            this.zoomPdf(0.25);
        };

        document.getElementById('pdf-zoom-out').onclick = () => {
            this.zoomPdf(-0.25);
        };

        // Download
        document.getElementById('pdf-download').onclick = () => {
            this.downloadCurrentPdf();
        };

        // Close on Escape
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && document.getElementById('pdf-viewer-modal').style.display === 'block') {
                this.closePdfViewer();
            }
        });

        // Close on outside click
        document.getElementById('pdf-viewer-modal').onclick = (e) => {
            if (e.target.id === 'pdf-viewer-modal') {
                this.closePdfViewer();
            }
        };
    },

    initializePdfJs: async function(pdfUrl) {
        if (typeof pdfjsLib === 'undefined') {
            console.warn('PDF.js not available, using default iframe');
            return;
        }

        try {
            const loadingTask = pdfjsLib.getDocument(pdfUrl);
            currentPdfDoc = await loadingTask.promise;

            document.getElementById('total-pages').textContent = currentPdfDoc.numPages;

            // Hide iframe and show canvas
            document.getElementById('pdf-viewer-frame').style.display = 'none';
            document.getElementById('pdf-canvas').style.display = 'block';

            this.renderPdfPage(1);
        } catch (error) {
            console.error('PDF.js error:', error);
            // Fallback to iframe
            document.getElementById('pdf-viewer-frame').style.display = 'block';
            document.getElementById('pdf-canvas').style.display = 'none';
        }
    },

    renderPdfPage: async function(pageNumber) {
        if (!currentPdfDoc) return;

        try {
            const page = await currentPdfDoc.getPage(pageNumber);
            const canvas = document.getElementById('pdf-canvas');
            const context = canvas.getContext('2d');

            const viewport = page.getViewport({ scale: currentZoom });
            canvas.height = viewport.height;
            canvas.width = viewport.width;

            const renderContext = {
                canvasContext: context,
                viewport: viewport
            };

            await page.render(renderContext).promise;

            currentPageNum = pageNumber;
            document.getElementById('current-page').textContent = pageNumber;
            this.updatePdfNavigationButtons();
        } catch (error) {
            console.error('Error rendering PDF page:', error);
        }
    },

    navigatePdfPage: function(direction) {
        if (!currentPdfDoc) return;

        const newPage = currentPageNum + direction;
        if (newPage >= 1 && newPage <= currentPdfDoc.numPages) {
            this.renderPdfPage(newPage);
        }
    },

    zoomPdf: function(delta) {
        const newZoom = Math.max(0.5, Math.min(3.0, currentZoom + delta));
        if (newZoom !== currentZoom) {
            currentZoom = newZoom;
            document.getElementById('current-zoom').textContent = Math.round(currentZoom * 100) + '%';

            if (currentPdfDoc) {
                this.renderPdfPage(currentPageNum);
            } else {
                // For iframe, use CSS transform
                const frame = document.getElementById('pdf-viewer-frame');
                frame.style.transform = `scale(${currentZoom})`;
                frame.style.transformOrigin = 'center';
            }
        }
    },

    updatePdfNavigationButtons: function() {
        if (!currentPdfDoc) return;

        document.getElementById('pdf-prev-page').disabled = currentPageNum <= 1;
        document.getElementById('pdf-next-page').disabled = currentPageNum >= currentPdfDoc.numPages;
    },

    downloadCurrentPdf: function() {
        const frame = document.getElementById('pdf-viewer-frame');
        if (frame && frame.src) {
            const link = document.createElement('a');
            link.href = frame.src;
            link.download = document.getElementById('pdf-viewer-title').textContent + '.pdf';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    },

    closePdfViewer: function() {
        const modal = document.getElementById('pdf-viewer-modal');
        if (modal) {
            modal.style.display = 'none';

            // Reset variables
            currentPdfDoc = null;
            currentPageNum = 1;
            currentZoom = 1.0;

            // Clean up content
            const frame = document.getElementById('pdf-viewer-frame');
            if (frame) frame.src = '';

            const canvas = document.getElementById('pdf-canvas');
            if (canvas) {
                const context = canvas.getContext('2d');
                context.clearRect(0, 0, canvas.width, canvas.height);
            }
        }
    }
};

// ============================================================================
// DRAG AND DROP MANAGER
// ============================================================================
// ============================================================================
// ENHANCED DRAG AND DROP MANAGER
// ============================================================================
const DragDropManager = {
    // Initialize the drag and drop system
    initialize: function() {
        this.setupDropZones();
        this.attachGlobalDragHandlers();
    },

    // Set up drop zones
    setupDropZones: function() {
        // Main drop zone
        let dropZone = document.querySelector('.drop-zone');

        if (!dropZone) {
            // Create global drop zone if it doesn't exist
            dropZone = this.createGlobalDropZone();
        }

        this.attachDropZoneHandlers(dropZone);

        // Additional drop zones
        const additionalZones = document.querySelectorAll('[data-drop-zone]');
        additionalZones.forEach(zone => {
            this.attachDropZoneHandlers(zone);
        });
    },

    // Create a global drop zone
    createGlobalDropZone: function() {
        const dropZone = document.createElement('div');
        dropZone.className = 'global-drop-zone';
        dropZone.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 123, 255, 0.1);
            border: 3px dashed #007bff;
            display: none;
            justify-content: center;
            align-items: center;
            z-index: 9999;
            pointer-events: none;
        `;

        dropZone.innerHTML = `
            <div style="text-align: center; color: #007bff; font-size: 24px; font-weight: bold;">
                <div style="font-size: 48px; margin-bottom: 20px;">📁</div>
                <div>Drop your files here</div>
                <div style="font-size: 16px; margin-top: 10px; opacity: 0.8;">
                    PDF, Word, Excel, Images, Videos...
                </div>
            </div>
        `;

        document.body.appendChild(dropZone);
        return dropZone;
    },

    // Attach global drag handlers
    attachGlobalDragHandlers: function() {
        let dragCounter = 0;

        document.addEventListener('dragenter', (e) => {
            e.preventDefault();
            dragCounter++;

            if (dragCounter === 1) {
                this.showGlobalDropZone();
            }
        });

        document.addEventListener('dragleave', (e) => {
            e.preventDefault();
            dragCounter--;

            if (dragCounter === 0) {
                this.hideGlobalDropZone();
            }
        });

        document.addEventListener('dragover', (e) => {
            e.preventDefault();
        });

        document.addEventListener('drop', (e) => {
            e.preventDefault();
            dragCounter = 0;
            this.hideGlobalDropZone();

            if (e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                this.handleFileDrop(e.dataTransfer.files);
            }
        });
    },

    // Attach handlers to a specific drop zone
    attachDropZoneHandlers: function(dropZone) {
        if (!dropZone) return;

        dropZone.addEventListener('dragenter', (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.add('drag-over');
        });

        dropZone.addEventListener('dragleave', (e) => {
            e.preventDefault();
            e.stopPropagation();
            if (!dropZone.contains(e.relatedTarget)) {
                dropZone.classList.remove('drag-over');
            }
        });

        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            e.stopPropagation();
        });

        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.remove('drag-over');

            if (e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                this.handleFileDrop(e.dataTransfer.files);
            }
        });
    },

    // Show global drop zone
    showGlobalDropZone: function() {
        const dropZone = document.querySelector('.global-drop-zone');
        if (dropZone) {
            dropZone.style.display = 'flex';
        }
    },

    // Hide global drop zone
    hideGlobalDropZone: function() {
        const dropZone = document.querySelector('.global-drop-zone');
        if (dropZone) {
            dropZone.style.display = 'none';
        }
    },

    // Handle dropped files
    handleFileDrop: function(files) {
        if (!files || !files.length) return;

        Array.from(files).forEach(file => {
            try {
                console.log('File dropped:', file.name);
                if (FileUploadManager && typeof FileUploadManager.handleFileUpload === 'function') {
                    FileUploadManager.handleFileUpload(file);
                }
            } catch (error) {
                console.error('Error handling dropped file:', error);
                if (NotificationManager && typeof NotificationManager.showError === 'function') {
                    NotificationManager.showError(`Error processing file: ${file.name}`);
                }
            }
        });
    }
};

// ============================================================================
// ENHANCED AUTHENTICATION MANAGER
// ============================================================================
const EnhancedAuthManager = {
    // Check authentication with multiple fallbacks
    checkAuthentication: async function() {
        try {
            // Check 1: Valid token
            if (!AuthenticationManager || !AuthenticationManager.getAuthToken()) {
                return await this.handleMissingAuth();
            }

            // Check 2: Valid session
            if (!AuthenticationManager.isAuthenticated()) {
                return await this.handleExpiredSession();
            }

            // Check 3: Valid user
            const user = AuthenticationManager.getCurrentUser();
            if (!user || !user.staffId) {
                return await this.handleInvalidUser();
            }

            return true;
        } catch (error) {
            console.error('Authentication check error:', error);
            return false;
        }
    },

    // Handle missing authentication
    handleMissingAuth: async function() {
        console.warn('Missing authentication token');

        // Try automatic recovery
        if (await this.attemptAutoAuth()) {
            return true;
        }

        // Prompt user to reauthenticate
        return this.promptReauth('Session expired. Please log in again.');
    },

    // Handle expired session
    handleExpiredSession: async function() {
        console.warn('Session expired');

        // Try refresh
        if (AuthenticationManager && typeof AuthenticationManager.refreshToken === 'function') {
            if (await AuthenticationManager.refreshToken()) {
                if (NotificationManager && typeof NotificationManager.showSuccess === 'function') {
                    NotificationManager.showSuccess('Session refreshed automatically');
                }
                return true;
            }
        }

        return this.promptReauth('Session expired. Unable to refresh automatically.');
    },

    // Handle invalid user
    handleInvalidUser: async function() {
        console.warn('Invalid user data');

        // Try user data recovery
        if (await this.attemptUserRecovery()) {
            return true;
        }

        return this.promptReauth('Corrupted user data. Reauthentication required.');
    },

    // Attempt automatic authentication
    attemptAutoAuth: async function() {
        try {
            // Check cookies or other sources
            const cookieToken = this.getTokenFromCookie();
            if (cookieToken) {
                localStorage.setItem('jwt_token', cookieToken);
                return await this.validateToken(cookieToken);
            }

            // Check URL (for direct links)
            const urlToken = this.getTokenFromUrl();
            if (urlToken) {
                localStorage.setItem('jwt_token', urlToken);
                return await this.validateToken(urlToken);
            }

            return false;
        } catch (error) {
            console.error('Auto-auth error:', error);
            return false;
        }
    },

    // Get token from cookies
    getTokenFromCookie: function() {
        try {
            if (!document.cookie) return null;

            const cookies = document.cookie.split(';');
            for (const cookie of cookies) {
                const [name, value] = cookie.trim().split('=');
                if (name === 'jwt_token' || name === 'authToken') {
                    return decodeURIComponent(value);
                }
            }
            return null;
        } catch (error) {
            console.error('Cookie parsing error:', error);
            return null;
        }
    },

    // Get token from URL
    getTokenFromUrl: function() {
        try {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get('token') || urlParams.get('auth');
        } catch (error) {
            console.error('URL parsing error:', error);
            return null;
        }
    },

    // Validate token
    validateToken: async function(token) {
        if (!token) return false;

        try {
            const response = await fetch('/api/validate-token', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response && response.ok) {
                const data = await response.json();
                if (data.valid && data.user) {
                    // Save user data
                    const sessionData = {
                        token: token,
                        staffId: data.user.staffId,
                        email: data.user.email,
                        fullName: data.user.fullName,
                        role: data.user.role
                    };

                    if (typeof UserSessionHandler !== 'undefined' &&
                        typeof UserSessionHandler.saveUserSession === 'function') {
                        UserSessionHandler.saveUserSession(sessionData);
                    }
                    return true;
                }
            }
            return false;
        } catch (error) {
            console.error('Token validation error:', error);
            return false;
        }
    },

    // Attempt user data recovery
    attemptUserRecovery: async function() {
        try {
            const token = AuthenticationManager ? AuthenticationManager.getAuthToken() : null;
            if (!token) return false;

            const response = await fetch('/api/user/profile', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response && response.ok) {
                const userData = await response.json();
                if (userData && userData.staffId) {
                    // Update user data
                    const currentData = JSON.parse(localStorage.getItem('staff_info') || '{}');
                    const updatedData = { ...currentData, ...userData };
                    localStorage.setItem('staff_info', JSON.stringify(updatedData));
                    return true;
                }
            }
            return false;
        } catch (error) {
            console.error('User recovery error:', error);
            return false;
        }
    },

    // Prompt user to reauthenticate
    promptReauth: function(message) {
        try {
            const shouldReauth = confirm(`${message}\n\nWould you like to be redirected to the login page?`);

            if (shouldReauth) {
                // Save current URL for post-login redirect
                sessionStorage.setItem('redirectAfterLogin', window.location.href);

                // Redirect to login page
                window.location.href = '/login.html';
            }

            return false;
        } catch (error) {
            console.error('Reauth prompt error:', error);
            return false;
        }
    },

    // Force reauthentication
    forceReauth: function() {
        try {
            // Clear session
            if (AuthenticationManager && typeof AuthenticationManager.clearSession === 'function') {
                AuthenticationManager.clearSession();
            }
            localStorage.clear();

            // Immediate redirect
            window.location.href = '/login.html';
        } catch (error) {
            console.error('Force reauth error:', error);
        }
    }
};





// ============================================================================
// MEDIA DISPLAY FUNCTIONS
// ============================================================================

function displayMediaMessage(message) {
    const messagesContainer = document.getElementById('messages-container');
    if (!messagesContainer) return;

    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${message.senderId === getCurrentUserId() ? 'sent' : 'received'}`;
    messageDiv.dataset.messageId = message.id;

    let mediaContent = '';
    const fileSize = formatFileSize(message.fileSize);

    switch(message.type) {
        case 'IMAGE':
            mediaContent = `
                <div class="media-message image-message">
                    <div class="media-container" onclick="openMediaViewer('${message.id}', 'image')">
                        <img src="/api/chat/media/thumbnail/${message.id}"
                             alt="${message.fileName}"
                             onerror="this.src='/api/chat/media/stream/${message.id}'">
                        <div class="media-overlay">
                            <i class="fas fa-expand"></i>
                        </div>
                    </div>
                    <div class="media-info">
                        <span class="file-name">${message.fileName}</span>
                        <span class="file-size">${fileSize}</span>
                        <a href="/api/chat/media/download/${message.id}"
                           download="${message.fileName}"
                           class="download-btn">
                            <i class="fas fa-download"></i>
                        </a>
                    </div>
                </div>
            `;
            break;

        case 'VIDEO':
            mediaContent = `
                <div class="media-message video-message">
                    <div class="media-container">
                        <video controls>
                            <source src="/api/chat/media/stream/${message.id}" type="video/mp4">
                            Your browser does not support the video tag.
                        </video>
                    </div>
                    <div class="media-info">
                        <span class="file-name">${message.fileName}</span>
                        <span class="file-size">${fileSize}</span>
                        <a href="/api/chat/media/download/${message.id}"
                           download="${message.fileName}"
                           class="download-btn">
                            <i class="fas fa-download"></i>
                        </a>
                    </div>
                </div>
            `;
            break;

        case 'DOCUMENT':
            mediaContent = `
                <div class="media-message document-message">
                    <div class="document-preview" onclick="openPdfViewer('/api/chat/media/stream/${message.id}', '${message.fileName}')">
                        <i class="fas fa-file-pdf"></i>
                        <span>PDF Document</span>
                    </div>
                    <div class="media-info">
                        <span class="file-name">${message.fileName}</span>
                        <span class="file-size">${fileSize}</span>
                        <a href="/api/chat/media/download/${message.id}"
                           download="${message.fileName}"
                           class="download-btn">
                            <i class="fas fa-download"></i>
                        </a>
                    </div>
                </div>
            `;
            break;

        default:
            mediaContent = `
                <div class="media-message file-message">
                    <div class="file-preview">
                        <i class="fas fa-file"></i>
                    </div>
                    <div class="media-info">
                        <span class="file-name">${message.fileName}</span>
                        <span class="file-size">${fileSize}</span>
                        <a href="/api/chat/media/download/${message.id}"
                           download="${message.fileName}"
                           class="download-btn">
                            <i class="fas fa-download"></i>
                        </a>
                    </div>
                </div>
            `;
    }

    messageDiv.innerHTML = `
        <div class="message-content">
            ${mediaContent}
            <div class="message-meta">
                <span class="message-time">${formatMessageTime(message.timestamp)}</span>
                ${message.senderId === getCurrentUserId() ?
                    `<span class="message-status ${message.status?.toLowerCase()}">
                        ${getStatusIcon(message.status)}
                    </span>` : ''}
            </div>
        </div>
    `;

    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

function openMediaViewer(messageId, type) {
    const viewer = document.getElementById('media-viewer-modal');
    if (!viewer) createMediaViewerModal();

    const token = AuthenticationManager.getAuthToken();

    fetch(`/api/chat/media/metadata/${messageId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        const viewerContent = document.getElementById('media-viewer-content');

        if (type === 'image') {
            viewerContent.innerHTML = `
                <img src="/api/chat/media/stream/${messageId}"
                     alt="${data.fileName}"
                     class="full-media">
                <div class="media-viewer-actions">
                    <button onclick="downloadFile('/api/chat/media/download/${messageId}', '${data.fileName}')">
                        <i class="fas fa-download"></i> Download
                    </button>
                    <button onclick="closeMediaViewer()">
                        <i class="fas fa-times"></i> Close
                    </button>
                </div>
            `;
        } else if (type === 'pdf') {
            PDFManager.openPdfViewer(
                `/api/chat/media/stream/${messageId}`,
                data.fileName
            );
            return;
        }

        document.getElementById('media-viewer-modal').style.display = 'flex';
    })
    .catch(error => {
        console.error('Error loading media:', error);
        NotificationManager.showError('Failed to load media');
    });
}

function createMediaViewerModal() {
    const modal = document.createElement('div');
    modal.id = 'media-viewer-modal';
    modal.className = 'modal';
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0,0,0,0.9);
        display: none;
        justify-content: center;
        align-items: center;
        z-index: 10000;
    `;

    modal.innerHTML = `
        <div id="media-viewer-content" class="media-viewer-content"></div>
    `;

    document.body.appendChild(modal);

    // Close on click outside
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeMediaViewer();
        }
    });
}

function closeMediaViewer() {
    const viewer = document.getElementById('media-viewer-modal');
    if (viewer) {
        viewer.style.display = 'none';
        viewer.innerHTML = '<div id="media-viewer-content" class="media-viewer-content"></div>';
    }
}

function loadConversationMedia(recipientId) {
    const token = AuthenticationManager.getAuthToken();
    if (!token) return;

    fetch(`/api/chat/conversation/${recipientId}/media`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        const mediaContainer = document.getElementById('media-gallery-container');
        if (!mediaContainer) return;

        mediaContainer.innerHTML = '';

        if (data.media && data.media.length > 0) {
            data.media.forEach(media => {
                const mediaItem = createMediaGalleryItem(media);
                mediaContainer.appendChild(mediaItem);
            });
        } else {
            mediaContainer.innerHTML = `
                <div class="no-media">
                    <i class="fas fa-images"></i>
                    <p>No media shared yet</p>
                </div>
            `;
        }
    })
    .catch(error => {
        console.error('Error loading media:', error);
        NotificationManager.showError('Failed to load media');
    });
}

function createMediaGalleryItem(media) {
    const item = document.createElement('div');
    item.className = 'media-gallery-item';
    item.dataset.type = media.type.toLowerCase();

    let thumbnail = '';
    let clickHandler = '';

    if (media.type === 'IMAGE') {
        thumbnail = `
            <img src="${media.thumbnailUrl || media.fileUrl}"
                 alt="${media.fileName}"
                 onerror="this.src='${media.fileUrl}'">`;
        clickHandler = `openMediaViewer('${media.messageId}', 'image')`;
    }
    else if (media.type === 'VIDEO') {
        thumbnail = `
            <div class="video-thumbnail">
                <i class="fas fa-play"></i>
            </div>`;
        clickHandler = `openMediaViewer('${media.messageId}', 'video')`;
    }
    else if (media.type === 'DOCUMENT') {
        thumbnail = `<i class="fas fa-file-pdf"></i>`;
        clickHandler = `openMediaViewer('${media.messageId}', 'pdf')`;
    }
    else {
        thumbnail = `<i class="fas fa-file"></i>`;
        clickHandler = `downloadFile('${media.downloadUrl}', '${media.fileName}')`;
    }

    item.innerHTML = `
        <div class="media-thumbnail" onclick="${clickHandler}">
            ${thumbnail}
        </div>
        <div class="media-info">
            <span class="file-name">${media.fileName}</span>
            <span class="file-size">${formatFileSize(media.fileSize)}</span>
        </div>
    `;

    return item;
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function formatMessageTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function getStatusIcon(status) {
    switch (status?.toLowerCase()) {
        case 'sent': return '✓';
        case 'delivered': return '✓✓';
        case 'read': return '✓✓';
        default: return '';
    }
}

function getCurrentUserId() {
    const user = AuthenticationManager.getCurrentUser();
    return user?.email || user?.staffId || '';
}

function scrollToBottom() {
    const container = document.getElementById('messages-container');
    if (container) {
        container.scrollTop = container.scrollHeight;
    }
}

// ============================================================================
// FILE SYSTEM INITIALIZATION AND EVENT MANAGEMENT
// ============================================================================
const FileSystemInitializer = {
    // Initialize the complete system
    initialize: function() {
        console.log('Initializing file system...');

        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                this.performInitialization();
            });
        } else {
            this.performInitialization();
        }
    },

    // Perform complete initialization
    performInitialization: function() {
        try {
            // Initialize handlers
            this.initializeFileInput();
            this.initializeDragDrop();
            this.initializeKeyboardShortcuts();
            this.initializeContextMenu();
            this.checkBrowserCompatibility();

            console.log('File system initialized successfully');
        } catch (error) {
            console.error('Initialization error:', error);
            if (NotificationManager && typeof NotificationManager.showError === 'function') {
                NotificationManager.showError('File system initialization error');
            }
        }
    },

    // Initialize file input fields
    initializeFileInput: function() {
        // Existing inputs
        const fileInputs = document.querySelectorAll('input[type="file"]');
        fileInputs.forEach(input => {
            input.addEventListener('change', (e) => {
                if (e.target && e.target.files && e.target.files.length > 0) {
                    Array.from(e.target.files).forEach(file => {
                        if (FileUploadManager && typeof FileUploadManager.handleFileUpload === 'function') {
                            FileUploadManager.handleFileUpload(file);
                        }
                    });
                }
            });
        });

        // Create global file input
        this.createGlobalFileInput();
    },

    // Create global file input
    createGlobalFileInput: function() {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.id = 'global-file-input';
        fileInput.multiple = true;
        fileInput.style.display = 'none';
        fileInput.accept = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.jpg,.jpeg,.png,.gif,.mp4,.mp3,.zip,.rar';

        fileInput.addEventListener('change', (e) => {
            if (e.target && e.target.files && e.target.files.length > 0) {
                Array.from(e.target.files).forEach(file => {
                    if (FileUploadManager && typeof FileUploadManager.handleFileUpload === 'function') {
                        FileUploadManager.handleFileUpload(file);
                    }
                });
            }
        });

        document.body.appendChild(fileInput);
    },

    // Initialize drag and drop
    initializeDragDrop: function() {
        if (DragDropManager && typeof DragDropManager.initialize === 'function') {
            DragDropManager.initialize();
        }
    },

    // Initialize keyboard shortcuts
    initializeKeyboardShortcuts: function() {
        document.addEventListener('keydown', (e) => {
            // Ctrl+U or Cmd+U to open file selector
            if ((e.ctrlKey || e.metaKey) && e.key === 'u') {
                e.preventDefault();
                this.openFileSelector();
            }

            // Ctrl+Shift+U to force new upload
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'U') {
                e.preventDefault();
                this.openFileSelector(true);
            }
        });
    },

    // Open file selector
    openFileSelector: function(force = false) {
        const fileInput = document.getElementById('global-file-input');
        if (fileInput) {
            if (force) {
                fileInput.value = ''; // Reset to allow selecting same file
            }
            fileInput.click();
        }
    },

    // Initialize context menu
    initializeContextMenu: function() {
        document.addEventListener('contextmenu', (e) => {
            // Add "Upload file" option to context menu
            this.showContextMenu(e);
        });

        // Close context menu on click
        document.addEventListener('click', () => {
            this.hideContextMenu();
        });
    },

    // Show custom context menu
    showContextMenu: function(e) {
        // Remove existing menu
        this.hideContextMenu();

        const menu = document.createElement('div');
        menu.id = 'file-context-menu';
        menu.style.cssText = `
            position: fixed;
            top: ${e.clientY}px;
            left: ${e.clientX}px;
            background: white;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.2);
            z-index: 10000;
            min-width: 150px;
        `;

        menu.innerHTML = `
            <div class="context-menu-item" data-action="upload" style="padding: 10px; cursor: pointer; border-bottom: 1px solid #eee;">
                📁 Upload file
            </div>
            <div class="context-menu-item" data-action="paste" style="padding: 10px; cursor: pointer;">
                📋 Paste from clipboard
            </div>
        `;

        // Event handlers
        if (menu.querySelector('[data-action="upload"]')) {
            menu.querySelector('[data-action="upload"]').onclick = (e) => {
                e.stopPropagation();
                this.openFileSelector();
                this.hideContextMenu();
            };
        }

        if (menu.querySelector('[data-action="paste"]')) {
            menu.querySelector('[data-action="paste"]').onclick = (e) => {
                e.stopPropagation();
                this.handleClipboardPaste();
                this.hideContextMenu();
            };
        }

        document.body.appendChild(menu);

        // Prevent default context menu
        e.preventDefault();
    },

    // Hide context menu
    hideContextMenu: function() {
        const menu = document.getElementById('file-context-menu');
        if (menu && menu.parentNode) {
            menu.parentNode.removeChild(menu);
        }
    },

    // Handle clipboard paste
    handleClipboardPaste: async function() {
        try {
            if (!navigator.clipboard || !navigator.clipboard.read) {
                throw new Error('Clipboard API not supported');
            }

            const clipboardItems = await navigator.clipboard.read();
            for (const clipboardItem of clipboardItems) {
                for (const type of clipboardItem.types) {
                    if (type.startsWith('image/')) {
                        const blob = await clipboardItem.getType(type);
                        const file = new File([blob], `pasted-image-${Date.now()}.${type.split('/')[1]}`, { type });
                        if (FileUploadManager && typeof FileUploadManager.handleFileUpload === 'function') {
                            FileUploadManager.handleFileUpload(file);
                        }
                    }
                }
            }
        } catch (error) {
            console.error('Clipboard paste error:', error);
            if (NotificationManager && typeof NotificationManager.showWarning === 'function') {
                NotificationManager.showWarning('Pasting from clipboard is not supported in this browser');
            }
        }
    },

    // Check browser compatibility
    checkBrowserCompatibility: function() {
        const features = {
            fileAPI: typeof File !== 'undefined',
            dragDrop: 'draggable' in document.createElement('div'),
            fetch: typeof fetch !== 'undefined',
            formData: typeof FormData !== 'undefined',
            clipboard: !!navigator.clipboard
        };

        const unsupported = Object.keys(features).filter(key => !features[key]);

        if (unsupported.length > 0) {
            console.warn('Unsupported features:', unsupported);
            if (NotificationManager && typeof NotificationManager.showWarning === 'function') {
                NotificationManager.showWarning(
                    `Some features may not work properly in this browser: ${unsupported.join(', ')}`
                );
            }
        }
    }
};

// ============================================================================
// EXPORT AND AUTOMATIC INITIALIZATION
// ============================================================================

// Expose global objects
if (typeof window !== 'undefined') {
    window.DragDropManager = DragDropManager || {};
    window.EnhancedAuthManager = EnhancedAuthManager || {};
    window.FileSystemInitializer = FileSystemInitializer || {};
}

// Automatic initialization
if (typeof FileSystemInitializer !== 'undefined' && typeof FileSystemInitializer.initialize === 'function') {
    FileSystemInitializer.initialize();
}

// Emoji picker
if (emojiBtn) {
    emojiBtn.addEventListener('click', toggleEmojiPicker);
}

// Fermer le emoji picker quand on clique ailleurs
document.addEventListener('click', (e) => {
    if (!emojiBtn.contains(e.target) && !document.getElementById('emoji-picker').contains(e.target)) {
        document.getElementById('emoji-picker').style.display = 'none';
    }
});

// File upload
if (attachBtn) {
    attachBtn.addEventListener('click', openFileUploadModal);
}

// Fermer le modal d'upload quand on clique ailleurs
document.getElementById('file-upload-modal').addEventListener('click', (e) => {
    if (e.target === document.getElementById('file-upload-modal')) {
        closeFileUploadModal();
    }
});
// Global error handler
window.addEventListener('error', (e) => {
    console.error('Global error:', e.error);
    if (EnhancedAuthManager && typeof EnhancedAuthManager.handleMissingAuth === 'function') {
        if (e.error && e.error.message && e.error.message.includes('auth')) {
            EnhancedAuthManager.handleMissingAuth();
        }
    }
});

// Unhandled promise rejection handler
window.addEventListener('unhandledrejection', (e) => {
    console.error('Unhandled promise rejection:', e.reason);
    if (EnhancedAuthManager && typeof EnhancedAuthManager.handleExpiredSession === 'function') {
        if (e.reason && e.reason.message && e.reason.message.includes('401')) {
            EnhancedAuthManager.handleExpiredSession();
        }
    }
});

console.log('Drag and Drop system loaded successfully');