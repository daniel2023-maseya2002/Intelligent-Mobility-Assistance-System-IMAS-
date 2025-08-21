/**
 * IMAS Staff Chat Application
 * A comprehensive WhatsApp-style chat system with real-time messaging
 */

class ImasStaffChat {
    constructor() {
        // Core state
        this.currentUser = null;
        this.currentChatPartner = null;
        this.stompClient = null;
        this.contacts = [];
        this.messages = new Map();
        this.unreadMessages = {};
        this.typingUsers = new Set();
        this.onlineUsers = new Set();
        this.selectedMessages = new Set();
        this.isSelectionMode = false;

        // Configuration
        this.config = {
            apiUrl: '/api',
            websocketUrl: '/ws',
            maxFileSize: 10 * 1024 * 1024, // 10MB
            typingTimeout: 3000,
            reconnectAttempts: 5,
            reconnectDelay: 1000,
            messagePageSize: 50,
            heartbeatInterval: 30000
        };

        // State management
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.typingTimer = null;
        this.lastActivity = Date.now();
        this.heartbeatTimer = null;

        // UI elements cache
        this.elements = {};

        // Event handlers
        this.boundHandlers = {
            beforeUnload: this.handleBeforeUnload.bind(this),
            visibilityChange: this.handleVisibilityChange.bind(this),
            resize: this.handleResize.bind(this),
            online: this.handleOnline.bind(this),
            offline: this.handleOffline.bind(this)
        };

        // Initialize application
        this.init();
    }

    async init() {
        try {
            console.log('🚀 Initializing IMAS Staff Chat...');

            // Setup error handlers
            window.addEventListener('error', this.handleGlobalError.bind(this));
            window.addEventListener('unhandledrejection', this.handleUnhandledRejection.bind(this));

            // Check authentication first
            await this.checkAuthentication();

            // Initialize UI components
            this.initializeUI();

            // Load contacts
            await this.loadContacts();

            // Setup WebSocket connection
            this.connectWebSocket();

            // Setup event listeners
            this.setupEventListeners();

            // Load unread messages
            await this.loadUnreadMessages();

            // Setup heartbeat
            this.setupHeartbeat();

            // Hide loading screen
            this.hideLoadingScreen();

            console.log('✅ IMAS Staff Chat initialized successfully');
            this.showSuccess('Chat system ready!');

        } catch (error) {
            console.error('❌ Error initializing chat:', error);
            this.showError('Failed to initialize chat system');
            this.hideLoadingScreen();
        }
    }

    async checkAuthentication() {
        console.log('🔐 Checking authentication...');

        const storageKeys = [
            'userSession',
            'currentUser',
            'staff_info',
            'user_data'
        ];

        let userData = null;
        let authToken = null;

        for (const key of storageKeys) {
            userData = localStorage.getItem(key) || sessionStorage.getItem(key);
            if (userData) break;
        }

        authToken = this.getAuthToken();

        if (!userData || !authToken) {
            throw new Error('No authentication data found');
        }

        try {
            this.currentUser = JSON.parse(userData);

            if (!this.currentUser.email) {
                throw new Error('Invalid user data - missing email');
            }

            if (!this.currentUser.fullName) {
                if (this.currentUser.firstName && this.currentUser.lastName) {
                    this.currentUser.fullName = `${this.currentUser.firstName} ${this.currentUser.lastName}`;
                } else if (this.currentUser.name) {
                    this.currentUser.fullName = this.currentUser.name;
                } else {
                    this.currentUser.fullName = this.currentUser.email.split('@')[0];
                }
            }

            if (!this.currentUser.id && !this.currentUser.staffId) {
                this.currentUser.id = this.currentUser.email;
            }

            console.log('✅ User authenticated:', this.currentUser.fullName);

        } catch (error) {
            console.error('❌ Error parsing user data:', error);
            this.redirectToLogin();
        }
    }

    decodeTokenPayload(token) {
        try {
            const payload = token.split('.')[1];
            const decoded = JSON.parse(atob(payload));
            return {
                email: decoded.sub || decoded.email,
                fullName: decoded.name || decoded.fullName || decoded.sub,
                role: decoded.role || 'Staff'
            };
        } catch (error) {
            throw new Error('Invalid token format');
        }
    }

    redirectToLogin() {
        console.log('🔄 Redirecting to login...');

        localStorage.clear();
        sessionStorage.clear();

        this.showError('Session expired. Redirecting to login...');

        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
    }

    initializeUI() {
        console.log('🎨 Initializing UI...');
        this.cacheElements();
        this.updateCurrentUserUI();
        this.setupResponsiveUI();
        this.initializeEmojiPicker();
        this.setupModalHandlers();
    }

    cacheElements() {
        this.elements = {
            loadingScreen: document.getElementById('loading-screen'),
            chatContainer: document.getElementById('chat-container'),
            sidebar: document.getElementById('sidebar'),
            contactsList: document.getElementById('contacts-list'),
            contactSearch: document.getElementById('contact-search'),
            clearSearch: document.getElementById('clear-search'),
            refreshContactsBtn: document.getElementById('refresh-contacts'),
            logoutBtn: document.getElementById('logout-btn'),
            currentUserName: document.getElementById('current-user-name'),
            currentUserAvatar: document.getElementById('current-user-avatar'),
            currentUserInitials: document.getElementById('current-user-initials'),
            chatArea: document.getElementById('chat-area'),
            messagesContainer: document.getElementById('messages-container'),
            chatPartnerName: document.getElementById('chat-partner-name'),
            chatPartnerAvatar: document.getElementById('chat-partner-avatar'),
            chatPartnerInitials: document.getElementById('chat-partner-initials'),
            chatPartnerStatus: document.getElementById('chat-partner-status'),
            backBtn: document.getElementById('back-btn'),
            chatInfoBtn: document.getElementById('chat-info-btn'),
            messageInput: document.getElementById('message-input'),
            sendBtn: document.getElementById('send-btn'),
            attachBtn: document.getElementById('attach-btn'),
            emojiBtn: document.getElementById('emoji-btn'),
            fileInput: document.getElementById('file-input'),
            typingIndicator: document.getElementById('typing-indicator'),
            uploadProgress: document.getElementById('upload-progress'),
            uploadProgressBar: document.getElementById('upload-progress-bar'),
            uploadText: document.getElementById('upload-text'),
            userInfoModal: document.getElementById('user-info-modal'),
            mediaViewerModal: document.getElementById('media-viewer-modal'),
            emojiPicker: document.getElementById('emoji-picker'),
            emojiContainer: document.getElementById('emoji-container'),
            contextMenu: document.getElementById('context-menu'),
            toastContainer: document.getElementById('toast-container'),
            connectionStatus: document.getElementById('connection-status')
        };
    }

