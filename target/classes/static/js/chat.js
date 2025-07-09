/**
 * 聊天功能相关
 * 包括会话管理、消息发送、流式响应等功能
 */

class ChatManager {
    constructor() {
        this.currentSession = null;
        this.sessions = [];
        this.isStreaming = false;
        this.currentStreamController = null;
        this.messageHistory = [];
        this.init();
    }

    /**
     * 初始化聊天管理器
     */
    init() {
        this.loadSessions();
        this.setupEventListeners();
    }

    /**
     * 设置事件监听器
     */
    setupEventListeners() {
        // 监听认证状态变化
        window.addEventListener('auth:login', () => {
            this.loadSessions();
        });
        
        window.addEventListener('auth:logout', () => {
            this.clearData();
        });
    }

    /**
     * 清除数据
     */
    clearData() {
        this.currentSession = null;
        this.sessions = [];
        this.messageHistory = [];
        this.isStreaming = false;
        this.currentStreamController = null;
    }

    /**
     * 加载会话列表
     */
    async loadSessions() {
        try {
            console.log('开始加载会话列表...');
            const response = await api.getSessionList({
                pageNum: 1,
                pageSize: 100
            });
            
            console.log('API响应:', response);
            
            if (response.code === 200) {
                this.sessions = response.data.records || [];
                console.log('解析的会话列表:', this.sessions);
                this.dispatchEvent('sessionsLoaded', { sessions: this.sessions });
                console.log('已分发sessionsLoaded事件');
            } else {
                console.error('API返回失败:', response.message);
                Utils.showMessage(response.message || '加载会话列表失败', 'error');
            }
        } catch (error) {
            console.error('加载会话列表失败:', error);
            Utils.showMessage('加载会话列表失败', 'error');
        }
    }

    /**
     * 创建新会话
     * @param {string} title - 会话标题
     * @returns {Promise<object>} 创建结果
     */
    async createSession(title = '新对话') {
        try {
            const response = await api.createSession({ title });
            
            if (response.code === 200) {
                // 后端返回的是sessionId，需要构造session对象
                const sessionId = response.data;
                const now = new Date().toISOString();
                const newSession = {
                    id: sessionId,
                    title: title,
                    gmtCreate: now,
                    gmtModified: now,
                    // 保持兼容性
                    createTime: now,
                    updateTime: now
                };
                
                this.sessions.unshift(newSession);
                this.currentSession = newSession;
                this.messageHistory = [];
                
                this.dispatchEvent('sessionCreated', { session: newSession });
                this.dispatchEvent('sessionChanged', { session: newSession });
                
                return { success: true, session: newSession };
            } else {
                throw new Error(response.message || '创建会话失败');
            }
        } catch (error) {
            console.error('创建会话失败:', error);
            Utils.showMessage('创建会话失败', 'error');
            throw error;
        }
    }

    /**
     * 切换会话
     * @param {number} sessionId - 会话ID
     */
    async switchSession(sessionId) {
        try {
            const session = this.sessions.find(s => s.id === sessionId);
            if (!session) {
                throw new Error('会话不存在');
            }
            
            this.currentSession = session;
            await this.loadChatHistory(sessionId);
            
            this.dispatchEvent('sessionChanged', { session });
        } catch (error) {
            console.error('切换会话失败:', error);
            Utils.showMessage('切换会话失败', 'error');
        }
    }

    /**
     * 加载聊天历史
     * @param {number} sessionId - 会话ID
     */
    async loadChatHistory(sessionId) {
        try {
            const response = await api.getChatHistory({
                sessionId,
                pageNum: 1,
                pageSize: 100
            });
            
            if (response.code === 200) {
                // 后端按ID倒序返回，前端需要正序显示（按时间顺序）
                let messages = response.data.records || [];
                // 将消息按ID正序排列，确保消息按时间顺序显示
                messages = messages.reverse();
                
                // 确保消息有正确的role字段
                messages = messages.map(msg => {
                    // 如果没有role字段，根据messageType推断
                    if (!msg.role && msg.messageType) {
                        msg.role = msg.messageType.toLowerCase() === 'user' ? 'user' : 'assistant';
                    }
                    // 如果还是没有role，根据content特征推断
                    if (!msg.role) {
                        msg.role = 'assistant'; // 默认为assistant
                    }
                    return msg;
                });
                
                this.messageHistory = messages;
                this.dispatchEvent('historyLoaded', { 
                    sessionId, 
                    messages: this.messageHistory 
                });
            }
        } catch (error) {
            console.error('加载聊天历史失败:', error);
            Utils.showMessage('加载聊天历史失败', 'error');
        }
    }

