// app.js - Main Application Code
document.addEventListener('DOMContentLoaded', function() {
    // Global variables
    let currentUser = null;
    let currentChat = null;
    let chatRooms = [];
    let staffMembers = [];
    let stompClient = null;
    let selectedAttachments = [];
    let recordingTimer = null;
    let recordingStartTime = null;

    // DOM Elements
    const chatListElement = document.getElementById('chat-list');
    const messagesContainer = document.getElementById('messages');
    const messageInput = document.getElementById('message-input');
    const sendMessageBtn = document.getElementById('send-message-btn');
    const chatArea = document.getElementById('chat-area');
    const activeChat = document.getElementById('active-chat');
    const newChatModal = document.getElementById('new-chat-modal');
    const userListElement = document.getElementById('user-list');
    const connectionStatus = document.getElementById('connection-status');
    const fileInput = document.getElementById('file-input');
    const attachmentsModal = document.getElementById('attachments-modal');
    const attachmentsPreview = document.getElementById('attachments-preview');
    const sendAttachmentsBtn = document.getElementById('send-attachments-btn');
    const mediaViewerModal = document.getElementById('media-viewer-modal');
    const mediaViewerImg = document.getElementById('media-viewer-img');
    const notificationSound = document.getElementById('notification-sound');
    const sendSound = document.getElementById('send-sound');

    // Initialize the application
    init();


function init() {
    // Load current user
    loadCurrentUser();

    // Setup event listeners
    setupEventListeners();

    // Vérifier que les dépendances sont chargées avant de se connecter
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.log('Waiting for WebSocket libraries to load...');
        setTimeout(init, 500);
        return;
    }

    // Connect to WebSocket
    connectWebSocket();

    // Load initial data
    loadInitialData();
}

    function loadCurrentUser() {
        // In a real app, this would come from your authentication system
        fetch('/api/staff/current')
            .then(response => response.json())
            .then(user => {
                currentUser = user;
                document.getElementById('user-name').textContent = user.firstName + ' ' + user.lastName;
                document.getElementById('user-avatar').src = user.photo ?
                    `data:image/jpeg;base64,${user.photo}` : 'assets/default-avatar.png';
            })
            .catch(error => {
                console.error('Error loading current user:', error);
                // Fallback for demo purposes
                currentUser = {
                    id: 1,
                    firstName: "John",
                    lastName: "Doe",
                    email: "john.doe@example.com",
                    photo: null
                };
                document.getElementById('user-name').textContent = "John Doe";
            });
    }

    function setupEventListeners() {
        // New chat button
        document.getElementById('new-chat-btn').addEventListener('click', showNewChatModal);
        document.getElementById('start-new-chat').addEventListener('click', showNewChatModal);

        // Modal controls
        document.querySelectorAll('.close-modal').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.modal').forEach(modal => {
                    modal.classList.remove('active');
                });
            });
        });

        // Back to chats button
        document.getElementById('back-to-chats').addEventListener('click', () => {
            activeChat.style.display = 'none';
            chatArea.style.display = 'flex';
            currentChat = null;
        });

        // Message input events
        messageInput.addEventListener('input', () => {
            sendMessageBtn.disabled = messageInput.value.trim() === '';
            if (!sendMessageBtn.disabled) {
                sendMessageBtn.classList.add('active');
            } else {
                sendMessageBtn.classList.remove('active');
            }
        });

        messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !sendMessageBtn.disabled) {
                sendMessage();
            }
        });

        // Send message button
        sendMessageBtn.addEventListener('click', sendMessage);

        // File attachment
        document.querySelector('.attach-btn').addEventListener('click', () => {
            fileInput.click();
        });

        fileInput.addEventListener('change', handleFileSelection);

        // Search functionality
        document.getElementById('chat-search').addEventListener('input', (e) => {
            const searchTerm = e.target.value.toLowerCase();
            const filteredChats = chatRooms.filter(chat =>
                chat.name.toLowerCase().includes(searchTerm) ||
                (chat.lastMessage && chat.lastMessage.content.toLowerCase().includes(searchTerm)));

            renderChatList(filteredChats);
        });

        document.getElementById('user-search').addEventListener('input', (e) => {
            const searchTerm = e.target.value.toLowerCase();
            const filteredUsers = staffMembers.filter(user =>
                (user.firstName + ' ' + user.lastName).toLowerCase().includes(searchTerm) ||
                user.email.toLowerCase().includes(searchTerm));

            renderUserList(filteredUsers);
        });

        // Attachments modal controls
        sendAttachmentsBtn.addEventListener('click', sendMessageWithAttachments);
        document.getElementById('cancel-attachments-btn').addEventListener('click', () => {
            attachmentsModal.classList.remove('active');
            selectedAttachments = [];
            renderAttachmentsPreview();
        });
    }