    updateCurrentUserUI() {
        if (this.elements.currentUserName) {
            this.elements.currentUserName.textContent = this.currentUser.fullName;
        }

        if (this.elements.currentUserInitials) {
            this.elements.currentUserInitials.textContent = this.getInitials(this.currentUser.fullName);
        }

        if (this.elements.currentUserAvatar && this.currentUser.photo) {
            this.updateAvatar(this.elements.currentUserAvatar, this.currentUser.photo, this.currentUser.fullName);
        }
    }

    setupResponsiveUI() {
        window.addEventListener('resize', this.boundHandlers.resize);

        if (this.elements.backBtn) {
            this.elements.backBtn.addEventListener('click', () => {
                this.showSidebar();
            });
        }

        this.handleResize();
    }

    handleResize() {
        const isMobile = window.innerWidth <= 768;

        if (this.elements.backBtn) {
            this.elements.backBtn.style.display = isMobile ? 'flex' : 'none';
        }

        if (this.elements.emojiPicker && isMobile) {
            this.elements.emojiPicker.style.position = 'fixed';
            this.elements.emojiPicker.style.bottom = '0';
            this.elements.emojiPicker.style.left = '0';
            this.elements.emojiPicker.style.right = '0';
            this.elements.emojiPicker.style.width = '100%';
        }
    }