    /**
     * 更新会话标题
     * @param {number} sessionId - 会话ID
     * @param {string} title - 新标题
     */
    async updateSessionTitle(sessionId, title) {
        try {
            const response = await api.updateSessionTitle({ sessionId, title });
            
            if (response.code === 200) {
                const session = this.sessions.find(s => s.id === sessionId);
                if (session) {
                    session.title = title;
                    this.dispatchEvent('sessionUpdated', { session });
                }
                
                Utils.showMessage('会话标题更新成功', 'success');
            }
        } catch (error) {
            console.error('更新会话标题失败:', error);
            Utils.showMessage('更新会话标题失败', 'error');
        }
    }

    /**
     * 删除会话
     * @param {number} sessionId - 会话ID
     */
    async deleteSession(sessionId) {
        try {
            const response = await api.deleteSession({ sessionId });
            
            if (response.code === 200) {
                // 从会话列表中移除
                this.sessions = this.sessions.filter(s => s.id !== sessionId);
                
                // 如果删除的是当前会话，清空当前会话
                if (this.currentSession && this.currentSession.id === sessionId) {
                    this.currentSession = null;
                    this.messageHistory = [];
                    this.dispatchEvent('sessionChanged', { session: null });
                }
                
                this.dispatchEvent('sessionDeleted', { sessionId });
                Utils.showMessage('会话删除成功', 'success');
                
                return true;
            } else {
                Utils.showMessage(response.message || '删除会话失败', 'error');
                return false;
            }
        } catch (error) {
            console.error('删除会话失败:', error);
            Utils.showMessage('删除会话失败', 'error');
            return false;
        }
    }

    /**
     * 发送消息
     * @param {string} message - 消息内容
     * @param {boolean} useStream - 是否使用流式响应
     * @returns {Promise<object>} 发送结果
     */
    async sendMessage(message, useStream = true) {
        // 如果没有当前会话，先创建一个新会话
        if (!this.currentSession) {
            await this.createSession();
            if (!this.currentSession) {
                throw new Error('创建会话失败，请重试');
            }
        }
        
        if (this.isStreaming) {
            throw new Error('正在处理中，请稍候');
        }
        
        try {
            // 添加用户消息到历史
            const userMessage = {
                id: Utils.generateId(),
                role: 'user',
                content: message,
                timestamp: new Date().toISOString(),
                sessionId: this.currentSession.id
            };
            
            this.messageHistory.push(userMessage);
            this.dispatchEvent('messageAdded', { message: userMessage });
            
            // 创建助手消息占位符
            const assistantMessage = {
                id: Utils.generateId(),
                role: 'assistant',
                content: '',
                timestamp: new Date().toISOString(),
                sessionId: this.currentSession.id,
                isStreaming: useStream
            };
            
            this.messageHistory.push(assistantMessage);
            this.dispatchEvent('messageAdded', { message: assistantMessage });
            
            if (useStream) {
                return this.sendStreamMessage(message, assistantMessage);
            } else {
                return this.sendNormalMessage(message, assistantMessage);
            }
        } catch (error) {
            console.error('发送消息失败:', error);
            throw error;
        }
    }

    /**
     * 发送普通消息
     * @param {string} message - 消息内容
     * @param {object} assistantMessage - 助手消息对象
     */
    async sendNormalMessage(message, assistantMessage) {
        try {
            const response = await api.askQuestion({
                sessionId: this.currentSession.id,
                question: message
            });
            
            if (response.code === 200) {
                assistantMessage.content = response.data.answer;
                assistantMessage.messageId = response.data.messageId;
                assistantMessage.isStreaming = false;
                
                this.dispatchEvent('messageUpdated', { message: assistantMessage });
                
                return { success: true, message: assistantMessage };
            } else {
                throw new Error(response.message || '发送失败');
            }
        } catch (error) {
            assistantMessage.content = '抱歉，发生了错误，请稍后重试。';
            assistantMessage.isError = true;
            assistantMessage.isStreaming = false;
            
            this.dispatchEvent('messageUpdated', { message: assistantMessage });
            throw error;
        }
    }

