// Enhanced chat.js with media display functionality

// Global variables for media handling
let currentMediaViewer = null;
let currentPdfUrl = null;

// Function to render chat messages with media support
function renderChatMessage(message) {
    const messagesContainer = document.getElementById('messages-container');
    const messageDiv = document.createElement('div');

    const isCurrentUser = message.senderId === getCurrentUserId();
    messageDiv.className = `message ${isCurrentUser ? 'sent' : 'received'}`;
    messageDiv.setAttribute('data-message-id', message.id);

    // Create message content based on type
    let messageContent = '';

    switch (message.type) {
        case 'TEXT':
            messageContent = createTextMessage(message);
            break;
        case 'IMAGE':
            messageContent = createImageMessage(message);
            break;
        case 'VIDEO':
            messageContent = createVideoMessage(message);
            break;
        case 'AUDIO':
            messageContent = createAudioMessage(message);
            break;
        case 'DOCUMENT':
            messageContent = createDocumentMessage(message);
            break;
        case 'FILE':
            messageContent = createFileMessage(message);
            break;
        default:
            messageContent = createTextMessage(message);
    }

    messageDiv.innerHTML = `
        <div class="message-content">
            ${messageContent}
            <div class="message-meta">
                <span class="message-time">${formatMessageTime(message.timestamp)}</span>
                ${isCurrentUser ? `<span class="message-status ${message.status?.toLowerCase()}">${getStatusIcon(message.status)}</span>` : ''}
            </div>
        </div>
    `;

    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

// Create text message
function createTextMessage(message) {
    return `
        <div class="text-content">
            <p>${escapeHtml(message.content)}</p>
        </div>
    `;
}

// Create image message
function createImageMessage(message) {
    const imageUrl = `/api/chat/media/stream/${message.id}`;
    const thumbnailUrl = `/api/chat/media/thumbnail/${message.id}`;

    return `
        <div class="media-content image-content">
            <div class="image-wrapper" onclick="openImageViewer('${imageUrl}', '${message.fileName}')">
                <img src="${thumbnailUrl}"
                     alt="${message.fileName}"
                     class="chat-image"
                     onerror="this.src='${imageUrl}'">
                <div class="image-overlay">
                    <i class="fas fa-eye"></i>
                </div>
            </div>
            <div class="media-info">
                <span class="file-name">${message.fileName}</span>
                <span class="file-size">${formatFileSize(message.fileSize)}</span>
            </div>
        </div>
    `;
}

// Create video message
function createVideoMessage(message) {
    const videoUrl = `/api/chat/media/stream/${message.id}`;

    return `
        <div class="media-content video-content">
            <div class="video-wrapper">
                <video controls class="chat-video" preload="metadata">
                    <source src="${videoUrl}" type="video/mp4">
                    Your browser does not support the video tag.
                </video>
            </div>
            <div class="media-info">
                <span class="file-name">${message.fileName}</span>
                <span class="file-size">${formatFileSize(message.fileSize)}</span>
            </div>
        </div>
    `;
}

// Create audio message
function createAudioMessage(message) {
    const audioUrl = `/api/chat/media/stream/${message.id}`;

    return `
        <div class="media-content audio-content">
            <div class="audio-wrapper">
                <audio controls class="chat-audio">
                    <source src="${audioUrl}" type="audio/mpeg">
                    Your browser does not support the audio tag.
                </audio>
            </div>
            <div class="media-info">
                <span class="file-name">${message.fileName}</span>
                <span class="file-size">${formatFileSize(message.fileSize)}</span>
            </div>
        </div>
    `;
}

// Create document message (PDF)
function createDocumentMessage(message) {
    const docUrl = `/api/chat/media/stream/${message.id}`;
    const downloadUrl = `/api/chat/media/download/${message.id}`;

    return `
        <div class="media-content document-content">
            <div class="document-wrapper">
                <div class="document-icon">
                    <i class="fas fa-file-pdf"></i>
                </div>
                <div class="document-details">
                    <span class="file-name">${message.fileName}</span>
                    <span class="file-size">${formatFileSize(message.fileSize)}</span>
                </div>
                <div class="document-actions">
                    <button onclick="openPdfViewer('${docUrl}', '${message.fileName}')"
                            title="View Document">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button onclick="downloadFile('${downloadUrl}', '${message.fileName}')"
                            title="Download">
                        <i class="fas fa-download"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
}

// Create generic file message
function createFileMessage(message) {
    const downloadUrl = `/api/chat/media/download/${message.id}`;

    return `
        <div class="media-content file-content">
            <div class="file-wrapper">
                <div class="file-icon">
                    <i class="fas fa-file"></i>
                </div>
                <div class="file-details">
                    <span class="file-name">${message.fileName}</span>
                    <span class="file-size">${formatFileSize(message.fileSize)}</span>
                </div>
                <div class="file-actions">
                    <button onclick="downloadFile('${downloadUrl}', '${message.fileName}')"
                            title="Download">
                        <i class="fas fa-download"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
}

// Image viewer functions
function openImageViewer(imageUrl, fileName) {
    // Create image viewer modal
    const modal = document.createElement('div');
    modal.className = 'modal image-viewer-modal';
    modal.innerHTML = `
        <div class="modal-content image-viewer-content">
            <span class="close-modal" onclick="closeImageViewer()">&times;</span>
            <div class="image-viewer-header">
                <h3>${fileName}</h3>
                <div class="image-viewer-actions">
                    <button onclick="downloadImage('${imageUrl}', '${fileName}')">
                        <i class="fas fa-download"></i>
                    </button>
                </div>
            </div>
            <div class="image-viewer-body">
                <img src="${imageUrl}" alt="${fileName}" class="full-image">
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    modal.style.display = 'block';
    currentMediaViewer = modal;
}

function closeImageViewer() {
    if (currentMediaViewer) {
        document.body.removeChild(currentMediaViewer);
        currentMediaViewer = null;
    }
}

// PDF viewer functions
function openPdfViewer(pdfUrl, fileName) {
    const modal = document.getElementById('pdf-viewer-modal');
    const iframe = document.getElementById('pdf-viewer-frame');
    const title = document.getElementById('pdf-viewer-title');

    title.textContent = fileName;
    iframe.src = pdfUrl;
    currentPdfUrl = pdfUrl;

    modal.style.display = 'block';
}

function closePdfViewer() {
    const modal = document.getElementById('pdf-viewer-modal');
    const iframe = document.getElementById('pdf-viewer-frame');

    modal.style.display = 'none';
    iframe.src = '';
    currentPdfUrl = null;
}

function downloadCurrentPdf() {
    if (currentPdfUrl) {
        const link = document.createElement('a');
        link.href = currentPdfUrl.replace('/stream/', '/download/');
        link.download = '';
        link.click();
    }
}

// Download functions
function downloadFile(downloadUrl, fileName) {
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

function downloadImage(imageUrl, fileName) {
    downloadFile(imageUrl.replace('/stream/', '/download/'), fileName);
    closeImageViewer();
}

// Load conversation media gallery
function loadConversationMedia(recipientId, type = null) {
    const token = localStorage.getItem('authToken');

    let url = `/api/chat/conversation/${recipientId}/media`;
    if (type) {
        url += `?type=${type}`;
    }

    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(mediaList => {
        displayMediaGallery(mediaList);
    })
    .catch(error => {
        console.error('Error loading conversation media:', error);
        showToast('Error loading media gallery', 'error');
    });
}

// Display media gallery
function displayMediaGallery(mediaList) {
    // This function would create a media gallery modal
    const modal = document.createElement('div');
    modal.className = 'modal media-gallery-modal';

    let galleryContent = '<div class="media-gallery-grid">';

    mediaList.forEach(media => {
        if (media.type === 'IMAGE') {
            galleryContent += `
                <div class="gallery-item image-item" onclick="openImageViewer('${media.fileUrl}', '${media.fileName}')">
                    <img src="${media.thumbnailUrl || media.fileUrl}" alt="${media.fileName}">
                    <div class="gallery-item-overlay">
                        <i class="fas fa-eye"></i>
                    </div>
                </div>
            `;
        } else if (media.type === 'VIDEO') {
            galleryContent += `
                <div class="gallery-item video-item">
                    <video src="${media.fileUrl}" muted>
                    </video>
                    <div class="gallery-item-overlay">
                        <i class="fas fa-play"></i>
                    </div>
                </div>
            `;
        } else if (media.type === 'DOCUMENT') {
            galleryContent += `
                <div class="gallery-item document-item" onclick="openPdfViewer('${media.fileUrl}', '${media.fileName}')">
                    <div class="document-preview">
                        <i class="fas fa-file-pdf"></i>
                        <p>${media.fileName}</p>
                    </div>
                </div>
            `;
        }
    });

    galleryContent += '</div>';

    modal.innerHTML = `
        <div class="modal-content media-gallery-content">
            <span class="close-modal" onclick="closeMediaGallery()">&times;</span>
            <div class="media-gallery-header">
                <h3>Media Gallery</h3>
                <div class="media-filters">
                    <button onclick="filterMedia('all')" class="active">All</button>
                    <button onclick="filterMedia('IMAGE')">Photos</button>
                    <button onclick="filterMedia('VIDEO')">Videos</button>
                    <button onclick="filterMedia('DOCUMENT')">Documents</button>
                </div>
            </div>
            <div class="media-gallery-body">
                ${galleryContent}
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    modal.style.display = 'block';
    currentMediaViewer = modal;
}

function closeMediaGallery() {
    if (currentMediaViewer) {
        document.body.removeChild(currentMediaViewer);
        currentMediaViewer = null;
    }
}

// Utility functions
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function formatMessageTime(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
        return date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
    } else if (diffDays === 1) {
        return 'Yesterday';
    } else if (diffDays < 7) {
        return date.toLocaleDateString([], {weekday: 'short'});
    } else {
        return date.toLocaleDateString([], {month: 'short', day: 'numeric'});
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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
    // This should return the current user's ID
    return localStorage.getItem('currentUserId') || '';
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    const container = document.getElementById('toast-container');
    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
    }, 100);

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            container.removeChild(toast);
        }, 300);
    }, 3000);
}