    async loadContacts() {
        try {
            console.log('📇 Loading contacts...');

            const response = await this.apiRequest('/staff', {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const staffList = await response.json();

            this.contacts = staffList
                .filter(staff => staff.email !== this.currentUser.email)
                .map(contact => ({
                    ...contact,
                    id: this.generateSafeId(contact.staffId || contact.email),
                    email: contact.email,
                    isOnline: false,
                    lastSeen: new Date(),
                    unreadCount: 0,
                    lastMessage: '',
                    lastMessageTime: null,
                    typingIndicator: false
                }));

            console.log(`✅ Loaded ${this.contacts.length} contacts`);
            this.renderContacts();

        } catch (error) {
            console.error('❌ Error loading contacts:', error);
            this.showError('Failed to load contacts');
            this.renderContactsError();
        }
    }

    renderContacts(filter = '') {
        if (!this.elements.contactsList) return;

        const filteredContacts = this.contacts.filter(contact =>
            contact.fullName.toLowerCase().includes(filter.toLowerCase()) ||
            contact.email.toLowerCase().includes(filter.toLowerCase()) ||
            (contact.role && contact.role.toLowerCase().includes(filter.toLowerCase()))
        );

        if (filteredContacts.length === 0) {
            this.elements.contactsList.innerHTML = this.getNoContactsHTML(filter);
            return;
        }

        filteredContacts.sort((a, b) => {
            if (a.isOnline && !b.isOnline) return -1;
            if (!a.isOnline && b.isOnline) return 1;
            return (b.lastMessageTime || 0) - (a.lastMessageTime || 0);
        });

        this.elements.contactsList.innerHTML = '';

        filteredContacts.forEach(contact => {
            const contactElement = this.createContactElement(contact);
            this.elements.contactsList.appendChild(contactElement);
        });
    }

    createContactElement(contact) {
        const contactDiv = document.createElement('div');
        contactDiv.className = 'contact';
        contactDiv.dataset.id = contact.id;
        contactDiv.dataset.email = contact.email;

        if (this.currentChatPartner && this.currentChatPartner.email === contact.email) {
            contactDiv.classList.add('active');
        }

        const unreadCount = contact.unreadCount || 0;
        const lastMessage = this.formatLastMessage(contact);
        const lastMessageTime = contact.lastMessageTime ?
            this.formatTime(new Date(contact.lastMessageTime)) : '';

        contactDiv.innerHTML = `
            <div class="contact-avatar-container">
                <div class="contact-avatar" id="contact-avatar-${contact.id}">
                    <span>${this.getInitials(contact.fullName)}</span>
                </div>
                ${contact.isOnline ? '<div class="online-indicator"></div>' : ''}
            </div>
            <div class="contact-info">
                <div class="contact-header">
                    <div class="contact-name">${this.escapeHtml(contact.fullName)}</div>
                    <div class="contact-time">${lastMessageTime}</div>
                </div>
                <div class="contact-last-message">${this.escapeHtml(lastMessage)}</div>
            </div>
            <div class="contact-meta">
                ${unreadCount > 0 ? `<div class="contact-unread">${unreadCount}</div>` : ''}
            </div>
        `;

        if (contact.photo) {
            const avatarElement = contactDiv.querySelector(`#contact-avatar-${contact.id}`);
            this.updateAvatar(avatarElement, contact.photo, contact.fullName);
        }

        contactDiv.addEventListener('click', () => {
            this.openChat(contact);
        });

        return contactDiv;
    }

    formatLastMessage(contact) {
        if (contact.lastMessage) {
            return contact.lastMessage;
        }
        return contact.role || 'No messages yet';
    }

    getNoContactsHTML(filter = '') {
        if (filter) {
            return `
                <div class="no-contacts">
                    <i class="fas fa-search"></i>
                    <p>No contacts found for "${filter}"</p>
                    <small>Try a different search term</small>
                </div>
            `;
        }

        return `
            <div class="no-contacts">
                <i class="fas fa-users"></i>
                <p>No contacts available</p>
                <small>Contact your administrator</small>
            </div>
        `;
    }

    renderContactsError() {
        if (!this.elements.contactsList) return;

        this.elements.contactsList.innerHTML = `
            <div class="no-contacts">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Failed to load contacts</p>
                <button onclick="window.chatApp.loadContacts()" class="icon-btn">
                    <i class="fas fa-retry"></i> Retry
                </button>
            </div>
        `;
    }

    async openChat(contact) {
        try {
            console.log(`💬 Opening chat with ${contact.fullName}`);

            this.currentChatPartner = contact;
            this.updateChatHeader(contact);
            this.updateActiveContact(contact.id);
            this.enableMessageControls();

            await this.loadChatHistory(contact.email);
            await this.markMessagesAsRead(contact.email);

            if (window.innerWidth <= 768) {
                this.showChatArea();
            }

            setTimeout(() => {
                if (this.elements.messageInput) {
                    this.elements.messageInput.focus();
                }
            }, 100);

        } catch (error) {
            console.error('❌ Error opening chat:', error);
            this.showError('Failed to open chat');
        }
    }

    updateChatHeader(contact) {
        if (this.elements.chatPartnerName) {
            this.elements.chatPartnerName.textContent = contact.fullName;
        }

        if (this.elements.chatPartnerInitials) {
            this.elements.chatPartnerInitials.textContent = this.getInitials(contact.fullName);
        }

        if (this.elements.chatPartnerAvatar && contact.photo) {
            this.updateAvatar(this.elements.chatPartnerAvatar, contact.photo, contact.fullName);
        }

        this.updateContactStatus(contact);
    }

    updateContactStatus(contact) {
        if (!this.elements.chatPartnerStatus) return;

        if (this.typingUsers.has(contact.email)) {
            this.elements.chatPartnerStatus.textContent = 'Typing...';
            this.elements.chatPartnerStatus.className = 'chat-partner-status typing';
        } else if (contact.isOnline) {
            this.elements.chatPartnerStatus.textContent = 'Online';
            this.elements.chatPartnerStatus.className = 'chat-partner-status online';
        } else {
            this.elements.chatPartnerStatus.textContent = `Last seen ${this.formatLastSeen(contact.lastSeen)}`;
            this.elements.chatPartnerStatus.className = 'chat-partner-status offline';
        }
    }

    updateActiveContact(contactId) {
        document.querySelectorAll('.contact.active').forEach(el => {
            el.classList.remove('active');
        });

        const contactElement = document.querySelector(`[data-id="${contactId}"]`);
        if (contactElement) {
            contactElement.classList.add('active');
        }
    }

    enableMessageControls() {
        if (this.elements.messageInput) {
            this.elements.messageInput.disabled = false;
            this.elements.messageInput.placeholder = 'Type a message...';
        }

        if (this.elements.sendBtn) {
            this.elements.sendBtn.disabled = false;
        }

        if (this.elements.attachBtn) {
            this.elements.attachBtn.disabled = false;
        }
    }

    async loadChatHistory(contactEmail, page = 0) {
        try {
            console.log(`📜 Loading chat history with ${contactEmail}`);

            const response = await this.apiRequest(`/chat/history/${encodeURIComponent(contactEmail)}?page=${page}&size=${this.config.messagePageSize}`, {
                method: 'GET'
            });

            if (!response.ok) {
                if (response.status === 401) {
                    this.showError('Session expired. Please log in again.');
                    this.redirectToLogin();
                    return;
                }
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const messages = await response.json();

            this.messages.set(contactEmail, Array.isArray(messages) ? messages : []);
            this.renderMessages(Array.isArray(messages) ? messages : []);

        } catch (error) {
            console.error('❌ Error loading chat history:', error);
            this.showError('Failed to load message history');
            this.renderMessagesError();
        }
    }

    renderMessages(messages) {
        if (!this.elements.messagesContainer) return;

        this.elements.messagesContainer.innerHTML = '';

        if (!messages || messages.length === 0) {
            this.renderNoMessages();
            return;
        }

        const messagesByDate = this.groupMessagesByDate(messages);

        Object.keys(messagesByDate).forEach(date => {
            const dateSeparator = this.createDateSeparator(new Date(date));
            this.elements.messagesContainer.appendChild(dateSeparator);

            messagesByDate[date].forEach(message => {
                const messageElement = this.createMessageElement(message);
                this.elements.messagesContainer.appendChild(messageElement);
            });
        });

        this.scrollToBottom();
    }

    renderNoMessages() {
        this.elements.messagesContainer.innerHTML = `
            <div class="no-messages">
                <i class="far fa-comment-alt"></i>
                <p>No messages yet</p>
                <small>Start the conversation by sending a message</small>
            </div>
        `;
    }

    renderMessagesError() {
        this.elements.messagesContainer.innerHTML = `
            <div class="no-messages">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Failed to load messages</p>
                <button onclick="window.chatApp.loadChatHistory('${this.currentChatPartner?.email}')" class="icon-btn">
                    <i class="fas fa-retry"></i> Retry
                </button>
            </div>
        `;
    }

    groupMessagesByDate(messages) {
        const groups = {};

        messages.forEach(message => {
            const messageDate = new Date(message.timestamp);
            const dateKey = messageDate.toDateString();

            if (!groups[dateKey]) {
                groups[dateKey] = [];
            }
            groups[dateKey].push(message);
        });

        return groups;
    }

    createDateSeparator(date) {
        const separator = document.createElement('div');
        separator.className = 'date-separator';
        separator.innerHTML = `<span>${this.formatDateSeparator(date)}</span>`;
        return separator;
    }

    createMessageElement(message) {
        const messageDiv = document.createElement('div');
        const isSent = message.senderId === this.currentUser.email;

        messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;
        messageDiv.dataset.messageId = message.id;

        let messageContent = '';
        let statusIcons = '';

        switch (message.type) {
            case 'IMAGE':
                messageContent = this.createImageMessage(message);
                break;
            case 'VIDEO':
                messageContent = this.createVideoMessage(message);
                break;
            case 'AUDIO':
                messageContent = this.createAudioMessage(message);
                break;
            case 'DOCUMENT':
            case 'FILE':
                messageContent = this.createDocumentMessage(message);
                break;
            default:
                messageContent = `<div class="message-text">${this.escapeHtml(message.content)}</div>`;
        }

        if (isSent) {
            statusIcons = this.getStatusIcon(message.status || 'SENT');
        }

        const messageTime = this.formatTime(new Date(message.timestamp));

        messageDiv.innerHTML = `
            <div class="message-content">
                ${messageContent}
                <div class="message-info">
                    <span class="message-time">${messageTime}</span>
                    ${statusIcons}
                </div>
            </div>
        `;

        this.addMessageEventListeners(messageDiv, message);

        return messageDiv;
    }

    createImageMessage(message) {
        const imageUrl = message.fileUrl || message.content;
        return `
            <div class="media-message">
                <img src="${imageUrl}" alt="Image" onclick="openMediaViewer('${imageUrl}', 'image')" loading="lazy">
                ${message.caption ? `<div class="message-text">${this.escapeHtml(message.caption)}</div>` : ''}
            </div>
        `;
    }

    createVideoMessage(message) {
        const videoUrl = message.fileUrl || message.content;
        return `
            <div class="media-message">
                <video controls preload="metadata">
                    <source src="${videoUrl}" type="video/mp4">
                    Your browser does not support video playback.
                </video>
                ${message.caption ? `<div class="message-text">${this.escapeHtml(message.caption)}</div>` : ''}
            </div>
        `;
    }

    createAudioMessage(message) {
        const audioUrl = message.fileUrl || message.content;
        return `
            <div class="media-message">
                <audio controls preload="metadata">
                    <source src="${audioUrl}" type="audio/mpeg">
                    Your browser does not support audio playback.
                </audio>
                ${message.caption ? `<div class="message-text">${this.escapeHtml(message.caption)}</div>` : ''}
            </div>
        `;
    }

    createDocumentMessage(message) {
        const fileName = message.fileName || 'Document';
        const fileSize = message.fileSize ? this.formatFileSize(message.fileSize) : '';
        const downloadUrl = message.fileUrl || message.content;

        return `
            <div class="document-message">
                <div class="document-icon">
                    <i class="fas fa-file-alt"></i>
                </div>
                <div class="document-info">
                    <div class="document-name">${this.escapeHtml(fileName)}</div>
                    ${fileSize ? `<div class="document-size">${fileSize}</div>` : ''}
                    <a href="${downloadUrl}" download="${fileName}" class="download-btn">
                        <i class="fas fa-download"></i> Download
                    </a>
                </div>
            </div>
        `;
    }

    addMessageEventListeners(messageElement, message) {
        messageElement.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            this.showMessageContextMenu(e, message);
        });

        messageElement.addEventListener('dblclick', () => {
            this.replyToMessage(message);
        });
    }

    getStatusIcon(status) {
        const icons = {
            'SENDING': '<i class="fas fa-clock message-status sending"></i>',
            'SENT': '<i class="fas fa-check message-status sent"></i>',
            'DELIVERED': '<i class="fas fa-check-double message-status delivered"></i>',
            'READ': '<i class="fas fa-check-double message-status read"></i>',
            'FAILED': '<i class="fas fa-exclamation-triangle message-status failed"></i>'
        };
        return icons[status] || icons['SENT'];
    }

    setupEventListeners() {
        console.log('⚙️ Setting up event listeners...');

        if (this.elements.contactSearch) {
            this.elements.contactSearch.addEventListener('input', (e) => {
                const value = e.target.value;
                this.renderContacts(value);
                this.elements.clearSearch.style.display = value ? 'block' : 'none';
            });
        }

        if (this.elements.clearSearch) {
            this.elements.clearSearch.addEventListener('click', () => {
                this.elements.contactSearch.value = '';
                this.elements.clearSearch.style.display = 'none';
                this.renderContacts();
            });
        }

        if (this.elements.refreshContactsBtn) {
            this.elements.refreshContactsBtn.addEventListener('click', () => {
                this.loadContacts();
                this.showSuccess('Contacts refreshed');
            });
        }

        if (this.elements.logoutBtn) {
            this.elements.logoutBtn.addEventListener('click', () => {
                this.logout();
            });
        }

        if (this.elements.sendBtn) {
            this.elements.sendBtn.addEventListener('click', () => {
                this.sendMessage();
            });
        }

        if (this.elements.messageInput) {
            this.elements.messageInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });

            this.elements.messageInput.addEventListener('input', () => {
                this.handleTyping();
                this.autoResizeTextarea();
            });
        }

