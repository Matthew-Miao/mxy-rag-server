/**
 * 用户认证相关功能
 * 包括登录、注册、密码修改等功能
 */

class AuthManager {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    /**
     * 初始化认证管理器
     */
    init() {
        this.loadUserFromStorage();
    }

    /**
     * 从本地存储加载用户信息
     */
    loadUserFromStorage() {
        const userData = Storage.get(CONFIG.USER_KEY);
        if (userData) {
            this.currentUser = userData;
        }
    }







    /**
     * 用户登录
     * @param {string} username - 用户名
     * @param {string} password - 密码
     * @param {boolean} rememberMe - 是否记住我
     * @returns {Promise<object>} 登录结果
     */
    async login(username, password, rememberMe = false) {
        try {
            const response = await api.login({ username, password });
            
            if (response.code === 200 && response.data) {
                
                // 检查是否有 token，如果没有则生成一个临时 token 或使用用户ID
                const token = response.data.token || `temp_token_${response.data.userId}_${Date.now()}`;
                
                // 创建完整的用户数据对象
                const userData = {
                    ...response.data,
                    token: token
                };
                
                // 保存令牌和用户信息
                const expiry = rememberMe ? 7 * 24 * 60 * 60 * 1000 : CONFIG.SESSION_TIMEOUT;
                Storage.set(CONFIG.TOKEN_KEY, token, expiry);
                Storage.set(CONFIG.USER_KEY, userData, expiry);
                
                this.currentUser = userData;
                
                // 触发登录成功事件
                this.dispatchEvent('login', { user: userData });
                
                return { success: true, user: userData };
            } else {
                throw new Error(response.message || '登录失败');
            }
        } catch (error) {
            console.error('登录失败:', error);
            throw error;
        }
    }

    /**
     * 用户注册
     * @param {object} userData - 用户数据
     * @returns {Promise<object>} 注册结果
     */
    /**
     * 用户注册
     * @param {object} userData - 用户数据
     * @returns {Promise<object>} 注册结果
     */
    async register(userData) {
        try {
            const response = await api.register(userData);
            
            if (response.code === 200) {
                // 注册成功后自动登录
                if (response.data && response.data.token) {
                    Storage.set(CONFIG.TOKEN_KEY, response.data.token, CONFIG.SESSION_TIMEOUT);
                    Storage.set(CONFIG.USER_KEY, response.data, CONFIG.SESSION_TIMEOUT);
                    this.currentUser = response.data;
                    this.dispatchEvent('login', { user: response.data });
                }
                
                return { success: true, user: response.data };
            } else {
                throw new Error(response.message || '注册失败');
            }
        } catch (error) {
            console.error('注册失败:', error);
            throw error;
        }
    }

    /**
     * 用户登出
     */
    logout() {
        // 清除本地存储
        Storage.remove(CONFIG.TOKEN_KEY);
        Storage.remove(CONFIG.USER_KEY);
        
        this.currentUser = null;
        
        // 触发登出事件
        this.dispatchEvent('logout');
        
        // 只有在非登录页面才重定向到登录页面
        if (window.location.pathname !== '/login.html') {
            window.location.href = '/login.html';
        }
    }

    /**
     * 修改密码
     * @param {string} oldPassword - 旧密码
     * @param {string} newPassword - 新密码
     * @returns {Promise<object>} 修改结果
     */
    async changePassword(oldPassword, newPassword) {
        try {
            const response = await api.changePassword({
                oldPassword,
                newPassword
            });
            
            if (response.code === 200) {
                Utils.showMessage('密码修改成功', 'success');
                return { success: true };
            } else {
                throw new Error(response.message || '密码修改失败');
            }
        } catch (error) {
            console.error('密码修改失败:', error);
            throw error;
        }
    }

    /**
     * 检查用户名是否可用
     * @param {string} username - 用户名
     * @returns {Promise<boolean>} 是否可用
     */
    /**
     * 检查用户名是否可用
     * @param {string} username - 用户名
     * @returns {boolean} true表示可用，false表示已存在
     */
    async checkUsername(username) {
        try {
            const response = await api.checkUsername(username);
            return response.data; // 返回true表示可用，false表示已存在
        } catch (error) {
            console.error('检查用户名失败:', error);
            return false;
        }
    }

    /**
     * 获取当前用户信息
     * @returns {object|null} 用户信息
     */
    getCurrentUser() {
        return this.currentUser;
    }

    /**
     * 检查用户是否已登录
     * @returns {boolean} 是否已登录
     */
    isLoggedIn() {
        const token = Storage.get(CONFIG.TOKEN_KEY);
        return !!(token && this.currentUser);
    }

