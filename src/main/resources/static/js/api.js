/**
 * API调用封装
 * 提供统一的HTTP请求接口和错误处理
 */

class ApiClient {
    constructor() {
        this.baseURL = CONFIG.API_BASE_URL;
        this.defaultHeaders = {
            'Content-Type': 'application/json'
        };
    }

    /**
     * 获取认证头
     * @returns {object} 认证头信息
     */
    getAuthHeaders() {
        const headers = {};
        
        // 添加认证token
        const token = Storage.get(CONFIG.TOKEN_KEY);
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        // 添加用户ID到请求头
        const user = Storage.get(CONFIG.USER_KEY);
        if (user && user.userId) {
            headers['X-User-Id'] = user.userId;
        }
        
        return headers;
    }

    /**
     * 发送HTTP请求
     * @param {string} url - 请求URL
     * @param {object} options - 请求选项
     * @returns {Promise} 请求结果
     */
    async request(url, options = {}) {
        const config = {
            method: 'GET',
            headers: {
                ...this.defaultHeaders,
                ...this.getAuthHeaders(),
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(`${this.baseURL}${url}`, config);
            
            // 处理认证失败
            if (response.status === 401) {
                Storage.remove(CONFIG.TOKEN_KEY);
                Storage.remove(CONFIG.USER_KEY);
                window.location.href = '/login.html';
                throw new Error('认证失败，请重新登录');
            }

            // 处理其他HTTP错误
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            // 处理不同的响应类型
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else if (contentType && contentType.includes('text/')) {
                return await response.text();
            } else {
                return response;
            }
        } catch (error) {
            console.error('API请求失败:', error);
            throw error;
        }
    }

    /**
     * GET请求
     * @param {string} url - 请求URL
     * @param {object} params - 查询参数
     * @returns {Promise} 请求结果
     */
    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        return this.request(fullUrl);
    }

    /**
     * POST请求
     * @param {string} url - 请求URL
     * @param {object} data - 请求数据
     * @returns {Promise} 请求结果
     */
    async post(url, data = {}) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    /**
     * PUT请求
     * @param {string} url - 请求URL
     * @param {object} data - 请求数据
     * @returns {Promise} 请求结果
     */
    async put(url, data = {}) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    /**
     * DELETE请求
     * @param {string} url - 请求URL
     * @returns {Promise} 请求结果
     */
    async delete(url) {
        return this.request(url, {
            method: 'DELETE'
        });
    }