        if (this.elements.attachBtn && this.elements.fileInput) {
            this.elements.attachBtn.addEventListener('click', () => {
                this.elements.fileInput.click();
            });

            this.elements.fileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) {
                    this.handleFileUpload(file);
                }
            });
        }

        if (this.elements.emojiBtn) {
            this.elements.emojiBtn.addEventListener('click', () => {
                this.toggleEmojiPicker();
            });
        }

        if (this.elements.chatInfoBtn) {
            this.elements.chatInfoBtn.addEventListener('click', () => {
                this.showUserInfoModal();
            });
        }

        window.addEventListener('beforeunload', this.boundHandlers.beforeUnload);
        document.addEventListener('visibilitychange', this.boundHandlers.visibilityChange);
        window.addEventListener('online', this.boundHandlers.online);
        window.addEventListener('offline', this.boundHandlers.offline);

        document.addEventListener('click', (e) => {
            this.handleDocumentClick(e);
        });
    }

    handleDocumentClick(e) {
        if (this.elements.emojiPicker &&
            !this.elements.emojiBtn.contains(e.target) &&
            !this.elements.emojiPicker.contains(e.target)) {
            this.elements.emojiPicker.style.display = 'none';
        }

        if (this.elements.contextMenu &&
            !this.elements.contextMenu.contains(e.target)) {
            this.elements.contextMenu.style.display = 'none';
        }
    }

    async sendMessage(content = null, type = 'CHAT') {
        const messageContent = content || this.elements.messageInput.value.trim();

        if (!messageContent || !this.currentChatPartner) {
            return;
        }

        const tempMessage = {
            id: 'temp_' + Date.now(),
            senderId: this.currentUser.email,
            recipientId: this.currentChatPartner.email,
            content: messageContent,
            type: type,
            timestamp: new Date().toISOString(),
            status: 'SENDING'
        };

        this.addMessageToUI(tempMessage);

        if (!content) {
            this.elements.messageInput.value = '';
            this.autoResizeTextarea();
        }

        try {
            if (this.isConnected && this.stompClient) {
                this.stompClient.send('/app/chat.private', {}, JSON.stringify({
                    content: messageContent,
                    senderId: this.currentUser.email,
                    recipientId: this.currentChatPartner.email,
                    type: type,
                    timestamp: new Date().toISOString()
                }));
            } else {
                await this.sendMessageViaHTTP(tempMessage);
            }

            this.updateMessageStatus(tempMessage.id, 'SENT');

        } catch (error) {
            console.error('❌ Error sending message:', error);
            this.updateMessageStatus(tempMessage.id, 'FAILED');
            this.showError('Failed to send message');
        }
    }

    async sendMessageViaHTTP(message) {
        const response = await this.apiRequest('/chat/send', {
            method: 'POST',
            body: JSON.stringify({
                content: message.content,
                senderId: message.senderId,
                recipientId: message.recipientId,
                type: message.type,
                timestamp: message.timestamp
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return response.json();
    }

    addMessageToUI(message) {
        if (!this.elements.messagesContainer) return;

        const welcomeMessage = this.elements.messagesContainer.querySelector('.welcome-message, .no-messages');
        if (welcomeMessage) {
            welcomeMessage.remove();
        }

        const lastMessage = this.elements.messagesContainer.lastElementChild;
        const shouldAddDate = !lastMessage ||
            !lastMessage.classList.contains('message') ||
            this.shouldAddDateSeparator(lastMessage, message);

        if (shouldAddDate) {
            const dateSeparator = this.createDateSeparator(new Date(message.timestamp));
            this.elements.messagesContainer.appendChild(dateSeparator);
        }

        const messageElement = this.createMessageElement(message);
        this.elements.messagesContainer.appendChild(messageElement);

        this.scrollToBottom();

        if (this.currentChatPartner) {
            const messages = this.messages.get(this.currentChatPartner.email) || [];
            messages.push(message);
            this.messages.set(this.currentChatPartner.email, messages);
        }
    }

    shouldAddDateSeparator(lastElement, newMessage) {
        const lastMessageId = lastElement.dataset.messageId;
        if (!lastMessageId) return true;

        const messages = this.messages.get(this.currentChatPartner.email) || [];
        const lastMessage = messages.find(m => m.id == lastMessageId);

        if (!lastMessage) return true;

        const lastDate = new Date(lastMessage.timestamp).toDateString();
        const newDate = new Date(newMessage.timestamp).toDateString();

        return lastDate !== newDate;
    }

    updateMessageStatus(messageId, status) {
        const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
        if (!messageElement) return;

        const statusIcon = messageElement.querySelector('.message-status');
        if (statusIcon) {
            statusIcon.outerHTML = this.getStatusIcon(status);
        }
    }

    async handleFileUpload(file) {
        if (!this.currentChatPartner) {
            this.showError('Please select a contact first');
            return;
        }

        if (file.size > this.config.maxFileSize) {
            this.showError(`File is too large. Maximum size is ${this.formatFileSize(this.config.maxFileSize)}`);
            return;
        }

        let messageType = 'DOCUMENT';
        if (file.type.startsWith('image/')) {
            messageType = 'IMAGE';
        } else if (file.type.startsWith('video/')) {
            messageType = 'VIDEO';
        } else if (file.type.startsWith('audio/')) {
            messageType = 'AUDIO';
        }

        try {
            this.showUploadProgress(true, 0);

            const formData = new FormData();
            formData.append('file', file);
            formData.append('recipientId', this.currentChatPartner.email);
            formData.append('type', messageType);

            const response = await this.uploadFileWithProgress(formData);

            if (!response.ok) {
                throw new Error(`Upload failed: ${response.status}`);
            }

            const result = await response.json();
            this.showSuccess('File uploaded successfully');
            this.addMessageToUI(result);

        } catch (error) {
            console.error('❌ Error uploading file:', error);
            this.showError('Failed to upload file');
        } finally {
            this.showUploadProgress(false);
            this.elements.fileInput.value = '';
        }
    }

    async uploadFileWithProgress(formData) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();

            xhr.upload.addEventListener('progress', (e) => {
                if (e.lengthComputable) {
                    const progress = Math.round((e.loaded / e.total) * 100);
                    this.showUploadProgress(true, progress);
                }
            });

            xhr.addEventListener('load', () => {
                if (xhr.status === 401) {
                    this.showError('Session expired. Please log in again.');
                    this.redirectToLogin();
                    reject(new Error('Unauthorized'));
                } else if (xhr.status >= 200 && xhr.status < 300) {
                    resolve({
                        ok: true,
                        status: xhr.status,
                        json: () => Promise.resolve(JSON.parse(xhr.responseText))
                    });
                } else {
                    reject(new Error(`HTTP ${xhr.status}`));
                }
            });

            xhr.addEventListener('error', () => {
                reject(new Error('Network error'));
            });

            xhr.open('POST', `${this.config.apiUrl}/chat/upload-media`);
            xhr.setRequestHeader('Authorization', `Bearer ${this.getAuthToken()}`);
            xhr.send(formData);
        });
    }

    showUploadProgress(show, progress = 0) {
        if (!this.elements.uploadProgress) return;

        if (show) {
            this.elements.uploadProgress.style.display = 'block';

            if (this.elements.uploadProgressBar) {
                this.elements.uploadProgressBar.style.width = `${progress}%`;
            }

            if (this.elements.uploadText) {
                this.elements.uploadText.textContent =
                    progress > 0 ? `Uploading... ${progress}%` : 'Preparing upload...';
            }
        } else {
            this.elements.uploadProgress.style.display = 'none';
        }
    }

    async refreshToken() {
        try {
            const response = await fetch(`${this.config.apiUrl}/auth/refresh`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.getAuthToken()}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to refresh token');
            }

            const { token } = await response.json();
            localStorage.setItem('authToken', token);
            sessionStorage.setItem('authToken', token);
            console.log('✅ Token refreshed successfully');
            return token;
        } catch (error) {
            console.error('❌ Error refreshing token:', error);
            this.redirectToLogin();
            throw error;
        }
    }

    async apiRequest(endpoint, options = {}) {
        const defaultOptions = {
            headers: {
                'Authorization': `Bearer ${this.getAuthToken()}`,
                'Content-Type': 'application/json'
            }
        };

        try {
            let response = await fetch(`${this.config.apiUrl}${endpoint}`, {
                ...defaultOptions,
                ...options,
                headers: {
                    ...defaultOptions.headers,
                    ...options.headers
                }
            });

            if (response.status === 401) {
                console.log('Attempting to refresh token');
                await this.refreshToken();
                response = await fetch(`${this.config.apiUrl}${endpoint}`, {
                    ...defaultOptions,
                    ...options,
                    headers: {
                        ...defaultOptions.headers,
                        ...options.headers,
                        'Authorization': `Bearer ${this.getAuthToken()}`
                    }
                });
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return response;
        } catch (error) {
            console.error(`Error in API request to ${endpoint}:`, error);
            if (error.message.includes('401')) {
                this.redirectToLogin();
            }
            throw error;
        }
    }

    getAuthToken() {
        const token = localStorage.getItem('authToken') ||
                      localStorage.getItem('token') ||
                      sessionStorage.getItem('authToken') ||
                      sessionStorage.getItem('token');
        if (!token) {
            console.error('No auth token found, redirecting to login');
            this.redirectToLogin();
            throw new Error('No authentication token available');
        }
        return token;
    }

    connectWebSocket() {
        try {
            console.log('🔌 Connecting to WebSocket...');

            const socket = new SockJS(this.config.websocketUrl);
            this.stompClient = Stomp.over(socket);

            this.stompClient.debug = null;

            const headers = {
                'Authorization': `Bearer ${this.getAuthToken()}`
            };

            this.stompClient.connect(headers,
                (frame) => {
                    console.log('✅ WebSocket connected');
                    this.isConnected = true;
                    this.reconnectAttempts = 0;
                    this.hideConnectionStatus();

                    this.subscribeToChannels();
                    this.sendPresenceUpdate('ONLINE');
                },
                (error) => {
                    console.error('❌ WebSocket error:', error);
                    this.handleWebSocketError();
                    if (error.includes('401') || error.includes('Unauthorized')) {
                        this.showError('Session expired. Please log in again.');
                        this.redirectToLogin();
                    }
                }
            );

        } catch (error) {
            console.error('❌ Error connecting WebSocket:', error);
            this.handleWebSocketError();
        }
    }

    subscribeToChannels() {
        if (!this.stompClient || !this.isConnected) return;

        this.stompClient.subscribe('/user/queue/private', (message) => {
            try {
                const chatMessage = JSON.parse(message.body);
                this.handleIncomingMessage(chatMessage);
            } catch (error) {
                console.error('❌ Error processing message:', error);
            }
        });

        this.stompClient.subscribe('/user/queue/message.status', (message) => {
            try {
                const statusUpdate = JSON.parse(message.body);
                this.handleMessageStatusUpdate(statusUpdate);
            } catch (error) {
                console.error('❌ Error processing status update:', error);
            }
        });

        this.stompClient.subscribe('/user/queue/typing', (message) => {
            try {
                const typingData = JSON.parse(message.body);
                this.handleTypingIndicator(typingData);
            } catch (error) {
                console.error('❌ Error processing typing indicator:', error);
            }
        });

        this.stompClient.subscribe('/topic/user.status', (message) => {
            try {
                const statusUpdate = JSON.parse(message.body);
                this.handleUserStatusUpdate(statusUpdate);
            } catch (error) {
                console.error('❌ Error processing user status:', error);
            }
        });
    }

    handleIncomingMessage(message) {
        console.log('📨 Message received:', message.senderId);

        this.playNotificationSound();

        const senderId = message.senderId;

        if (this.currentChatPartner && this.currentChatPartner.email === senderId) {
            this.addMessageToUI(message);
            this.markMessageAsRead(message.id);
        } else {
            this.incrementUnreadCount(senderId);
        }

        this.updateContactLastMessage(senderId, message);

        if (document.hidden) {
            this.showBrowserNotification(message);
        }
    }

    handleMessageStatusUpdate(statusUpdate) {
        if (statusUpdate.messageId && statusUpdate.status) {
            this.updateMessageStatus(statusUpdate.messageId, statusUpdate.status);
        }
    }

    handleTypingIndicator(typingData) {
        const { senderId, isTyping } = typingData;

        if (isTyping) {
            this.typingUsers.add(senderId);
        } else {
            this.typingUsers.delete(senderId);
        }

        if (this.currentChatPartner && this.currentChatPartner.email === senderId) {
            this.updateContactStatus(this.currentChatPartner);
        }

        this.updateTypingIndicator();
    }

    handleUserStatusUpdate(statusUpdate) {
        const { userId, status } = statusUpdate;

        if (status === 'ONLINE') {
            this.onlineUsers.add(userId);
        } else {
            this.onlineUsers.delete(userId);
        }

        const contact = this.contacts.find(c => c.email === userId);
        if (contact) {
            contact.isOnline = status === 'ONLINE';
            contact.lastSeen = new Date();
        }

        this.renderContacts();

        if (this.currentChatPartner && this.currentChatPartner.email === userId) {
            this.currentChatPartner.isOnline = status === 'ONLINE';
            this.updateContactStatus(this.currentChatPartner);
        }
    }

    handleWebSocketError() {
        this.isConnected = false;
        this.showConnectionStatus('Connecting...');

        if (this.reconnectAttempts < this.config.reconnectAttempts) {
            this.reconnectAttempts++;
            const delay = this.config.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);

            console.log(`🔄 Reconnection attempt ${this.reconnectAttempts}/${this.config.reconnectAttempts} in ${delay}ms`);

            setTimeout(() => {
                this.connectWebSocket();
            }, delay);
        } else {
            console.error('❌ Unable to reconnect to WebSocket');
            this.showConnectionStatus('Connection failed');
            this.showError('Connection lost. Please refresh the page.');
        }
    }

    async loadUnreadMessages() {
        try {
            const response = await this.apiRequest('/chat/unread', {
                method: 'GET'
            });

            if (response.ok) {
                const unreadMessages = await response.json();

                this.unreadMessages = {};
                if (Array.isArray(unreadMessages)) {
                    unreadMessages.forEach(message => {
                        const senderId = message.senderId;
                        if (!this.unreadMessages[senderId]) {
                            this.unreadMessages[senderId] = 0;
                        }
                        this.unreadMessages[senderId]++;
                    });
                }

                this.updateContactsUnreadCount();
            }
        } catch (error) {
            console.error('❌ Error loading unread messages:', error);
        }
    }

    updateContactsUnreadCount() {
        this.contacts.forEach(contact => {
            contact.unreadCount = this.unreadMessages[contact.email] || 0;
        });
        this.renderContacts();
    }

    incrementUnreadCount(senderId) {
        if (!this.unreadMessages[senderId]) {
            this.unreadMessages[senderId] = 0;
        }
        this.unreadMessages[senderId]++;

        const contact = this.contacts.find(c => c.email === senderId);
        if (contact) {
            contact.unreadCount = this.unreadMessages[senderId];
        }

        this.renderContacts();
    }

    async markMessagesAsRead(contactEmail) {
        try {
            await this.apiRequest('/chat/mark-read', {
                method: 'POST',
                body: JSON.stringify({ senderId: contactEmail })
            });

            this.unreadMessages[contactEmail] = 0;

            const contact = this.contacts.find(c => c.email === contactEmail);
            if (contact) {
                contact.unreadCount = 0;
            }

            this.renderContacts();

        } catch (error) {
            console.error('❌ Error marking messages as read:', error);
        }
    }

    async markMessageAsRead(messageId) {
        try {
            await this.apiRequest(`/chat/mark-read/${messageId}`, {
                method: 'POST'
            });
        } catch (error) {
            console.error('❌ Error marking message as read:', error);
        }
    }

    updateContactLastMessage(contactEmail, message) {
        const contact = this.contacts.find(c => c.email === contactEmail);
        if (contact) {
            let lastMessage = '';
            switch (message.type) {
                case 'IMAGE':
                    lastMessage = '📷 Image';
                    break;
                case 'VIDEO':
                    lastMessage = '🎥 Video';
                    break;
                case 'AUDIO':
                    lastMessage = '🎵 Audio';
                    break;
                case 'DOCUMENT':
                case 'FILE':
                    lastMessage = '📄 Document';
                    break;
                default:
                    lastMessage = message.content.substring(0, 50);
            }

            contact.lastMessage = lastMessage;
            contact.lastMessageTime = new Date(message.timestamp);
        }

        this.renderContacts();
    }

    handleTyping() {
        if (!this.currentChatPartner) return;

        this.sendTypingIndicator(true);

        clearTimeout(this.typingTimer);

        this.typingTimer = setTimeout(() => {
            this.sendTypingIndicator(false);
        }, this.config.typingTimeout);
    }

    sendTypingIndicator(isTyping) {
        if (!this.stompClient || !this.isConnected || !this.currentChatPartner) return;

        try {
            this.stompClient.send('/app/message.typing', {}, JSON.stringify({
                senderId: this.currentUser.email,
                recipientId: this.currentChatPartner.email,
                isTyping: isTyping
            }));
        } catch (error) {
            console.error('❌ Error sending typing indicator:', error);
        }
    }

    sendPresenceUpdate(status) {
        if (!this.stompClient || !this.isConnected) return;

        try {
            this.stompClient.send('/app/user.online', {}, JSON.stringify({
                userId: this.currentUser.email,
                status: status,
                timestamp: new Date().toISOString()
            }));
        } catch (error) {
            console.error('❌ Error sending presence update:', error);
        }
    }

    autoResizeTextarea() {
        const textarea = this.elements.messageInput;
        if (!textarea) return;

        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
    }

    setupHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
        }

        this.heartbeatTimer = setInterval(() => {
            if (this.isConnected) {
                this.sendPresenceUpdate('ONLINE');
            }
        }, this.config.heartbeatInterval);
    }

    initializeEmojiPicker() {
        const emojiContainer = this.elements.emojiContainer;
        if (!emojiContainer) return;

        const emojiCategories = {
            smileys: ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣', '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓'],
            people: ['👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕', '👇', '☝️', '👋', '🤚', '🖐️', '✋', '🖖', '👏', '🙌', '🤲', '🤝', '🙏'],
            nature: ['🌸', '🌺', '🌻', '🌷', '🌹', '🌼', '🌾', '🌿', '☘️', '🍀', '🌱', '🌲', '🌳', '🌴', '🌵', '🌶️', '🍄', '🌰', '🌙', '🌛', '🌜', '🌡️', '☀️', '🌤️', '⛅', '🌥️', '☁️', '🌦️', '🌧️', '⛈️', '🌩️', '🌨️', '❄️', '☃️', '⛄', '🌬️', '💨', '💧', '💦', '☔', '☂️', '🌊', '🌫️'],
            food: ['🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🫐', '🍈', '🍒', '🍑', '🥭', '🍍', '🥥', '🥝', '🍅', '🍆', '🥑', '🥦', '🥬', '🥒', '🌶️', '🫑', '🌽', '🥕', '🫒', '🧄', '🧅', '🥔', '🍠', '🥐', '🥖', '🍞', '🥨', '🥯', '🧀', '🥚', '🍳', '🧈', '🥞', '🧇', '🥓', '🥩', '🍗', '🍖', '🦴', '🌭', '🍔', '🍟', '🍕'],
            activities: ['⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱', '🪀', '🏓', '🏸', '🏒', '🏑', '🥍', '🏏', '🪃', '🥅', '⛳', '🪁', '🏹', '🎣', '🤿', '🥊', '🥋', '🎽', '🛹', '🛷', '⛸️', '🥌', '🎿', '⛷️', '🏂'],
            travel: ['🚗', '🚕', '🚙', '🚌', '🚎', '🏎️', '🚓', '🚑', '🚒', '🚐', '🛻', '🚚', '🚛', '🚜', '🏍️', '🛵', '🚲', '🛴', '🛹', '🛼', '🚁', '🛸', '✈️', '🛩️', '🛫', '🛬', '🪂', '💺', '🚀', '�Satellite', '🚢', '⛵', '🚤', '🛥️', '🛳️', '⛴️', '🚂', '🚃', '🚄', '🚅', '🚆', '🚇', '🚈', '🚉', '🚊', '🚝', '🚞', '🚋'],
            objects: ['💡', '🔦', '🕯️', '🪔', '🧯', '🛢️', '💸', '💵', '💴', '💶', '💷', '🪙', '💰', '💳', '💎', '⚖️', '🪜', '🧰', '🔧', '🔨', '⚒️', '🛠️', '⛏️', '🪓', '🪚', '🔩', '⚙️', '🪤', '🧱', '⛓️', '🧲', '🔫', '💣', '🧨', '🪓', '🔪', '🗡️', '⚔️', '🛡️'],
            symbols: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉️', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐', '⛎', '♈', '♉', '♊', '♋', '♌', '♍', '♎', '♏', '♐', '♑', '♒', '♓']
        };

        this.loadEmojiCategory('smileys', emojiCategories);

        document.querySelectorAll('.emoji-category').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.emoji-category').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                const category = btn.dataset.category;
                this.loadEmojiCategory(category, emojiCategories);
            });
        });
    }

    loadEmojiCategory(category, emojiCategories) {
        const container = this.elements.emojiContainer;
        if (!container) return;

        const emojis = emojiCategories[category] || emojiCategories.smileys;

        container.innerHTML = '';
        emojis.forEach(emoji => {
            const emojiElement = document.createElement('div');
            emojiElement.className = 'emoji-item';
            emojiElement.textContent = emoji;
            emojiElement.addEventListener('click', () => {
                this.insertEmoji(emoji);
            });
            container.appendChild(emojiElement);
        });
    }

    toggleEmojiPicker() {
        if (!this.elements.emojiPicker) return;

        const isVisible = this.elements.emojiPicker.style.display === 'block';
        this.elements.emojiPicker.style.display = isVisible ? 'none' : 'block';
    }

    insertEmoji(emoji) {
        if (!this.elements.messageInput) return;

        const input = this.elements.messageInput;
        const start = input.selectionStart;
        const end = input.selectionEnd;
        const text = input.value;

        input.value = text.substring(0, start) + emoji + text.substring(end);
        input.focus();
        input.setSelectionRange(start + emoji.length, start + emoji.length);

        this.elements.emojiPicker.style.display = 'none';
        this.autoResizeTextarea();
    }

    setupModalHandlers() {
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('close-modal') || e.target.closest('.close-modal')) {
                const modal = e.target.closest('.modal');
                if (modal) {
                    modal.style.display = 'none';
                }
            }
        });

        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal')) {
                e.target.style.display = 'none';
            }
        });
    }

    showUserInfoModal() {
        if (!this.currentChatPartner || !this.elements.userInfoModal) return;

        const elements = {
            name: document.getElementById('modal-user-name'),
            role: document.getElementById('modal-user-role'),
            email: document.getElementById('modal-user-email'),
            phone: document.getElementById('modal-user-phone'),
            status: document.getElementById('modal-user-status'),
            avatar: document.getElementById('modal-user-avatar'),
            initials: document.getElementById('modal-user-initials')
        };

        if (elements.name) elements.name.textContent = this.currentChatPartner.fullName;
        if (elements.role) elements.role.textContent = this.currentChatPartner.role || 'Staff';
        if (elements.email) elements.email.textContent = this.currentChatPartner.email;
        if (elements.phone) elements.phone.textContent = this.currentChatPartner.phoneNumber || 'N/A';

        const statusText = this.currentChatPartner.isOnline ?
            'Online' : `Last seen ${this.formatLastSeen(this.currentChatPartner.lastSeen)}`;
        if (elements.status) elements.status.textContent = statusText;

        if (elements.initials) {
            elements.initials.textContent = this.getInitials(this.currentChatPartner.fullName);
        }

        if (elements.avatar && this.currentChatPartner.photo) {
            this.updateAvatar(elements.avatar, this.currentChatPartner.photo, this.currentChatPartner.fullName);
        }

        this.elements.userInfoModal.style.display = 'flex';
    }

    showMessageContextMenu(event, message) {
        if (!this.elements.contextMenu) return;

        this.elements.contextMenu.style.display = 'block';
        this.elements.contextMenu.style.left = event.clientX + 'px';
        this.elements.contextMenu.style.top = event.clientY + 'px';

        this.contextMessage = message;
    }

    updateTypingIndicator() {
        if (!this.elements.typingIndicator) return;

        if (this.currentChatPartner && this.typingUsers.has(this.currentChatPartner.email)) {
            this.elements.typingIndicator.style.display = 'flex';
            const avatar = this.elements.typingIndicator.querySelector('.typing-avatar span');
            const text = this.elements.typingIndicator.querySelector('.typing-text');

            if (avatar) {
                avatar.textContent = this.getInitials(this.currentChatPartner.fullName);
            }

            if (text) {
                text.textContent = `${this.currentChatPartner.fullName} is typing...`;
            }
        } else {
            this.elements.typingIndicator.style.display = 'none';
        }
    }

    showSidebar() {
        if (this.elements.sidebar && this.elements.chatArea) {
            this.elements.sidebar.classList.add('active');
            this.elements.chatArea.classList.remove('active');
        }
    }

    showChatArea() {
        if (this.elements.sidebar && this.elements.chatArea) {
            this.elements.sidebar.classList.remove('active');
            this.elements.chatArea.classList.add('active');
        }
    }

    showConnectionStatus(message) {
        if (!this.elements.connectionStatus) return;

        const content = this.elements.connectionStatus.querySelector('.connection-content span');
        if (content) {
            content.textContent = message;
        }

        this.elements.connectionStatus.style.display = 'block';
    }

    hideConnectionStatus() {
        if (this.elements.connectionStatus) {
            this.elements.connectionStatus.style.display = 'none';
        }
    }






