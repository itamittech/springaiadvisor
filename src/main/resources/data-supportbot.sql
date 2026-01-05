-- =====================================================
-- Support Bot Sample Data
-- Tutorial Part 2: Customer Support Bot with Knowledge Base
-- =====================================================

-- Sample Customers
INSERT INTO customers (name, email, plan, company_name, created_at) VALUES
('John Smith', 'john@acme.com', 'PREMIUM', 'Acme Corp', CURRENT_TIMESTAMP),
('Sarah Johnson', 'sarah@techstart.io', 'ENTERPRISE', 'TechStart', CURRENT_TIMESTAMP),
('Mike Brown', 'mike.brown@gmail.com', 'FREE', NULL, CURRENT_TIMESTAMP),
('Emily Davis', 'emily@designco.com', 'PREMIUM', 'Design Co', CURRENT_TIMESTAMP),
('Alex Wilson', 'alex@startup.io', 'FREE', 'Startup Inc', CURRENT_TIMESTAMP);

-- Sample Knowledge Base Articles
INSERT INTO articles (title, content, category, tags, is_published) VALUES
('Getting Started with TaskFlow', 
'Welcome to TaskFlow! This guide helps you get started.

Creating Your First Project:
1. Click New Project in the top navigation
2. Enter project name and description
3. Select a template or start from scratch
4. Invite team members by email

Key Features:
- Task Boards: Kanban-style boards
- Timeline View: Gantt charts for planning
- Team Chat: Built-in messaging
- File Storage: Upload and share documents', 
'getting-started', 'onboarding,basics', TRUE),

('How to Reset Your Password', 
'If you forgot your password:

Reset via Email:
1. Go to app.taskflow.com
2. Click Forgot Password
3. Enter your email address
4. Check inbox for reset link
5. Create a new password

Password Requirements:
- Minimum 8 characters
- At least one uppercase letter
- At least one number
- At least one special character

Contact support@taskflow.com if issues persist.', 
'account', 'password,login,security', TRUE),

('Billing and Pricing FAQ', 
'Pricing Plans:
- FREE: Up to 3 projects, 5 team members
- PREMIUM ($15/user/month): Unlimited projects, advanced features
- ENTERPRISE ($30/user/month): SSO, priority support

Cancellation:
- Cancel anytime from Settings > Billing
- Access continues until end of billing period
- Data retained for 30 days

Refund Policy:
- Full refund within 14 days of purchase
- No refunds for partial months', 
'billing', 'pricing,payment,refund,cancel', TRUE),

('Troubleshooting: App Not Loading', 
'Quick Fixes:
1. Clear Browser Cache (Ctrl+Shift+Delete)
2. Try Incognito Mode
3. Check Internet Connection
4. Disable Browser Extensions

Supported Browsers:
- Chrome 90+ (recommended)
- Firefox 88+
- Safari 14+
- Edge 90+

Mobile App Issues:
- Ensure latest app version
- Force-close and reopen
- Reinstall if needed

Contact support@taskflow.com for help.', 
'troubleshooting', 'loading,error,browser', TRUE);

-- Sample Tickets
INSERT INTO tickets (customer_id, subject, description, status, priority, category, created_at) VALUES
(1, 'Cannot access premium features', 'Upgraded yesterday but features not showing.', 'OPEN', 'HIGH', 'billing', CURRENT_TIMESTAMP),
(3, 'Export question', 'How do I export to CSV?', 'IN_PROGRESS', 'MEDIUM', 'general', CURRENT_TIMESTAMP),
(2, 'SSO not working', 'Getting 403 error with SAML SSO.', 'OPEN', 'CRITICAL', 'technical', CURRENT_TIMESTAMP);