    /**
     * 上传文件
     * @param {string} url - 上传URL
     * @param {FormData} formData - 表单数据
     * @param {Function} onProgress - 进度回调
     * @returns {Promise} 上传结果
     */
    async upload(url, formData, onProgress = null) {
        console.log('🚀 ApiClient.upload 开始执行');
        console.log('📍 上传URL:', url);
        console.log('📦 FormData内容:', formData);
        console.log('🔗 完整请求URL:', `${this.baseURL}${url}`);
        
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            
            // 设置进度监听
            if (onProgress) {
                xhr.upload.addEventListener('progress', (e) => {
                    if (e.lengthComputable) {
                        const percentComplete = (e.loaded / e.total) * 100;
                        console.log(`📊 上传进度: ${percentComplete.toFixed(2)}%`);
                        onProgress(percentComplete);
                    }
                });
            }

            // 设置完成监听
            xhr.addEventListener('load', () => {
                console.log('✅ 上传请求完成，状态码:', xhr.status);
                console.log('📄 响应内容:', xhr.responseText);
                
                if (xhr.status >= 200 && xhr.status < 300) {
                    try {
                        const response = JSON.parse(xhr.responseText);
                        console.log('✅ 上传成功，解析后的响应:', response);
                        resolve(response);
                    } catch (e) {
                        console.log('⚠️ JSON解析失败，返回原始文本:', xhr.responseText);
                        resolve(xhr.responseText);
                    }
                } else {
                    console.error('❌ 上传失败，状态码:', xhr.status, '状态文本:', xhr.statusText);
                    reject(new Error(`上传失败: ${xhr.status} ${xhr.statusText}`));
                }
            });

            // 设置错误监听
            xhr.addEventListener('error', () => {
                console.error('❌ 上传网络错误');
                reject(new Error('上传失败: 网络错误'));
            });

            // 打开请求（必须在设置请求头之前调用）
            console.log('🚀 打开POST请求到:', `${this.baseURL}${url}`);
            xhr.open('POST', `${this.baseURL}${url}`);
            
            // 设置请求头
            const authHeaders = this.getAuthHeaders();
            console.log('🔐 认证头信息:', authHeaders);
            Object.keys(authHeaders).forEach(key => {
                xhr.setRequestHeader(key, authHeaders[key]);
            });

            // 发送请求
            console.log('📤 发送FormData');
            xhr.send(formData);
        });
    }

    /**
     * 流式请求
     * @param {string} url - 请求URL
     * @param {object} data - 请求数据
     * @param {Function} onMessage - 消息回调
     * @param {Function} onError - 错误回调
     * @param {Function} onComplete - 完成回调
     */
    async stream(url, data, onMessage, onError, onComplete) {
        try {
            const response = await fetch(`${this.baseURL}${url}`, {
                method: 'POST',
                headers: {
                    ...this.defaultHeaders,
                    ...this.getAuthHeaders()
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            while (true) {
                const { done, value } = await reader.read();
                
                if (done) {
                    onComplete && onComplete();
                    break;
                }

                const chunk = decoder.decode(value, { stream: true });
                onMessage && onMessage(chunk);
            }
        } catch (error) {
            console.error('流式请求失败:', error);
            onError && onError(error);
        }
    }
}

/**
 * API接口定义
 */
class API {
    constructor() {
        this.client = new ApiClient();
    }

    // ========== 用户相关接口 ==========
    
    /**
     * 用户注册
     * @param {object} userData - 用户数据
     * @returns {Promise} 注册结果
     */
    async register(userData) {
        console.log('API.register 被调用，参数:', userData);
        console.log('发送 POST 请求到 /user/register');
        try {
            const result = await this.client.post('/user/register', userData);
            console.log('HTTP 请求成功，返回结果:', result);
            return result;
        } catch (error) {
            console.error('HTTP 请求失败:', error);
            throw error;
        }
    }

    /**
     * 用户登录
     * @param {object} credentials - 登录凭据
     * @returns {Promise} 登录结果
     */
    async login(credentials) {
        return this.client.post('/user/login', credentials);
    }

    /**
     * 修改密码
     * @param {object} passwordData - 密码数据
     * @returns {Promise} 修改结果
     */
    async changePassword(passwordData) {
        return this.client.post('/user/change-password', passwordData);
    }

    /**
     * 获取用户信息
     * @returns {Promise} 用户信息
     */
    async getUserInfo() {
        return this.client.get('/user/info');
    }

    /**
     * 检查用户名是否可用
     * @param {string} username - 用户名
     * @returns {Promise} 检查结果
     */
    async checkUsername(username) {
        return this.client.get('/user/check-username', { username });
    }

    // ========== 会话相关接口 ==========

    /**
     * 创建会话
     * @param {object} sessionData - 会话数据
     * @returns {Promise} 创建结果
     */
    async createSession(sessionData) {
        return this.client.post('/chat/sessions/create', sessionData);
    }

    /**
     * 获取会话列表
     * @param {object} queryParams - 查询参数
     * @returns {Promise} 会话列表
     */
    async getSessionList(queryParams) {
        return this.client.post('/chat/sessions/list', queryParams);
    }

    /**
     * 获取会话详情
     * @param {number} sessionId - 会话ID
     * @returns {Promise} 会话详情
     */
    async getSessionDetail(sessionId) {
        return this.client.get(`/chat/sessions/detail/${sessionId}`);
    }

    /**
     * 更新会话标题
     * @param {object} updateData - 更新数据
     * @returns {Promise} 更新结果
     */
    async updateSessionTitle(updateData) {
        return this.client.post('/chat/sessions/update-title', updateData);
    }

    /**
     * 删除会话
     * @param {object} deleteData - 删除数据
     * @returns {Promise} 删除结果
     */
    async deleteSession(deleteData) {
        return this.client.post('/chat/sessions/delete', deleteData);
    }

    // ========== 聊天相关接口 ==========

    /**
     * 智能问答
     * @param {object} questionData - 问题数据
     * @returns {Promise} 回答结果
     */
    async askQuestion(questionData) {
        return this.client.post('/chat/ask', questionData);
    }

    /**
     * 流式智能问答
     * @param {object} questionData - 问题数据
     * @param {Function} onMessage - 消息回调
     * @param {Function} onError - 错误回调
     * @param {Function} onComplete - 完成回调
     */
    async askQuestionStream(questionData, onMessage, onError, onComplete) {
        return this.client.stream('/chat/stream', questionData, onMessage, onError, onComplete);
    }

    /**
     * 获取聊天历史
     * @param {object} queryParams - 查询参数
     * @returns {Promise} 聊天历史
     */
    async getChatHistory(queryParams) {
        return this.client.post('/chat/getChatHistory', queryParams);
    }

    /**
     * 提交反馈
     * @param {object} feedbackData - 反馈数据
     * @returns {Promise} 提交结果
     */
    async submitFeedback(feedbackData) {
        return this.client.post('/chat/feedback', feedbackData);
    }

    /**
     * 生成会话标题
     * @param {number} sessionId - 会话ID
     * @returns {Promise} 生成的标题
     */
    async generateSessionTitle(sessionId) {
        return this.client.post(`/chat/generateTitle/${sessionId}`);
    }

    // ========== 知识库相关接口 ==========

    /**
     * 上传文件到知识库
     * @param {File} file - 文件对象
     * @param {Function} onProgress - 进度回调
     * @returns {Promise} 上传结果
     */
    async uploadFile(file, onProgress) {
        console.log('🎯 API.uploadFile 被调用');
        console.log('📁 文件信息:', {
            name: file.name,
            size: file.size,
            type: file.type,
            lastModified: file.lastModified
        });
        
        const formData = new FormData();
        formData.append('file', file);
        
        console.log('📦 FormData已创建，文件已添加');
        console.log('🔄 调用 client.upload 方法');
        
        try {
            const result = await this.client.upload('/knowledge-base/upload-file', formData, onProgress);
            console.log('✅ uploadFile 成功完成，结果:', result);
            return result;
        } catch (error) {
            console.error('❌ uploadFile 失败:', error);
            throw error;
        }
    }

    /**
     * 插入文本内容到知识库
     * @param {string} content - 文本内容
     * @returns {Promise} 插入结果
     */
    async insertText(content) {
        const formData = new FormData();
        formData.append('content', content);
        return this.client.request('/knowledge-base/insert-text', {
            method: 'POST',
            headers: this.client.getAuthHeaders(),
            body: formData
        });
    }

    /**
     * 搜索知识库
     * @param {string} query - 搜索查询
     * @param {number} topK - 返回结果数量
     * @returns {Promise} 搜索结果
     */
    async searchKnowledge(query, topK = 5) {
        return this.client.get('/knowledge-base/search', { query, topK });
    }
}

// 创建全局API实例
const api = new API();

// 导出到全局
window.api = api;
window.ApiClient = ApiClient;
window.API = API;