function connectWebSocket() {
    // Vérifier que Stomp est disponible
    if (typeof Stomp === 'undefined') {
        console.error('STOMP library not loaded');
        setTimeout(connectWebSocket, 1000); // Réessayer après 1 seconde
        return;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        updateConnectionStatus(true);

        // Subscribe to user's personal channel
        stompClient.subscribe(`/user/${currentUser.id}/queue/messages`, function(message) {
            const receivedMessage = JSON.parse(message.body);
            handleIncomingMessage(receivedMessage);
        });

        // Subscribe to typing notifications
        stompClient.subscribe('/topic/typing', function(notification) {
            const data = JSON.parse(notification.body);
            if (data.chatRoomId === currentChat?.id) {
                showTypingIndicator(data.userId);
            }
        });

        // Load any missed messages while disconnected
        loadUnreadMessages();

    }, function(error) {
        console.log('Error connecting to WebSocket:', error);
        updateConnectionStatus(false);
        // Try to reconnect after 5 seconds
        setTimeout(connectWebSocket, 5000);
    });
}
    function updateConnectionStatus(connected) {
        const statusElement = document.getElementById('connection-status');
        if (connected) {
            statusElement.innerHTML = '<i class="fas fa-circle"></i> <span>Connected</span>';
            statusElement.classList.add('connected');
            statusElement.classList.remove('disconnected');
        } else {
            statusElement.innerHTML = '<i class="fas fa-circle"></i> <span>Disconnected</span>';
            statusElement.classList.add('disconnected');
            statusElement.classList.remove('connected');
        }
    }

    function loadInitialData() {
        // Load chat rooms
        fetch('/api/chatrooms/user/' + currentUser.id)
            .then(response => response.json())
            .then(data => {
                chatRooms = data;
                renderChatList(chatRooms);
            })
            .catch(error => console.error('Error loading chat rooms:', error));

        // Load staff members
        fetch('/api/staff')
            .then(response => response.json())
            .then(data => {
                staffMembers = data;
                renderUserList(staffMembers);
            })
            .catch(error => console.error('Error loading staff members:', error));
    }

    function loadUnreadMessages() {
        if (!currentUser) return;

        fetch(`/api/messages/unread-total/${currentUser.id}`)
            .then(response => response.json())
            .then(count => {
                if (count > 0) {
                    // In a real app, you might show a notification badge
                    console.log(`You have ${count} unread messages`);
                }
            })
            .catch(error => console.error('Error loading unread messages:', error));
    }

    function renderChatList(chats) {
        if (!chats || chats.length === 0) {
            chatListElement.innerHTML = '<div class="chat-list-loading">No chats found</div>';
            return;
        }

        chatListElement.innerHTML = '';

        chats.forEach(chat => {
            const chatItem = document.createElement('div');
            chatItem.className = 'chat-item';
            if (currentChat && chat.id === currentChat.id) {
                chatItem.classList.add('active');
            }

            chatItem.innerHTML = `
                <div class="chat-item-avatar">
                    <img src="${chat.type === 'PRIVATE' ?
                        (getOtherUserInPrivateChat(chat)?.photo ?
                            `data:image/jpeg;base64,${getOtherUserInPrivateChat(chat).photo}` :
                            'assets/default-avatar.png') :
                        (chat.avatar ? `data:image/jpeg;base64,${chat.avatar}` : 'assets/group-avatar.png')}"
                        class="profile-img">
                    ${chat.online ? '<span class="online-indicator"></span>' : ''}
                </div>
                <div class="chat-item-info">
                    <div class="chat-item-header">
                        <span class="chat-item-name">${chat.type === 'PRIVATE' ?
                            getOtherUserInPrivateChat(chat)?.firstName + ' ' + getOtherUserInPrivateChat(chat)?.lastName :
                            chat.name}</span>
                        <span class="chat-item-time">${formatTime(chat.lastMessage?.createdAt)}</span>
                    </div>
                    <div class="chat-item-message">
                        ${getMessagePreviewIcon(chat.lastMessage)}
                        <span>${getMessagePreviewText(chat.lastMessage)}</span>
                        ${chat.unreadCount > 0 ? `<span class="unread-count">${chat.unreadCount}</span>` : ''}
                    </div>
                </div>
            `;

            chatItem.addEventListener('click', () => openChat(chat));
            chatListElement.appendChild(chatItem);
        });
    }

    function getOtherUserInPrivateChat(chat) {
        if (chat.type !== 'PRIVATE') return null;
        return chat.members.find(member => member.staff.id !== currentUser.id)?.staff;
    }

    function getMessagePreviewIcon(message) {
        if (!message) return '';

        switch(message.type) {
            case 'IMAGE': return '<i class="fas fa-image"></i>';
            case 'VIDEO': return '<i class="fas fa-video"></i>';
            case 'DOCUMENT': return '<i class="fas fa-file"></i>';
            case 'VOICE_NOTE': return '<i class="fas fa-microphone"></i>';
            case 'LOCATION': return '<i class="fas fa-map-marker-alt"></i>';
            case 'CONTACT': return '<i class="fas fa-user"></i>';
            default: return message.sender.id === currentUser.id ? '<i class="fas fa-check-double"></i>' : '';
        }
    }

    function getMessagePreviewText(message) {
        if (!message) return 'No messages yet';

        const senderPrefix = message.sender.id === currentUser.id ? 'You: ' : '';

        switch(message.type) {
            case 'IMAGE': return senderPrefix + '📷 Photo';
            case 'VIDEO': return senderPrefix + '🎥 Video';
            case 'DOCUMENT': return senderPrefix + '📄 Document';
            case 'VOICE_NOTE': return senderPrefix + '🎤 Voice message';
            case 'LOCATION': return senderPrefix + '📍 Location';
            case 'CONTACT': return senderPrefix + '👤 Contact';
            default: return senderPrefix + message.content.substring(0, 30) + (message.content.length > 30 ? '...' : '');
        }
    }

    function openChat(chat) {
        currentChat = chat;

        // Update UI
        chatArea.style.display = 'none';
        activeChat.style.display = 'flex';

        // Update chat header
        document.getElementById('chat-name').textContent =
            chat.type === 'PRIVATE' ?
            getOtherUserInPrivateChat(chat)?.firstName + ' ' + getOtherUserInPrivateChat(chat)?.lastName :
            chat.name;

        document.getElementById('chat-avatar').src =
            chat.type === 'PRIVATE' ?
            (getOtherUserInPrivateChat(chat)?.photo ?
                `data:image/jpeg;base64,${getOtherUserInPrivateChat(chat).photo}` :
                'assets/default-avatar.png') :
            (chat.avatar ? `data:image/jpeg;base64,${chat.avatar}` : 'assets/group-avatar.png');

        document.getElementById('chat-status').textContent =
            chat.type === 'PRIVATE' ?
            (getOtherUserInPrivateChat(chat)?.online ? 'Online' : 'Offline') :
            `${chat.memberCount} members`;

        // Load messages
        loadMessages(chat.id);

        // Mark messages as read
        markMessagesAsRead(chat.id);
    }

    function loadMessages(chatRoomId) {
        fetch(`/api/messages/chatroom/${chatRoomId}?page=0&size=50`)
            .then(response => response.json())
            .then(page => {
                messagesContainer.innerHTML = '';
                page.content.forEach(message => {
                    renderMessage(message);
                });

                // Scroll to bottom
                setTimeout(() => {
                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                }, 100);
            })
            .catch(error => console.error('Error loading messages:', error));
    }

    function renderMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.className = `message ${message.sender.id === currentUser.id ? 'sent' : 'received'}`;

        let contentHtml = '';

        switch(message.type) {
            case 'TEXT':
                contentHtml = `<div class="message-content">${message.content}</div>`;
                break;

            case 'IMAGE':
                contentHtml = `
                    <div class="message-content">${message.content || ''}</div>
                    <div class="attachment-container">
                        <img src="/api/messages/attachments/${message.attachments[0].id}/content"
                             class="attachment-image"
                             onclick="openMediaViewer('/api/messages/attachments/${message.attachments[0].id}/content',
                                 '${message.sender.firstName} ${message.sender.lastName}',
                                 '${formatTime(message.createdAt)}')">
                    </div>
                `;
                break;

            case 'DOCUMENT':
                contentHtml = `
                    <div class="message-content">${message.content || ''}</div>
                    <div class="attachment-document" onclick="window.open('/api/messages/attachments/${message.attachments[0].id}/content')">
                        <i class="fas fa-file-alt"></i>
                        <div class="attachment-document-info">
                            <div class="attachment-document-name">${message.attachments[0].originalFileName}</div>
                            <div class="attachment-document-size">${formatFileSize(message.attachments[0].fileSize)}</div>
                        </div>
                    </div>
                `;
                break;

            case 'VOICE_NOTE':
                contentHtml = `
                    <div class="message-content">${message.content || ''}</div>
                    <div class="attachment-audio">
                        <i class="fas fa-microphone"></i>
                        <div class="audio-player">
                            <progress value="0" max="100"></progress>
                            <div class="audio-controls">
                                <button class="audio-play-btn" onclick="playAudio(this, '/api/messages/attachments/${message.attachments[0].id}/content')">
                                    <i class="fas fa-play"></i>
                                </button>
                                <span class="audio-time">${formatDuration(message.attachments[0].duration)}</span>
                            </div>
                        </div>
                    </div>
                `;
                break;

            case 'LOCATION':
                const locationData = message.content.split('\n');
                contentHtml = `
                    <div class="location-container">
                        <div class="location-header">
                            <i class="fas fa-map-marker-alt"></i>
                            <span class="location-title">Location</span>
                        </div>
                        <div class="location-address">${locationData[0]}</div>
                        <div class="location-map">
                            <img src="https://maps.googleapis.com/maps/api/staticmap?center=${locationData[1]},${locationData[2]}&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7C${locationData[1]},${locationData[2]}&key=YOUR_API_KEY"
                                 alt="Location">
                        </div>
                        <div class="location-action" onclick="window.open('https://www.google.com/maps?q=${locationData[1]},${locationData[2]}')">
                            Open in Maps
                        </div>
                    </div>
                `;
                break;

            case 'CONTACT':
                const contactData = message.content.split('\n');
                contentHtml = `
                    <div class="contact-container">
                        <div class="contact-header">
                            <img src="assets/default-avatar.png" class="contact-avatar">
                            <div class="contact-info">
                                <div class="contact-name">${contactData[0].replace('Contact: ', '')}</div>
                                <div class="contact-phone">${contactData[1].replace('Phone: ', '')}</div>
                            </div>
                        </div>
                        <div class="contact-actions">
                            <button class="contact-action-btn" onclick="window.location.href='tel:${contactData[1].replace('Phone: ', '')}'">
                                <i class="fas fa-phone"></i> Call
                            </button>
                            <button class="contact-action-btn" onclick="window.location.href='sms:${contactData[1].replace('Phone: ', '')}'">
                                <i class="fas fa-sms"></i> SMS
                            </button>
                        </div>
                    </div>
                `;
                break;

            default:
                contentHtml = `<div class="message-content">${message.content}</div>`;
        }

        messageElement.innerHTML = `
            ${message.replyTo ? `
                <div class="message-reply">
                    <div class="reply-info">Replying to ${message.replyTo.sender.firstName}</div>
                    <div class="reply-content">${message.replyTo.content.substring(0, 50)}${message.replyTo.content.length > 50 ? '...' : ''}</div>
                </div>
            ` : ''}
            ${contentHtml}
            <div class="message-info">
                <span class="message-time">${formatTime(message.createdAt)}</span>
                ${message.sender.id === currentUser.id ? `
                    <span class="message-status">
                        ${message.readBy.length > 0 ? '<i class="fas fa-check-double" style="color: #4fc3f7;"></i>' :
                         message.deliveredTo.length > 0 ? '<i class="fas fa-check-double"></i>' :
                         '<i class="fas fa-check"></i>'}
                    </span>
                ` : ''}
            </div>
        `;

        messagesContainer.appendChild(messageElement);
    }

    function markMessagesAsRead(chatRoomId) {
        if (!currentUser) return;

        fetch(`/api/messages/chatroom/${chatRoomId}/read-all/${currentUser.id}`, {
            method: 'POST'
        })
        .catch(error => console.error('Error marking messages as read:', error));
    }

    function sendMessage() {
        const content = messageInput.value.trim();
        if (!content || !currentChat || !currentUser) return;

        const message = {
            chatRoomId: currentChat.id,
            senderId: currentUser.id,
            content: content,
            type: 'TEXT'
        };

        fetch('/api/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(message)
        })
        .then(response => response.json())
        .then(sentMessage => {
            // Play send sound
            sendSound.play();

            // Clear input
            messageInput.value = '';
            sendMessageBtn.disabled = true;
            sendMessageBtn.classList.remove('active');

            // Add to UI immediately (optimistic update)
            renderMessage(sentMessage);

            // Scroll to bottom
            setTimeout(() => {
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }, 100);

            // Update last message in chat list
            updateChatLastMessage(currentChat.id, sentMessage);
        })
        .catch(error => console.error('Error sending message:', error));
    }

    function sendMessageWithAttachments() {
        if (!currentChat || !currentUser || selectedAttachments.length === 0) return;

        const formData = new FormData();
        formData.append('chatRoomId', currentChat.id);
        formData.append('senderId', currentUser.id);
        formData.append('type', getAttachmentType(selectedAttachments[0]));

        // Add all selected files
        selectedAttachments.forEach(file => {
            formData.append('files', file);
        });

        fetch('/api/messages/with-multiple-attachments', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(sentMessage => {
            // Play send sound
            sendSound.play();

            // Clear attachments
            selectedAttachments = [];
            renderAttachmentsPreview();
            attachmentsModal.classList.remove('active');

            // Add to UI
            renderMessage(sentMessage);

            // Scroll to bottom
            setTimeout(() => {
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }, 100);

            // Update last message in chat list
            updateChatLastMessage(currentChat.id, sentMessage);
        })
        .catch(error => console.error('Error sending message with attachments:', error));
    }

    function handleFileSelection(event) {
        const files = Array.from(event.target.files);
        if (files.length === 0) return;

        // For demo, we'll just show the preview
        selectedAttachments = [...selectedAttachments, ...files];
        renderAttachmentsPreview();

        // Show attachments modal
        attachmentsModal.classList.add('active');

        // Clear file input
        event.target.value = '';
    }

    function renderAttachmentsPreview() {
        attachmentsPreview.innerHTML = '';

        if (selectedAttachments.length === 0) {
            attachmentsPreview.innerHTML = '<p>No attachments selected</p>';
            return;
        }

        selectedAttachments.forEach((file, index) => {
            const previewElement = document.createElement('div');
            previewElement.className = 'attachment-preview';

            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    previewElement.innerHTML = `
                        <img src="${e.target.result}" class="attachment-preview-image">
                        <div class="attachment-preview-info">
                            <div class="attachment-preview-name">${file.name}</div>
                            <div class="attachment-preview-size">${formatFileSize(file.size)}</div>
                        </div>
                        <button class="attachment-preview-remove" onclick="removeAttachment(${index})">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                };
                reader.readAsDataURL(file);
            } else {
                previewElement.innerHTML = `
                    <i class="fas fa-file-alt"></i>
                    <div class="attachment-preview-info">
                        <div class "attachment-preview-name">${file.name}</div>
                        <div class="attachment-preview-size">${formatFileSize(file.size)}</div>
                    </div>
                    <button class="attachment-preview-remove" onclick="removeAttachment(${index})">
                        <i class="fas fa-times"></i>
                    </button>
                `;
            }

            attachmentsPreview.appendChild(previewElement);
        });
    }

    function removeAttachment(index) {
        selectedAttachments.splice(index, 1);
        renderAttachmentsPreview();
    }

    function getAttachmentType(file) {
        if (file.type.startsWith('image/')) return 'IMAGE';
        if (file.type.startsWith('video/')) return 'VIDEO';
        if (file.type.startsWith('audio/')) return 'VOICE_NOTE';
        return 'DOCUMENT';
    }

    function handleIncomingMessage(message) {
        // Play notification sound if message is not from current user
        if (message.sender.id !== currentUser.id) {
            notificationSound.play();
        }

        // If message is for the current chat, display it
        if (currentChat && message.chatRoom.id === currentChat.id) {
            renderMessage(message);

            // Scroll to bottom
            setTimeout(() => {
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }, 100);

            // Mark as read
            if (message.sender.id !== currentUser.id) {
                fetch(`/api/messages/${message.id}/read/${currentUser.id}`, {
                    method: 'POST'
                })
                .catch(error => console.error('Error marking message as read:', error));
            }
        }

        // Update last message in chat list
        updateChatLastMessage(message.chatRoom.id, message);
    }

    function updateChatLastMessage(chatRoomId, message) {
        const chatIndex = chatRooms.findIndex(chat => chat.id === chatRoomId);
        if (chatIndex !== -1) {
            chatRooms[chatIndex].lastMessage = message;

            // If the message is not from current user, increment unread count
            if (message.sender.id !== currentUser.id) {
                if (!chatRooms[chatIndex].unreadCount) {
                    chatRooms[chatIndex].unreadCount = 0;
                }
                chatRooms[chatIndex].unreadCount++;
            }

            // Move chat to top of the list
            const updatedChat = chatRooms.splice(chatIndex, 1)[0];
            chatRooms.unshift(updatedChat);

            // Update UI
            renderChatList(chatRooms);
        }
    }

    function showTypingIndicator(userId) {
        const user = staffMembers.find(u => u.id === userId);
        if (!user) return;

        // Remove any existing typing indicator
        const existingIndicator = document.querySelector('.typing-indicator');
        if (existingIndicator) {
            existingIndicator.remove();
        }

        // Add new typing indicator
        const typingIndicator = document.createElement('div');
        typingIndicator.className = 'typing-indicator';
        typingIndicator.innerHTML = `
            ${user.firstName} is typing
            <div class="typing-dots">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
            </div>
        `;

        messagesContainer.appendChild(typingIndicator);

        // Scroll to bottom
        setTimeout(() => {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }, 100);

        // Remove after 3 seconds
        setTimeout(() => {
            if (typingIndicator.parentNode) {
                typingIndicator.remove();
            }
        }, 3000);
    }

    function sendTypingIndicator() {
        if (!stompClient || !currentChat || !currentUser) return;

        stompClient.send("/app/typing", {}, JSON.stringify({
            chatRoomId: currentChat.id,
            userId: currentUser.id
        }));
    }

    function showNewChatModal() {
        newChatModal.classList.add('active');
    }

    function renderUserList(users) {
        userListElement.innerHTML = '';

        if (!users || users.length === 0) {
            userListElement.innerHTML = '<p>No users found</p>';
            return;
        }

        // Filter out current user
        const filteredUsers = users.filter(user => user.id !== currentUser.id);

        filteredUsers.forEach(user => {
            const userItem = document.createElement('div');
            userItem.className = 'user-item';

            userItem.innerHTML = `
                <div class="user-item-avatar">
                    <img src="${user.photo ? `data:image/jpeg;base64,${user.photo}` : 'assets/default-avatar.png'}"
                         class="profile-img">
                    ${user.online ? '<span class="online-indicator"></span>' : ''}
                </div>
                <div class="user-item-info">
                    <div class="user-item-name">${user.firstName} ${user.lastName}</div>
                    <div class="user-item-role">${user.role}</div>
                </div>
                <button class="user-item-action" onclick="startPrivateChat(${user.id})">
                    Chat
                </button>
            `;

            userListElement.appendChild(userItem);
        });
    }

    function startPrivateChat(userId) {
        const user = staffMembers.find(u => u.id === userId);
        if (!user) return;

        // Check if a private chat already exists with this user
        const existingChat = chatRooms.find(chat =>
            chat.type === 'PRIVATE' &&
            chat.members.some(member => member.staff.id === userId));

        if (existingChat) {
            // Open existing chat
            openChat(existingChat);
            newChatModal.classList.remove('active');
            return;
        }




// Create new private chat
        fetch('/api/chatrooms/private', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                user1Id: currentUser.id,
                user2Id: userId
            })
        })
        .then(response => response.json())
        .then(chat => {
            // Add to chat list
            chatRooms.unshift(chat);
            renderChatList(chatRooms);

            // Open the new chat
            openChat(chat);

            // Close modal
            newChatModal.classList.remove('active');
        })
        .catch(error => console.error('Error creating private chat:', error));
    }

    // Helper functions
    function formatTime(dateString) {
        if (!dateString) return '';

        const date = new Date(dateString);
        const now = new Date();

        if (date.toDateString() === now.toDateString()) {
            // Today - show time
            return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        } else if (date.getFullYear() === now.getFullYear()) {
            // This year - show month and day
            return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
        } else {
            // Older - show full date
            return date.toLocaleDateString();
        }
    }

    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';

        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    function formatDuration(seconds) {
        if (!seconds) return '00:00';

        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);

        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    // Global functions (accessible from HTML)
    window.openMediaViewer = function(src, sender, time) {
        mediaViewerImg.src = src;
        document.getElementById('media-sender').textContent = sender;
        document.getElementById('media-time').textContent = time;
        mediaViewerModal.classList.add('active');
    };

    window.playAudio = function(button, src) {
        const audio = new Audio(src);
        const progress = button.parentElement.previousElementSibling;

        button.onclick = null; // Prevent multiple clicks
        button.innerHTML = '<i class="fas fa-pause"></i>';

        audio.play();

        audio.addEventListener('timeupdate', () => {
            progress.value = (audio.currentTime / audio.duration) * 100;
        });

        audio.addEventListener('ended', () => {
            button.innerHTML = '<i class="fas fa-play"></i>';
            button.onclick = function() { playAudio(this, src); };
            progress.value = 0;
        });

        button.onclick = function() {
            audio.pause();
            button.innerHTML = '<i class="fas fa-play"></i>';
            button.onclick = function() { playAudio(this, src); };
        };
    };

    window.removeAttachment = function(index) {
        removeAttachment(index);
    };

    window.startPrivateChat = function(userId) {
        startPrivateChat(userId);
    };

    // Additional utility functions for enhanced functionality
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Typing indicator with debounce
    const debouncedTypingIndicator = debounce(sendTypingIndicator, 1000);

    // Add typing event to message input
    messageInput.addEventListener('input', () => {
        if (messageInput.value.trim().length > 0) {
            debouncedTypingIndicator();
        }
    });

    // Online/Offline status handling
    window.addEventListener('online', () => {
        console.log('Connection restored');
        if (!stompClient || !stompClient.connected) {
            connectWebSocket();
        }
        updateConnectionStatus(true);
    });

    window.addEventListener('offline', () => {
        console.log('Connection lost');
        updateConnectionStatus(false);
    });

    // Reconnection logic
    function handleReconnection() {
        if (!stompClient || !stompClient.connected) {
            console.log('Attempting to reconnect...');
            setTimeout(() => {
                connectWebSocket();
            }, 5000);
        }
    }

    // Call reconnection handler when needed
    setInterval(() => {
        if (navigator.onLine && (!stompClient || !stompClient.connected)) {
            handleReconnection();
        }
    }, 10000);

    // Notification permission request
    function requestNotificationPermission() {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission().then(permission => {
                console.log('Notification permission:', permission);
            });
        }
    }

    // Show desktop notification for new messages
    function showDesktopNotification(message) {
        if ('Notification' in window && Notification.permission === 'granted') {
            const notification = new Notification(`New message from ${message.sender.firstName}`, {
                body: getMessagePreviewText(message),
                icon: '/assets/app-icon.png',
                tag: 'chat-message'
            });

            notification.onclick = function() {
                window.focus();
                if (message.chatRoom.id !== currentChat?.id) {
                    const chat = chatRooms.find(c => c.id === message.chatRoom.id);
                    if (chat) {
                        openChat(chat);
                    }
                }
                notification.close();
            };

            setTimeout(() => notification.close(), 5000);
        }
    }

    // Request notification permission on app load
    requestNotificationPermission();

    // Enhanced message handling with notifications
    function handleIncomingMessageEnhanced(message) {
        handleIncomingMessage(message);

        // Show desktop notification if not in current chat or window not focused
        if (message.sender.id !== currentUser.id &&
            (!currentChat || message.chatRoom.id !== currentChat.id || !document.hasFocus())) {
            showDesktopNotification(message);
        }
    }

    // Scroll to bottom with smooth behavior option
    function scrollToBottom(smooth = false) {
        messagesContainer.scrollTo({
            top: messagesContainer.scrollHeight,
            behavior: smooth ? 'smooth' : 'auto'
        });
    }

    // Message context menu functionality
    function showMessageContextMenu(event, messageElement, message) {
        event.preventDefault();

        // Remove existing context menu
        const existingMenu = document.querySelector('.message-context-menu');
        if (existingMenu) {
            existingMenu.remove();
        }

        const contextMenu = document.createElement('div');
        contextMenu.className = 'message-context-menu';
        contextMenu.style.left = event.pageX + 'px';
        contextMenu.style.top = event.pageY + 'px';

        const menuItems = [];

        // Reply option
        menuItems.push({
            icon: 'fas fa-reply',
            text: 'Reply',
            action: () => startReply(message)
        });

        // Copy option (for text messages)
        if (message.type === 'TEXT') {
            menuItems.push({
                icon: 'fas fa-copy',
                text: 'Copy',
                action: () => navigator.clipboard.writeText(message.content)
            });
        }

        // Delete option (only for own messages)
        if (message.sender.id === currentUser.id) {
            menuItems.push({
                icon: 'fas fa-trash',
                text: 'Delete',
                action: () => deleteMessage(message.id)
            });
        }

        contextMenu.innerHTML = menuItems.map(item => `
            <div class="context-menu-item" onclick="${item.action.name}()">
                <i class="${item.icon}"></i>
                <span>${item.text}</span>
            </div>
        `).join('');

        document.body.appendChild(contextMenu);

        // Remove menu when clicking outside
        setTimeout(() => {
            document.addEventListener('click', function removeMenu() {
                contextMenu.remove();
                document.removeEventListener('click', removeMenu);
            });
        }, 100);
    }

    // Reply functionality
    let replyingTo = null;

    function startReply(message) {
        replyingTo = message;
        showReplyPreview(message);
    }

    function showReplyPreview(message) {
        const existingPreview = document.querySelector('.reply-preview');
        if (existingPreview) {
            existingPreview.remove();
        }

        const replyPreview = document.createElement('div');
        replyPreview.className = 'reply-preview';
        replyPreview.innerHTML = `
            <div class="reply-preview-content">
                <div class="reply-preview-header">
                    <i class="fas fa-reply"></i>
                    <span>Replying to ${message.sender.firstName}</span>
                    <button class="reply-preview-close" onclick="cancelReply()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="reply-preview-message">${message.content.substring(0, 100)}${message.content.length > 100 ? '...' : ''}</div>
            </div>
        `;

        messageInput.parentElement.insertBefore(replyPreview, messageInput.parentElement.firstChild);
        messageInput.focus();
    }

    window.cancelReply = function() {
        replyingTo = null;
        const replyPreview = document.querySelector('.reply-preview');
        if (replyPreview) {
            replyPreview.remove();
        }
    };

    // Enhanced send message with reply support
    function sendMessageEnhanced() {
        const content = messageInput.value.trim();
        if (!content || !currentChat || !currentUser) return;

        const message = {
            chatRoomId: currentChat.id,
            senderId: currentUser.id,
            content: content,
            type: 'TEXT',
            replyToId: replyingTo ? replyingTo.id : null
        };

        fetch('/api/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(message)
        })
        .then(response => response.json())
        .then(sentMessage => {
            // Play send sound
            sendSound.play();

            // Clear input and reply
            messageInput.value = '';
            sendMessageBtn.disabled = true;
            sendMessageBtn.classList.remove('active');
            cancelReply();

            // Add to UI immediately (optimistic update)
            renderMessage(sentMessage);

            // Scroll to bottom
            scrollToBottom(true);

            // Update last message in chat list
            updateChatLastMessage(currentChat.id, sentMessage);
        })
        .catch(error => console.error('Error sending message:', error));
    }

    // Message deletion
    function deleteMessage(messageId) {
        if (!confirm('Are you sure you want to delete this message?')) {
            return;
        }

        fetch(`/api/messages/${messageId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                // Remove message from UI
                const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
                if (messageElement) {
                    messageElement.remove();
                }
            }
        })
        .catch(error => console.error('Error deleting message:', error));
    }

    // Message search functionality
    function searchMessages(query) {
        if (!currentChat || !query.trim()) return;

        fetch(`/api/messages/search?chatRoomId=${currentChat.id}&query=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(messages => {
                highlightSearchResults(messages, query);
            })
            .catch(error => console.error('Error searching messages:', error));
    }

    function highlightSearchResults(messages, query) {
        // Clear existing highlights
        document.querySelectorAll('.message-highlight').forEach(el => {
            el.classList.remove('message-highlight');
        });

        // Highlight matching messages
        messages.forEach(message => {
            const messageElement = document.querySelector(`[data-message-id="${message.id}"]`);
            if (messageElement) {
                messageElement.classList.add('message-highlight');

                // Scroll to first result
                if (messages.indexOf(message) === 0) {
                    messageElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }

    // File drag and drop functionality
    function setupDragAndDrop() {
        const dropZone = document.getElementById('messages');

        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('drag-over');
        });

        dropZone.addEventListener('dragleave', (e) => {
            e.preventDefault();
            dropZone.classList.remove('drag-over');
        });

        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('drag-over');

            const files = Array.from(e.dataTransfer.files);
            if (files.length > 0) {
                selectedAttachments = [...selectedAttachments, ...files];
                renderAttachmentsPreview();
                attachmentsModal.classList.add('active');
            }
        });
    }

    // Voice message recording
    let mediaRecorder = null;
    let audioChunks = [];

    function startVoiceRecording() {
        navigator.mediaDevices.getUserMedia({ audio: true })
            .then(stream => {
                mediaRecorder = new MediaRecorder(stream);
                audioChunks = [];

                mediaRecorder.ondataavailable = event => {
                    audioChunks.push(event.data);
                };

                mediaRecorder.onstop = () => {
                    const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
                    sendVoiceMessage(audioBlob);
                    stream.getTracks().forEach(track => track.stop());
                };

                mediaRecorder.start();
                recordingStartTime = Date.now();
                updateRecordingTimer();
            })
            .catch(error => {
                console.error('Error accessing microphone:', error);
                alert('Unable to access microphone. Please check your permissions.');
            });
    }

    function stopVoiceRecording() {
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            mediaRecorder.stop();
            clearInterval(recordingTimer);
        }
    }

    function updateRecordingTimer() {
        recordingTimer = setInterval(() => {
            const elapsed = Date.now() - recordingStartTime;
            const minutes = Math.floor(elapsed / 60000);
            const seconds = Math.floor((elapsed % 60000) / 1000);

            const timerElement = document.getElementById('recording-timer');
            if (timerElement) {
                timerElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
            }
        }, 1000);
    }

    function sendVoiceMessage(audioBlob) {
        if (!currentChat || !currentUser) return;

        const formData = new FormData();
        formData.append('chatRoomId', currentChat.id);
        formData.append('senderId', currentUser.id);
        formData.append('type', 'VOICE_NOTE');
        formData.append('files', audioBlob, 'voice-message.wav');

        fetch('/api/messages/with-attachment', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(sentMessage => {
            renderMessage(sentMessage);
            scrollToBottom(true);
            updateChatLastMessage(currentChat.id, sentMessage);
        })
        .catch(error => console.error('Error sending voice message:', error));
    }

    // Initialize drag and drop
    setupDragAndDrop();

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        // Ctrl/Cmd + Enter to send message
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            if (!sendMessageBtn.disabled) {
                sendMessage();
            }
        }

        // Escape to cancel reply
        if (e.key === 'Escape' && replyingTo) {
            cancelReply();
        }

        // Ctrl/Cmd + F to search messages
        if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
            e.preventDefault();
            const searchInput = document.getElementById('message-search');
            if (searchInput) {
                searchInput.focus();
            }
        }
    });

    // Auto-save draft messages
    function saveDraft() {
        if (currentChat && messageInput.value.trim()) {
            localStorage.setItem(`draft_${currentChat.id}`, messageInput.value);
        }
    }

    function loadDraft() {
        if (currentChat) {
            const draft = localStorage.getItem(`draft_${currentChat.id}`);
            if (draft) {
                messageInput.value = draft;
                messageInput.dispatchEvent(new Event('input'));
            }
        }
    }

    function clearDraft() {
        if (currentChat) {
            localStorage.removeItem(`draft_${currentChat.id}`);
        }
    }

    // Auto-save drafts periodically
    setInterval(saveDraft, 2000);

    // Load draft when opening chat (modify openChat function)
    const originalOpenChat = openChat;
    openChat = function(chat) {
        originalOpenChat(chat);
        setTimeout(loadDraft, 100);
    };

    // Clear draft when sending message (modify sendMessage function)
    const originalSendMessage = sendMessage;
    sendMessage = function() {
        originalSendMessage();
        clearDraft();
    };

    // Message status updates via WebSocket
    if (stompClient) {
        stompClient.subscribe('/topic/message-status', function(statusUpdate) {
            const data = JSON.parse(statusUpdate.body);
            updateMessageStatus(data.messageId, data.status, data.userId);
        });
    }

    function updateMessageStatus(messageId, status, userId) {
        const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
        if (messageElement) {
            const statusElement = messageElement.querySelector('.message-status i');
            if (statusElement) {
                switch(status) {
                    case 'DELIVERED':
                        statusElement.className = 'fas fa-check-double';
                        break;
                    case 'READ':
                        statusElement.className = 'fas fa-check-double';
                        statusElement.style.color = '#4fc3f7';
                        break;
                }
            }
        }
    }

    // Performance optimization: Virtual scrolling for large message lists
    function setupVirtualScrolling() {
        let isLoading = false;

        messagesContainer.addEventListener('scroll', () => {
            if (messagesContainer.scrollTop === 0 && !isLoading && currentChat) {
                isLoading = true;
                loadMoreMessages(currentChat.id);
            }
        });
    }

    function loadMoreMessages(chatRoomId, page = 1) {
        fetch(`/api/messages/chatroom/${chatRoomId}?page=${page}&size=20`)
            .then(response => response.json())
            .then(pageData => {
                if (pageData.content && pageData.content.length > 0) {
                    const oldHeight = messagesContainer.scrollHeight;

                    // Prepend messages to the beginning
                    pageData.content.reverse().forEach(message => {
                        const messageElement = createMessageElement(message);
                        messagesContainer.insertBefore(messageElement, messagesContainer.firstChild);
                    });

                    // Maintain scroll position
                    const newHeight = messagesContainer.scrollHeight;
                    messagesContainer.scrollTop = newHeight - oldHeight;
                }
                isLoading = false;
            })
            .catch(error => {
                console.error('Error loading more messages:', error);
                isLoading = false;
            });
    }

    // Initialize virtual scrolling
    setupVirtualScrolling();

    // Export functions for global access
    window.deleteMessage = deleteMessage;
    window.startReply = startReply;
    window.searchMessages = searchMessages;
    window.startVoiceRecording = startVoiceRecording;
    window.stopVoiceRecording = stopVoiceRecording;

});

// SessionTracker.js - Track user session activity
class SessionTracker {
    constructor() {
        this.timeout = null;
        this.timeoutDuration = 30 * 60 * 1000; // 30 minutes
        this.events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];

        this.setupEventListeners();
        this.resetTimeout();
    }

    setupEventListeners() {
        this.events.forEach(event => {
            document.addEventListener(event, () => this.resetTimeout());
        });
    }

    resetTimeout() {
        clearTimeout(this.timeout);
        this.timeout = setTimeout(() => this.logout(), this.timeoutDuration);
    }

    logout() {
        // In a real app, you would make an API call to logout
        console.log('User logged out due to inactivity');
        window.location.href = '/logout';
    }
}

// Initialize session tracker
new SessionTracker();

// user_session_handler.js - Handle user session across tabs
document.addEventListener('DOMContentLoaded', function() {
    // Listen for storage events (changes from other tabs)
    window.addEventListener('storage', function(event) {
        if (event.key === 'user_session_event') {
            const data = JSON.parse(event.newValue);
            if (data.type === 'logout') {
                // Another tab logged out
                window.location.href = '/login';
            }
        }
    });

    // Function to notify other tabs of logout
    function notifyLogout() {
        localStorage.setItem('user_session_event', JSON.stringify({
            type: 'logout',
            timestamp: new Date().getTime()
        }));
    }

    // Enhanced session management
    function syncSessionAcrossTabs() {
        // Heartbeat to indicate this tab is active
        setInterval(() => {
            localStorage.setItem('tab_heartbeat', JSON.stringify({
                timestamp: Date.now(),
                tabId: generateTabId()
            }));
        }, 5000);
    }

    function generateTabId() {
        return 'tab_' + Math.random().toString(36).substr(2, 9);
    }

    // Initialize session sync
    syncSessionAcrossTabs();

    // In a real app, you would call notifyLogout() when the user logs out
    window.notifyLogout = notifyLogout;
});