playNotificationSound() {
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        oscillator.type = 'sine';
        oscillator.frequency.setValueAtTime(440, audioContext.currentTime);
        oscillator.connect(audioContext.destination);
        oscillator.start();
        oscillator.stop(audioContext.currentTime + 0.3);
    } catch (error) {
        console.error('❌ Error playing notification sound:', error);
    }
}

showBrowserNotification(message) {
    if (!("Notification" in window)) {
        return;
    }

    if (Notification.permission === "granted") {
        this.createNotification(message);
    } else if (Notification.permission !== "denied") {
        Notification.requestPermission().then(permission => {
            if (permission === "granted") {
                this.createNotification(message);
            }
        });
    }
}

createNotification(message) {
    const sender = this.contacts.find(c => c.email === message.senderId);
    const title = sender ? sender.fullName : 'New Message';
    const options = {
        body: message.type === 'CHAT' ? message.content : `${message.type.toLowerCase()} received`,
        icon: sender && sender.photo ? sender.photo : '/assets/default-avatar.png',
        badge: '/assets/badge.png'
    };

    const notification = new Notification(title, options);
    notification.onclick = () => {
        window.focus();
        this.openChat(sender);
    };
}

showError(message) {
    if (!this.elements.toastContainer) return;

    const toast = document.createElement('div');
    toast.className = 'toast error';
    toast.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <span>${this.escapeHtml(message)}</span>
    `;

    this.elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);
    }, 100);
}

showSuccess(message) {
    if (!this.elements.toastContainer) return;

    const toast = document.createElement('div');
    toast.className = 'toast success';
    toast.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span>${this.escapeHtml(message)}</span>
    `;

    this.elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);
    }, 100);
}

