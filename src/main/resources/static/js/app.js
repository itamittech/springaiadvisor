document.addEventListener('DOMContentLoaded', () => {
    // State
    const state = {
        activeTab: 'memory', // memory, user, persistent, window
        history: {
            memory: [],
            user: [],
            persistent: [],
            window: []
        }
    };

    // DOM Elements
    const chatContainer = document.getElementById('chat-container');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const userIdInput = document.getElementById('userid-input');
    const userIdContainer = document.getElementById('userid-container');
    const tabLinks = document.querySelectorAll('.nav-link');

    // Tab Switching
    tabLinks.forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            
            // UI Updates
            tabLinks.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            
            // Logic Updates
            state.activeTab = tab.dataset.tab;
            
            // User ID Visibility
            if (state.activeTab === 'user') {
                userIdContainer.style.display = 'flex';
            } else {
                userIdContainer.style.display = 'none';
            }

            // Restore History
            renderHistory();
        });
    });

    // Sending Messages
    async function sendMessage() {
        const text = messageInput.value.trim();
        if (!text) return;

        // Add User Message
        addMessage(text, 'user');
        messageInput.value = '';

        // Show Typing Indicator
        const loadingId = addLoading();

        try {
            const response = await fetchResponse(text);
            removeLoading(loadingId);
            addMessage(response, 'bot');
        } catch (error) {
            removeLoading(loadingId);
            addMessage('Error: ' + error.message, 'bot');
        }
    }

    // API Call
    async function fetchResponse(message) {
        let url = '';
        const headers = {};

        switch (state.activeTab) {
            case 'memory':
                url = `/advisor/chat/memory?message=${encodeURIComponent(message)}`;
                break;
            case 'user':
                const userId = userIdInput.value.trim() || 'Guest';
                url = `/advisor/chat/user?message=${encodeURIComponent(message)}`;
                headers['userId'] = userId;
                break;
            case 'persistent':
                url = `/advisor/chat/persistent?message=${encodeURIComponent(message)}`;
                break;
            case 'window':
                url = `/advisor/chat/window?message=${encodeURIComponent(message)}`;
                break;
        }

        const res = await fetch(url, { headers });
        if (!res.ok) throw new Error('Network response was not ok');
        return await res.text();
    }

    // UI Helpers
    function addMessage(text, type) {
        const msg = { text, type, timestamp: new Date() };
        state.history[state.activeTab].push(msg);
        renderMessage(msg);
    }

    function renderMessage(msg) {
        const div = document.createElement('div');
        div.className = `message ${msg.type}`;
        div.textContent = msg.text;
        chatContainer.appendChild(div);
        scrollToBottom();
    }

    function renderHistory() {
        chatContainer.innerHTML = '';
        state.history[state.activeTab].forEach(renderMessage);
        scrollToBottom();
    }

    function addLoading() {
        const id = 'loading-' + Date.now();
        const div = document.createElement('div');
        div.id = id;
        div.className = 'message bot typing-indicator';
        div.innerHTML = '<span></span><span></span><span></span>';
        chatContainer.appendChild(div);
        scrollToBottom();
        return id;
    }

    function removeLoading(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom() {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    // Event Listeners
    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
});