    /**
     * 发送流式消息
     * @param {string} message - 消息内容
     * @param {object} assistantMessage - 助手消息对象
     */
    async sendStreamMessage(message, assistantMessage) {
        this.isStreaming = true;
        
        try {
            await api.askQuestionStream(
                {
                    sessionId: this.currentSession.id,
                    question: message
                },
                (chunk) => {
                    // 处理流式数据
                    assistantMessage.content += chunk;
                    this.dispatchEvent('messageUpdated', { message: assistantMessage });
                },
                (error) => {
                    // 处理错误
                    console.error('流式响应错误:', error);
                    assistantMessage.content = '抱歉，发生了错误，请稍后重试。';
                    assistantMessage.isError = true;
                    assistantMessage.isStreaming = false;
                    this.isStreaming = false;
                    
                    this.dispatchEvent('messageUpdated', { message: assistantMessage });
                },
                () => {
                    // 完成回调
                    assistantMessage.isStreaming = false;
                    this.isStreaming = false;
                    
                    this.dispatchEvent('messageUpdated', { message: assistantMessage });
                    this.dispatchEvent('streamComplete', { message: assistantMessage });
                }
            );
            
            return { success: true, message: assistantMessage };
        } catch (error) {
            this.isStreaming = false;
            assistantMessage.content = '抱歉，发生了错误，请稍后重试。';
            assistantMessage.isError = true;
            assistantMessage.isStreaming = false;
            
            this.dispatchEvent('messageUpdated', { message: assistantMessage });
            throw error;
        }
    }

    /**
     * 停止流式响应
     */
    stopStreaming() {
        if (this.currentStreamController) {
            this.currentStreamController.abort();
            this.currentStreamController = null;
        }
        this.isStreaming = false;
    }

    /**
     * 提交反馈
     * @param {string} messageId - 消息ID
     * @param {number} rating - 评分 (1-5)
     * @param {string} feedback - 反馈内容
     */
    async submitFeedback(messageId, rating, feedback = '') {
        try {
            const response = await api.submitFeedback({
                messageId,
                rating,
                feedback
            });
            
            if (response.code === 200) {
                Utils.showMessage('反馈提交成功', 'success');
                
                // 更新消息的反馈状态
                const message = this.messageHistory.find(m => m.messageId === messageId);
                if (message) {
                    message.rating = rating;
                    message.feedback = feedback;
                    this.dispatchEvent('messageUpdated', { message });
                }
                
                return { success: true };
            } else {
                throw new Error(response.message || '反馈提交失败');
            }
        } catch (error) {
            console.error('提交反馈失败:', error);
            Utils.showMessage('反馈提交失败', 'error');
            throw error;
        }
    }

    /**
     * 获取当前会话
     * @returns {object|null} 当前会话
     */
    getCurrentSession() {
        return this.currentSession;
    }

    /**
     * 获取会话列表
     * @returns {Array} 会话列表
     */
    getSessions() {
        return this.sessions;
    }

    /**
     * 获取消息历史
     * @returns {Array} 消息历史
     */
    getMessageHistory() {
        return this.messageHistory;
    }

    /**
     * 检查是否正在流式响应
     * @returns {boolean} 是否正在流式响应
     */
    getIsStreaming() {
        return this.isStreaming;
    }

    /**
     * 触发自定义事件
     * @param {string} eventName - 事件名称
     * @param {object} detail - 事件详情
     */
    dispatchEvent(eventName, detail = {}) {
        const event = new CustomEvent(`chat:${eventName}`, { detail });
        window.dispatchEvent(event);
    }

    /**
     * 监听聊天事件
     * @param {string} eventName - 事件名称
     * @param {Function} callback - 回调函数
     */
    on(eventName, callback) {
        window.addEventListener(`chat:${eventName}`, callback);
    }

    /**
     * 移除聊天事件监听
     * @param {string} eventName - 事件名称
     * @param {Function} callback - 回调函数
     */
    off(eventName, callback) {
        window.removeEventListener(`chat:${eventName}`, callback);
    }
}

/**
 * 消息渲染器
 */