    /**
     * 检查用户权限
     * @param {string} permission - 权限名称
     * @returns {boolean} 是否有权限
     */
    hasPermission(permission) {
        if (!this.currentUser) return false;
        
        const userPermissions = this.currentUser.permissions || [];
        return userPermissions.includes(permission) || userPermissions.includes('admin');
    }

    /**
     * 触发自定义事件
     * @param {string} eventName - 事件名称
     * @param {object} detail - 事件详情
     */
    dispatchEvent(eventName, detail = {}) {
        const event = new CustomEvent(`auth:${eventName}`, { detail });
        window.dispatchEvent(event);
    }

    /**
     * 监听认证事件
     * @param {string} eventName - 事件名称
     * @param {Function} callback - 回调函数
     */
    on(eventName, callback) {
        window.addEventListener(`auth:${eventName}`, callback);
    }

    /**
     * 移除认证事件监听
     * @param {string} eventName - 事件名称
     * @param {Function} callback - 回调函数
     */
    off(eventName, callback) {
        window.removeEventListener(`auth:${eventName}`, callback);
    }
}

/**
 * 页面访问控制
 */
class PageGuard {
    /**
     * 检查页面访问权限
     * @param {string} requiredAuth - 是否需要认证
     * @param {string} requiredPermission - 需要的权限
     */
    static checkAccess(requiredAuth = true, requiredPermission = null) {
        const auth = window.authManager;
        
        if (requiredAuth && !auth.isLoggedIn()) {
            // 需要认证但未登录，重定向到登录页
            window.location.href = '/login.html';
            return false;
        }
        
        if (requiredPermission && !auth.hasPermission(requiredPermission)) {
            // 需要特定权限但用户没有
            Utils.showMessage('您没有访问此页面的权限', 'error');
            window.history.back();
            return false;
        }
        
        return true;
    }

    /**
     * 设置页面守卫
     * @param {object} options - 守卫选项
     */
    static setup(options = {}) {
        const {
            requireAuth = true,
            requirePermission = null,
            redirectTo = '/login.html'
        } = options;
        
        // 页面加载时检查权限
        document.addEventListener('DOMContentLoaded', () => {
            if (!PageGuard.checkAccess(requireAuth, requirePermission)) {
                window.location.href = redirectTo;
            }
        });
        
        // 监听认证状态变化
        window.addEventListener('auth:logout', () => {
            if (requireAuth) {
                window.location.href = redirectTo;
            }
        });
    }
}

/**
 * 表单验证器
 */
class FormValidator {
    /**
     * 验证登录表单
     * @param {object} formData - 表单数据
     * @returns {object} 验证结果
     */
    static validateLogin(formData) {
        const errors = {};
        
        if (!formData.username || formData.username.trim().length < 3) {
            errors.username = '用户名至少需要3个字符';
        }
        
        if (!formData.password || formData.password.length < 6) {
            errors.password = '密码至少需要6个字符';
        }
        
        return {
            isValid: Object.keys(errors).length === 0,
            errors
        };
    }

    /**
     * 验证注册表单
     * @param {object} formData - 表单数据
     * @returns {object} 验证结果
     */
    static validateRegister(formData) {
        const errors = {};
        
        // 用户名验证
        if (!formData.username || formData.username.trim().length < 3) {
            errors.username = '用户名至少需要3个字符';
        } else if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
            errors.username = '用户名只能包含字母、数字和下划线';
        }
        
        // 密码验证
        if (!formData.password || !Utils.validatePassword(formData.password)) {
            errors.password = '密码至少8位，包含字母和数字';
        }
        
        // 确认密码验证
        if (formData.password !== formData.confirmPassword) {
            errors.confirmPassword = '两次输入的密码不一致';
        }
        
        return {
            isValid: Object.keys(errors).length === 0,
            errors
        };
    }

    /**
     * 验证密码修改表单
     * @param {object} formData - 表单数据
     * @returns {object} 验证结果
     */
    static validateChangePassword(formData) {
        const errors = {};
        
        if (!formData.oldPassword) {
            errors.oldPassword = '请输入当前密码';
        }
        
        if (!formData.newPassword || !Utils.validatePassword(formData.newPassword)) {
            errors.newPassword = '新密码至少8位，包含字母和数字';
        }
        
        if (formData.newPassword !== formData.confirmPassword) {
            errors.confirmPassword = '两次输入的新密码不一致';
        }
        
        if (formData.oldPassword === formData.newPassword) {
            errors.newPassword = '新密码不能与当前密码相同';
        }
        
        return {
            isValid: Object.keys(errors).length === 0,
            errors
        };
    }
}

// 创建全局认证管理器实例
const authManager = new AuthManager();

// 导出到全局
window.authManager = authManager;
window.AuthManager = AuthManager;
window.PageGuard = PageGuard;
window.FormValidator = FormValidator;