scrollToBottom() {
    if (this.elements.messagesContainer) {
        this.elements.messagesContainer.scrollTop = this.elements.messagesContainer.scrollHeight;
    }
}

updateAvatar(element, photoUrl, fullName) {
    if (!element) return;

    const initialsElement = element.querySelector('span');
    if (photoUrl) {
        element.style.backgroundImage = `url(${photoUrl})`;
        element.style.backgroundColor = 'transparent';
        if (initialsElement) {
            initialsElement.style.display = 'none';
        }
    } else {
        element.style.backgroundImage = 'none';
        element.style.backgroundColor = this.getAvatarColor(fullName);
        if (initialsElement) {
            initialsElement.style.display = 'block';
        }
    }
}

getAvatarColor(fullName) {
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEEAD', '#D4A5A5', '#9B59B6', '#3498DB'];
    let hash = 0;
    for (let i = 0; i < fullName.length; i++) {
        hash = fullName.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}

getInitials(fullName) {
    if (!fullName) return '??';
    const names = fullName.split(' ');
    return names.map(n => n.charAt(0).toUpperCase()).slice(0, 2).join('');
}

formatDateSeparator(date) {
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
        return 'Today';
    } else if (date.toDateString() === yesterday.toDateString()) {
        return 'Yesterday';
    } else {
        return date.toLocaleDateString('en-US', {
            weekday: 'long',
            month: 'long',
            day: 'numeric'
        });
    }
}