class MessageRenderer {
    /**
     * 渲染消息
     * @param {object} message - 消息对象
     * @returns {HTMLElement} 消息元素
     */
    static renderMessage(message) {
        const messageEl = document.createElement('div');
        messageEl.className = `message ${message.role}`;
        messageEl.dataset.messageId = message.id;
        
        const avatar = document.createElement('div');
        avatar.className = 'message-avatar';
        avatar.innerHTML = message.role === 'user' ? 
            '<i class="fas fa-user"></i>' : 
            '<i class="fas fa-robot"></i>';
        
        const content = document.createElement('div');
        content.className = 'message-content';
        
        const text = document.createElement('div');
        text.className = 'message-text';
        
        if (message.isStreaming) {
            text.innerHTML = message.content + '<span class="typing-cursor">|</span>';
        } else {
            text.innerHTML = this.formatMessageContent(message.content);
        }
        
        content.appendChild(text);
        
        // 为助手消息添加操作按钮
        if (message.role === 'assistant' && !message.isStreaming) {
            const actions = this.createMessageActions(message);
            content.appendChild(actions);
        }
        
        messageEl.appendChild(avatar);
        messageEl.appendChild(content);
        
        return messageEl;
    }

    /**
     * 创建消息操作按钮
     * @param {object} message - 消息对象
     * @returns {HTMLElement} 操作按钮容器
     */
    static createMessageActions(message) {
        const actions = document.createElement('div');
        actions.className = 'message-actions';
        
        // 复制按钮
        const copyBtn = document.createElement('button');
        copyBtn.className = 'action-btn';
        copyBtn.innerHTML = '<i class="fas fa-copy"></i>';
        copyBtn.title = '复制';
        copyBtn.onclick = () => {
            Utils.copyToClipboard(message.content);
            Utils.showMessage('已复制到剪贴板', 'success');
        };
        
        // 评分按钮
        const ratingBtn = document.createElement('button');
        ratingBtn.className = 'action-btn';
        ratingBtn.innerHTML = '<i class="fas fa-thumbs-up"></i>';
        ratingBtn.title = '评分';
        ratingBtn.onclick = () => {
            this.showRatingDialog(message);
        };
        
        actions.appendChild(copyBtn);
        actions.appendChild(ratingBtn);
        
        return actions;
    }

    /**
     * 显示评分对话框
     * @param {object} message - 消息对象
     */
    static showRatingDialog(message) {
        const dialog = document.createElement('div');
        dialog.className = 'rating-dialog';
        dialog.innerHTML = `
            <div class="rating-content">
                <h3>为这个回答评分</h3>
                <div class="rating-stars">
                    ${[1, 2, 3, 4, 5].map(i => 
                        `<span class="star" data-rating="${i}">★</span>`
                    ).join('')}
                </div>
                <textarea placeholder="可选：提供具体反馈" class="feedback-text"></textarea>
                <div class="rating-actions">
                    <button class="btn btn-secondary cancel-btn">取消</button>
                    <button class="btn btn-primary submit-btn">提交</button>
                </div>
            </div>
        `;
        
        document.body.appendChild(dialog);
        
        let selectedRating = 0;
        
        // 星级评分交互
        const stars = dialog.querySelectorAll('.star');
        stars.forEach((star, index) => {
            star.addEventListener('click', () => {
                selectedRating = index + 1;
                stars.forEach((s, i) => {
                    s.classList.toggle('active', i < selectedRating);
                });
            });
        });
        
        // 取消按钮
        dialog.querySelector('.cancel-btn').onclick = () => {
            document.body.removeChild(dialog);
        };
        
        // 提交按钮
        dialog.querySelector('.submit-btn').onclick = async () => {
            if (selectedRating === 0) {
                Utils.showMessage('请选择评分', 'warning');
                return;
            }
            
            const feedback = dialog.querySelector('.feedback-text').value;
            
            try {
                await window.chatManager.submitFeedback(
                    message.messageId, 
                    selectedRating, 
                    feedback
                );
                document.body.removeChild(dialog);
            } catch (error) {
                Utils.showMessage('提交失败，请重试', 'error');
            }
        };
    }

    /**
     * 格式化消息内容
     * @param {string} content - 原始内容
     * @returns {string} 格式化后的内容
     */
    static formatMessageContent(content) {
        // 转义HTML
        content = Utils.escapeHtml(content);
        
        // 处理换行
        content = content.replace(/\n/g, '<br>');
        
        // 处理代码块
        content = content.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
        
        // 处理行内代码
        content = content.replace(/`([^`]+)`/g, '<code>$1</code>');
        
        // 处理链接
        content = content.replace(
            /(https?:\/\/[^\s]+)/g, 
            '<a href="$1" target="_blank">$1</a>'
        );
        
        return content;
    }
}

// 创建全局聊天管理器实例
const chatManager = new ChatManager();

// 导出到全局
window.chatManager = chatManager;
window.ChatManager = ChatManager;
window.MessageRenderer = MessageRenderer;