document.addEventListener('DOMContentLoaded', () => {
    // State
    const state = {
        activeTab: 'memory', // memory, user, persistent, window
        history: {
            memory: [],
            user: [],
            persistent: [],
            window: [],
            custom: [],
            rag: [],
            safety: [],
            logging: [],
            'custom-feature': []
        }
    };

    // Tutorial Content
    const tutorialContent = {
        'memory': '<strong>In-Memory (The Elephant)</strong>: This bot remembers the conversation context within the application memory. It resets when the application restarts.',
        'user': '<strong>Multi-User Sandbox</strong>: Provide a User ID to maintain separate conversation histories for different users.',
        'persistent': '<strong>Persistent Memory</strong>: Conversation history is saved to a database (H2). It persists even after application restarts.',
        'window': '<strong>Sliding Window</strong>: Optimizes memory usage by keeping only the last N messages (e.g., last 3 exchanges). Useful for reducing token costs.',
        'rag': '<strong>RAG (Retrieval Augmented Generation)</strong>: Ask questions about Mars! The bot uses a "Mars Colonization Guide" document to answer specialized questions.',
        'safety': '<strong>Content Safety</strong>: This bot is guarded. Try using sensitive words (e.g., "violence") to see the safety filter in action.',
        'logging': '<strong>Custom Logging</strong>: All requests are logged with token usage/performance data to the server console using a custom Call Advisor.',
        'custom-feature': '<strong>Advisor Chain Demo</strong><br><ul><li><strong>PromptEnhancer</strong>: Automatically adds \"format as bullet points\" to your prompt (via Spring AOP).</li><li><strong>SafeGuard</strong>: Blocks sensitive words like \"violence\".</li><li><strong>RAG</strong>: Answers from Mars Guide.</li><li><strong>Persistence</strong>: Remembers your chat history.</li></ul>'
    };

    // DOM Elements
    const chatContainer = document.getElementById('chat-container');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const userIdInput = document.getElementById('userid-input');
    const userIdContainer = document.getElementById('userid-container');
    const tutorialInfo = document.getElementById('tutorial-info');
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

            // Tutorial Info Update
            const content = tutorialContent[state.activeTab];
            if (content && tutorialInfo) {
                tutorialInfo.innerHTML = content;
                tutorialInfo.style.display = 'block';
            } else if (tutorialInfo) {
                tutorialInfo.style.display = 'none';
            }

            // Restore History
            renderHistory();
        });
    });

    // Initialize Tutorial for Default Tab (Memory)
    if (tutorialInfo && tutorialContent['memory']) {
        tutorialInfo.innerHTML = tutorialContent['memory'];
        tutorialInfo.style.display = 'block';
    }

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
            case 'rag':
                url = `/advisor/chat/rag?message=${encodeURIComponent(message)}`;
                break;
            case 'safety':
                url = `/advisor/chat/safety?message=${encodeURIComponent(message)}`;
                break;
            case 'logging':
                url = `/advisor/chat/logging?message=${encodeURIComponent(message)}`;
                break;
            case 'custom-feature':
                url = `/advisor/chat/custom-feature?message=${encodeURIComponent(message)}`;
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

        if (msg.type === 'bot') {
            // Convert markdown-style formatting to HTML
            let html = msg.text
                // Escape HTML first
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                // Convert markdown headers
                .replace(/^### (.+)$/gm, '<strong>$1</strong>')
                .replace(/^## (.+)$/gm, '<strong style="font-size:1.1em">$1</strong>')
                // Convert markdown bold
                .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
                // Convert markdown bullet points (- or *)
                .replace(/^[\-\*] (.+)$/gm, '<li>$1</li>')
                // Wrap consecutive <li> in <ul>
                .replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
                // Convert numbered lists
                .replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
                // Convert newlines to <br> (except in lists)
                .replace(/\n(?!<)/g, '<br>');

            div.innerHTML = html;
        } else {
            div.textContent = msg.text;
        }

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