formatTime(date) {
    return date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    });
}

formatLastSeen(date) {
    const now = new Date();
    const diffInMinutes = Math.floor((now - date) / (1000 * 60));

    if (diffInMinutes < 1) {
        return 'just now';
    } else if (diffInMinutes < 60) {
        return `${diffInMinutes} minute${diffInMinutes === 1 ? '' : 's'} ago`;
    } else if (diffInMinutes < 1440) {
        const hours = Math.floor(diffInMinutes / 60);
        return `${hours} hour${hours === 1 ? '' : 's'} ago`;
    } else {
        return this.formatDateSeparator(date) + ' at ' + this.formatTime(date);
    }
}

formatFileSize(bytes) {
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    let size = bytes;
    let unitIndex = 0;

    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
    }

    return `${size.toFixed(1)} ${units[unitIndex]}`;
}

escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

generateSafeId(id) {
    return id.replace(/[^a-zA-Z0-9_-]/g, '_');
}

logout() {
    console.log('🔐 Logging out...');

    if (this.stompClient && this.isConnected) {
        this.sendPresenceUpdate('OFFLINE');
        this.stompClient.disconnect();
    }

    localStorage.clear();
    sessionStorage.clear();

    this.showSuccess('Logged out successfully');
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 1000);
}

handleGlobalError(error) {
    console.error('❌ Global error:', error);
    this.showError('An unexpected error occurred');
}