function scrollToBottom() {
    const messagesContainer = document.getElementById('messages-container');
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Enhanced chat history loading
function loadChatHistory(recipientId, page = 0, size = 50) {
    const token = localStorage.getItem('authToken');

    fetch(`/api/chat/history/${recipientId}?page=${page}&size=${size}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(messages => {
        const messagesContainer = document.getElementById('messages-container');
        messagesContainer.innerHTML = ''; // Clear existing messages

        if (messages.length === 0) {
            messagesContainer.innerHTML = `
                <div class="no-messages">
                    <i class="far fa-comment-dots"></i>
                    <p>No messages yet. Start the conversation!</p>
                </div>
            `;
            return;
        }

        // Render messages in reverse order (oldest first)
        messages.reverse().forEach(message => {
            renderChatMessage(message);
        });

        scrollToBottom();
    })
    .catch(error => {
        console.error('Error loading chat history:', error);
        showToast('Error loading chat history', 'error');
    });
}
function renderMediaMessage(message) {
    const mediaContainer = document.createElement('div');
    mediaContainer.className = 'media-message';

    switch(message.type) {
        case 'IMAGE':
            mediaContainer.innerHTML = createImageMessageHTML(message);
            break;
        case 'VIDEO':
            mediaContainer.innerHTML = createVideoMessageHTML(message);
            break;
        case 'AUDIO':
            mediaContainer.innerHTML = createAudioMessageHTML(message);
            break;
        case 'DOCUMENT':
            mediaContainer.innerHTML = createDocumentMessageHTML(message);
            break;
        default:
            mediaContainer.innerHTML = createFileMessageHTML(message);
    }

    return mediaContainer;
}

function createImageMessageHTML(message) {
    return `
        <div class="media-container image-container">
            <img src="/api/chat/media/preview/${message.id}"
                 alt="${message.fileName}"
                 onclick="openMediaViewer('${message.id}', 'image')">
            <div class="media-info">
                <span>${message.fileName}</span>
                <span>${formatFileSize(message.fileSize)}</span>
            </div>
        </div>
    `;
}

function createVideoMessageHTML(message) {
    return `
        <div class="media-container video-container">
            <video controls>
                <source src="/api/chat/media/stream/${message.id}" type="video/mp4">
            </video>
            <div class="media-info">
                <span>${message.fileName}</span>
                <span>${formatFileSize(message.fileSize)}</span>
            </div>
        </div>
    `;
}

function createAudioMessageHTML(message) {
    return `
        <div class="media-container audio-container">
            <audio controls>
                <source src="/api/chat/media/stream/${message.id}" type="audio/mpeg">
            </audio>
            <div class="media-info">
                <span>${message.fileName}</span>
                <span>${formatFileSize(message.fileSize)}</span>
            </div>
        </div>
    `;
}

function createDocumentMessageHTML(message) {
    return `
        <div class="media-container document-container">
            <div class="document-preview" onclick="openMediaViewer('${message.id}', 'document')">
                <i class="fas fa-file-pdf"></i>
            </div>
            <div class="media-info">
                <span>${message.fileName}</span>
                <span>${formatFileSize(message.fileSize)}</span>
                <button onclick="downloadFile('/api/chat/media/download/${message.id}', '${message.fileName}')">
                    <i class="fas fa-download"></i>
                </button>
            </div>
        </div>
    `;
}

function openMediaViewer(messageId, type) {
    if (type === 'image') {
        fetch(`/api/chat/media/metadata/${messageId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        })
        .then(response => response.json())
        .then(data => {
            showImageModal(data);
        });
    } else if (type === 'document') {
        fetch(`/api/chat/media/metadata/${messageId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        })
        .then(response => response.json())
        .then(data => {
            showPdfModal(data);
        });
    }
}

function showImageModal(metadata) {
    const modal = document.createElement('div');
    modal.className = 'media-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close" onclick="this.parentElement.parentElement.remove()">&times;</span>
            <img src="/api/chat/media/stream/${metadata.messageId}"
                 alt="${metadata.fileName}">
            <div class="modal-footer">
                <span>${metadata.fileName}</span>
                <button onclick="downloadFile('/api/chat/media/download/${metadata.messageId}', '${metadata.fileName}')">
                    <i class="fas fa-download"></i> Download
                </button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

function showPdfModal(metadata) {
    const modal = document.createElement('div');
    modal.className = 'pdf-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close" onclick="this.parentElement.parentElement.remove()">&times;</span>
            <iframe src="/api/chat/media/stream/${metadata.messageId}"
                    frameborder="0"></iframe>
            <div class="modal-footer">
                <span>${metadata.fileName}</span>
                <button onclick="downloadFile('/api/chat/media/download/${metadata.messageId}', '${metadata.fileName}')">
                    <i class="fas fa-download"></i> Download
                </button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}
// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Add media gallery button to chat actions
    const chatActions = document.querySelector('.chat-actions');
    if (chatActions) {
        const mediaGalleryBtn = document.createElement('button');
        mediaGalleryBtn.id = 'media-gallery-btn';
        mediaGalleryBtn.innerHTML = '<i class="fas fa-images"></i>';
        mediaGalleryBtn.title = 'Media Gallery';
        mediaGalleryBtn.onclick = function() {
            const currentRecipient = getCurrentRecipientId();
            if (currentRecipient) {
                loadConversationMedia(currentRecipient);
            }
        };
        chatActions.appendChild(mediaGalleryBtn);
    }

    // Close modals when clicking outside
    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            if (event.target.classList.contains('image-viewer-modal')) {
                closeImageViewer();
            } else if (event.target.id === 'pdf-viewer-modal') {
                closePdfViewer();
            } else if (event.target.classList.contains('media-gallery-modal')) {
                closeMediaGallery();
            }
        }
    };
});

function getCurrentRecipientId() {
    // This should return the current recipient's ID
    return document.getElementById('chat-partner-name')?.getAttribute('data-recipient-id') || '';
}