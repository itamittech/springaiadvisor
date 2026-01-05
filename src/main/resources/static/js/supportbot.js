/**
 * TaskFlow Support Bot - Frontend JavaScript
 */

// State
let messageCount = 0;
let currentCustomerId = null;
let sessionId = generateSessionId();

// DOM Elements
const chatMessages = document.getElementById('chatMessages');
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');
const typingIndicator = document.getElementById('typingIndicator');
const customerSelect = document.getElementById('customerSelect');
const customerInfo = document.getElementById('customerInfo');
const ticketsList = document.getElementById('ticketsList');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    autoResizeTextarea();
});

function setupEventListeners() {
    // Send message on Enter (Shift+Enter for new line)
    messageInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Auto-resize textarea
    messageInput.addEventListener('input', autoResizeTextarea);

    // Customer selection
    customerSelect.addEventListener('change', handleCustomerChange);
}

function autoResizeTextarea() {
    messageInput.style.height = 'auto';
    messageInput.style.height = Math.min(messageInput.scrollHeight, 120) + 'px';
}

// Generate unique session ID
function generateSessionId() {
    return 'session-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
}

// Handle customer selection change
async function handleCustomerChange() {
    const customerId = customerSelect.value;
    currentCustomerId = customerId || null;

    if (customerId) {
        try {
            const response = await fetch(`/support/customers/${customerId}`);
            const customer = await response.json();

            document.getElementById('customerEmail').textContent = customer.email;
            document.getElementById('customerPlan').textContent = customer.plan;
            document.getElementById('customerCompany').textContent = customer.companyName || 'N/A';

            customerInfo.classList.remove('hidden');

            // Load tickets for this customer
            loadTickets();

            // Add system message
            addMessage('bot', `👋 Hello, ${customer.name}! I can see you're on the **${customer.plan}** plan. How can I help you today?`);
        } catch (error) {
            console.error('Failed to load customer:', error);
        }
    } else {
        customerInfo.classList.add('hidden');
        ticketsList.innerHTML = `
            <div class="empty-state">
                <p>No tickets yet.</p>
                <p class="subtext">Select a customer to view their tickets.</p>
            </div>
        `;
    }
}

// Load tickets for current customer
async function loadTickets() {
    if (!currentCustomerId) return;

    try {
        const response = await fetch(`/support/tickets/customer/${currentCustomerId}`);
        const tickets = await response.json();

        if (tickets.length === 0) {
            ticketsList.innerHTML = `
                <div class="empty-state">
                    <p>No tickets yet.</p>
                    <p class="subtext">If you need to escalate, just ask to "speak to a human".</p>
                </div>
            `;
            return;
        }

        ticketsList.innerHTML = tickets.map(ticket => `
            <div class="ticket-card">
                <div class="ticket-header">
                    <span class="ticket-id">#${ticket.id}</span>
                    <span class="ticket-status ${ticket.status.toLowerCase()}">${ticket.status}</span>
                </div>
                <div class="ticket-subject">${escapeHtml(ticket.subject)}</div>
                <div class="ticket-priority ${ticket.priority.toLowerCase()}">
                    Priority: ${ticket.priority}
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Failed to load tickets:', error);
    }
}

// Send message (Streaming)
async function sendMessage() {
    const message = messageInput.value.trim();
    if (!message) return;

    // Add user message
    addMessage('user', message);
    messageInput.value = '';
    autoResizeTextarea();

    // Show typing indicator
    showTyping(true);
    sendBtn.disabled = true;

    // Prepare Bot Message Container
    const botMessageDiv = createMessageDiv('bot');
    const contentDiv = botMessageDiv.querySelector('.message-content');
    contentDiv.innerHTML = ''; // Start empty
    chatMessages.appendChild(botMessageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    let fullText = '';

    try {
        // Construct SSE URL
        const params = new URLSearchParams({
            message: message,
            customerId: currentCustomerId || '',
            sessionId: sessionId || ''
        });

        const eventSource = new EventSource(`/support/stream?${params.toString()}`);

        eventSource.onmessage = (event) => {
            // Hide typing indicator on first token
            showTyping(false);

            const token = event.data;
            // Handle newlines in SSE (often sent as literal \n or separate events)
            // Spring AI sends raw tokens.

            fullText += token.replace(/\\n/g, '\n'); // Simple unescape if needed
            contentDiv.innerHTML = marked.parse(fullText);

            // Scroll to bottom
            chatMessages.scrollTop = chatMessages.scrollHeight;
        };

        eventSource.onerror = (error) => {
            // Stream ended or error
            eventSource.close();
            sendBtn.disabled = false;
            showTyping(false);

            // Refresh tickets just in case (optional, we lose the 'ticketCreated' flag from JSON response)
            // We could blindly refresh or poll.
            loadTickets();
        };

    } catch (error) {
        console.error('Failed to start stream:', error);
        showTyping(false);
        sendBtn.disabled = false;
        contentDiv.innerHTML = '❌ Error starting stream.';
    }
}

function createMessageDiv(type) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}-message`;
    const avatar = type === 'bot' ? '🤖' : '👤';
    messageDiv.innerHTML = `
        <div class="message-avatar">${avatar}</div>
        <div class="message-content"><span class="cursor"></span></div>
    `;
    return messageDiv;
}

// Quick question buttons
function quickQuestion(question) {
    messageInput.value = question;
    sendMessage();
}

// Add message to chat
function addMessage(type, content) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}-message`;

    const avatar = type === 'bot' ? '🤖' : '👤';

    // Parse markdown for bot messages
    const htmlContent = type === 'bot' ? marked.parse(content) : escapeHtml(content);

    messageDiv.innerHTML = `
        <div class="message-avatar">${avatar}</div>
        <div class="message-content">${htmlContent}</div>
    `;

    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    messageCount++;
    document.getElementById('messageCount').textContent = messageCount;
}

// Show/hide typing indicator
function showTyping(show) {
    typingIndicator.classList.toggle('hidden', !show);
    if (show) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}

// Update sentiment display
function updateSentiment(sentiment) {
    const emojiMap = {
        'POSITIVE': '😊',
        'NEUTRAL': '😐',
        'FRUSTRATED': '😤',
        'ANGRY': '😠'
    };
    document.getElementById('currentSentiment').textContent = emojiMap[sentiment] || '😐';
}

// Clear chat
function clearChat() {
    // Keep only the welcome message
    const welcomeMessage = chatMessages.querySelector('.message');
    chatMessages.innerHTML = '';
    if (welcomeMessage) {
        chatMessages.appendChild(welcomeMessage);
    }

    messageCount = 1;
    document.getElementById('messageCount').textContent = messageCount;
    sessionId = generateSessionId();
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
