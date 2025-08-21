// Gestion des emojis et des fichiers
document.addEventListener('DOMContentLoaded', function() {
    // Références aux éléments
    const emojiBtn = document.getElementById('emoji-btn');
    const emojiPicker = document.getElementById('emoji-picker');
    const messageInput = document.getElementById('message-input');
    const attachBtn = document.getElementById('attach-btn');
    const fileInput = document.getElementById('file-input');

    // Vérification que les éléments existent
    if (!emojiBtn || !emojiPicker || !messageInput || !attachBtn || !fileInput) {
        console.error('Certains éléments requis sont manquants dans le DOM');
        return;
    }

    // Liste d'emojis par catégorie
    const emojis = {
        people: ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇'],
        nature: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯'],
        food: ['🍏', '🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🍈'],
        activities: ['⚽', '🏀', '🏈', '⚾', '🎾', '🏐', '🏉', '🎱', '🏓', '🏸'],
        travel: ['🚗', '🚕', '🚙', '🚌', '🚎', '🏎', '🚓', '🚑', '🚒', '🚐'],
        objects: ['⌚', '📱', '💻', '⌨️', '🖥', '🖨', '🖱', '🖲', '💽', '💾'],
        symbols: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔'],
        flags: ['🏁', '🚩', '🎌', '🏴', '🏳️', '🏳️‍🌈', '🏳️‍⚧️', '🏴‍☠️', '🇦🇫', '🇦🇽']
    };

    // Remplir le emoji picker
    function populateEmojis(category = 'people') {
        const emojiContainer = document.getElementById('emoji-container');
        if (!emojiContainer) return;

        emojiContainer.innerHTML = '';
        emojis[category].forEach(emoji => {
            const button = document.createElement('button');
            button.textContent = emoji;
            button.addEventListener('click', () => {
                if (messageInput) {
                    messageInput.value += emoji;
                    messageInput.focus();
                }
            });
            emojiContainer.appendChild(button);
        });
    }

    // Gestion des catégories d'emojis (si elles existent)
    const categoryButtons = document.querySelectorAll('.emoji-categories button');
    if (categoryButtons.length > 0) {
        categoryButtons.forEach(button => {
            button.addEventListener('click', () => {
                const category = button.getAttribute('data-category');
                if (category) {
                    populateEmojis(category);
                }
            });
        });
    }

    // Afficher/masquer le emoji picker
    emojiBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        emojiPicker.style.display = emojiPicker.style.display === 'block' ? 'none' : 'block';
        if (emojiPicker.style.display === 'block') {
            populateEmojis();
        }
    });

    // Fermer le emoji picker quand on clique ailleurs
    document.addEventListener('click', () => {
        emojiPicker.style.display = 'none';
    });

    // Empêcher la fermeture quand on clique dans le picker
    emojiPicker.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    // Gestion du téléchargement de fichiers
    attachBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        openFileUploadModal();
    });

    // Gestion du changement de fichier
    fileInput.addEventListener('change', handleFileUpload);
});

// Fonction pour ouvrir le modal d'upload
function openFileUploadModal() {
    const modal = document.getElementById('file-upload-modal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// Fonction pour gérer le téléchargement de fichiers
function handleFileUpload(event) {
    const files = event.target.files;
    if (!files || files.length === 0) return;

    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        displayFileInChat(file);
        // uploadFile(file); // Décommentez pour activer l'upload
    }
}

// Fonction pour afficher les fichiers dans le chat
function displayFileInChat(file) {
    const messagesContainer = document.getElementById('messages-container');
    if (!messagesContainer) return;

    const messageDiv = document.createElement('div');
    messageDiv.className = 'message sent';

    if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'message-media';
            messageDiv.appendChild(img);

            const infoDiv = document.createElement('div');
            infoDiv.className = 'message-info';
            infoDiv.innerHTML = `
                <span class="message-time">${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                <span class="message-status read"><i class="fas fa-check-double"></i></span>
            `;
            messageDiv.appendChild(infoDiv);

            messagesContainer.appendChild(messageDiv);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        };
        reader.readAsDataURL(file);
    } else if (file.type.startsWith('video/')) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const video = document.createElement('video');
            video.src = e.target.result;
            video.className = 'message-media';
            video.controls = true;
            messageDiv.appendChild(video);

            const infoDiv = document.createElement('div');
            infoDiv.className = 'message-info';
            infoDiv.innerHTML = `
                <span class="message-time">${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                <span class="message-status read"><i class="fas fa-check-double"></i></span>
            `;
            messageDiv.appendChild(infoDiv);

            messagesContainer.appendChild(messageDiv);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        };
        reader.readAsDataURL(file);
    } else {
        // Pour les documents
        const docDiv = document.createElement('div');
        docDiv.className = 'message-document';
        docDiv.innerHTML = `
            <div class="document-icon">
                <i class="fas fa-file-${getFileIcon(file.type)}"></i>
            </div>
            <div class="document-info">
                <div class="document-name">${file.name}</div>
                <div class="document-size">${formatFileSize(file.size)}</div>
            </div>
        `;
        messageDiv.appendChild(docDiv);

        const infoDiv = document.createElement('div');
        infoDiv.className = 'message-info';
        infoDiv.innerHTML = `
            <span class="message-time">${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
            <span class="message-status read"><i class="fas fa-check-double"></i></span>
        `;
        messageDiv.appendChild(infoDiv);

        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

// Fonction utilitaire pour obtenir l'icône du fichier
function getFileIcon(fileType) {
    if (fileType.includes('pdf')) return 'pdf';
    if (fileType.includes('word')) return 'word';
    if (fileType.includes('excel') || fileType.includes('spreadsheet')) return 'excel';
    if (fileType.includes('powerpoint') || fileType.includes('presentation')) return 'powerpoint';
    if (fileType.includes('text')) return 'alt';
    return 'alt';
}

// Fonction utilitaire pour formater la taille du fichier
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Fonction pour envoyer le fichier au serveur
async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/chat/upload-media', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${getAuthToken()}`
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error('Upload failed');
        }

        return await response.json();
    } catch (error) {
        console.error('Upload error:', error);
        throw error;
    }
}

// Fonction utilitaire pour obtenir le token
function getAuthToken() {
    return localStorage.getItem('jwt_token') || '';
}