handleUnhandledRejection(event) {
    console.error('❌ Unhandled promise rejection:', event.reason);
    this.showError('An unexpected error occurred');
}

handleBeforeUnload() {
    if (this.stompClient && this.isConnected) {
        this.sendPresenceUpdate('OFFLINE');
    }
}

handleVisibilityChange() {
    if (document.hidden) {
        this.sendPresenceUpdate('AWAY');
    } else {
        this.lastActivity = Date.now();
        this.sendPresenceUpdate('ONLINE');
    }
}

handleOnline() {
    console.log('🌐 Network online');
    this.connectWebSocket();
}

handleOffline() {
    console.error('🌐 Network offline');
    this.showConnectionStatus('Offline');
    this.isConnected = false;
}

replyToMessage(message) {
    if (!this.elements.messageInput) return;

    this.elements.messageInput.value = `Replying to "${message.content.substring(0, 50)}...": `;
    this.elements.messageInput.focus();
    this.autoResizeTextarea();
}

hideLoadingScreen() {
    if (this.elements.loadingScreen) {
        this.elements.loadingScreen.style.display = 'none';
    }
}

// Expose the instance globally for debugging and external access
static initGlobal() {
    window.chatApp = new ImasStaffChat();
}
}

// Initialize the application when DOM is fully loaded
document.addEventListener('DOMContentLoaded', () => {
    ImasStaffChat.initGlobal();
});