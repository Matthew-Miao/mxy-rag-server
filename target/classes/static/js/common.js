/**
 * 公共工具函数库
 * 提供通用的工具方法和常量定义
 */

// 常量定义
const CONFIG = {
    API_BASE_URL: '/api/v1',
    TOKEN_KEY: 'auth_token',
    USER_KEY: 'user_info',
    SESSION_TIMEOUT: 24 * 60 * 60 * 1000, // 24小时
    MAX_FILE_SIZE: 10 * 1024 * 1024, // 10MB
    SUPPORTED_FILE_TYPES: ['.pdf', '.doc', '.docx', '.txt'],
    TYPING_DELAY: 50 // 打字效果延迟
};

/**
 * 工具函数类
 */
class Utils {
    /**
     * 显示消息提示
     * @param {string} message - 消息内容
     * @param {string} type - 消息类型 (success, error, info)
     * @param {number} duration - 显示时长(毫秒)
     */
    static showMessage(message, type = 'info', duration = 3000) {
        // 移除已存在的消息
        const existingAlert = document.querySelector('.alert-message');
        if (existingAlert) {
            existingAlert.remove();
        }

        // 创建新消息元素
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-message`;
        alertDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            max-width: 500px;
            padding: 15px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
            transform: translateX(100%);
            transition: transform 0.3s ease;
        `;
        alertDiv.textContent = message;

        // 添加到页面
        document.body.appendChild(alertDiv);

        // 显示动画
        setTimeout(() => {
            alertDiv.style.transform = 'translateX(0)';
        }, 100);

        // 自动隐藏
        setTimeout(() => {
            alertDiv.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.parentNode.removeChild(alertDiv);
                }
            }, 300);
        }, duration);
    }

    /**
     * 显示加载状态
     * @param {HTMLElement} element - 目标元素
     * @param {boolean} show - 是否显示加载状态
     * @param {string} text - 加载文本
     */
    static showLoading(element, show = true, text = '加载中...') {
        if (show) {
            element.disabled = true;
            element.dataset.originalText = element.textContent;
            element.innerHTML = `<span class="loading"></span> ${text}`;
        } else {
            element.disabled = false;
            element.textContent = element.dataset.originalText || element.textContent;
        }
    }

    /**
     * 格式化文件大小
     * @param {number} bytes - 字节数
     * @returns {string} 格式化后的文件大小
     */
    static formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    /**
     * 格式化时间
     * @param {string|Date} date - 日期
     * @returns {string} 格式化后的时间
     */
    /**
     * 格式化时间显示
     * @param {string|Date} date - 日期
     * @returns {string} 格式化后的时间字符串
     */
    static formatTime(date) {
        if (!date) return '未知时间';
        
        const target = new Date(date);
        
        // 检查日期是否有效
        if (isNaN(target.getTime())) {
            return '未知时间';
        }
        
        const now = new Date();
        const diff = now - target;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return '刚刚';
        if (minutes < 60) return `${minutes}分钟前`;
        if (hours < 24) return `${hours}小时前`;
        if (days < 7) return `${days}天前`;
        
        return target.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    /**
     * 防抖函数
     * @param {Function} func - 要防抖的函数
     * @param {number} wait - 等待时间
     * @returns {Function} 防抖后的函数
     */
    static debounce(func, wait) {
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

    /**
     * 节流函数
     * @param {Function} func - 要节流的函数
     * @param {number} limit - 限制时间
     * @returns {Function} 节流后的函数
     */
    static throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    /**
     * 验证邮箱格式
     * @param {string} email - 邮箱地址
     * @returns {boolean} 是否有效
     */
    static validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    /**
     * 验证密码强度
     * @param {string} password - 密码
     * @returns {object} 验证结果
     */
    static validatePassword(password) {
        const result = {
            valid: false,
            score: 0,
            message: ''
        };

        if (password.length < 6) {
            result.message = '密码长度至少6位';
            return result;
        }

        if (password.length >= 8) result.score += 1;
        if (/[a-z]/.test(password)) result.score += 1;
        if (/[A-Z]/.test(password)) result.score += 1;
        if (/[0-9]/.test(password)) result.score += 1;
        if (/[^\w\s]/.test(password)) result.score += 1;

        if (result.score >= 3) {
            result.valid = true;
            result.message = '密码强度良好';
        } else {
            result.message = '密码强度较弱，建议包含大小写字母、数字和特殊字符';
        }

        return result;
    }

    /**
     * 生成唯一ID
     * @returns {string} 唯一ID
     */
    static generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }

    /**
     * 深拷贝对象
     * @param {any} obj - 要拷贝的对象
     * @returns {any} 拷贝后的对象
     */
    static deepClone(obj) {
        if (obj === null || typeof obj !== 'object') return obj;
        if (obj instanceof Date) return new Date(obj.getTime());
        if (obj instanceof Array) return obj.map(item => this.deepClone(item));
        if (typeof obj === 'object') {
            const clonedObj = {};
            for (const key in obj) {
                if (obj.hasOwnProperty(key)) {
                    clonedObj[key] = this.deepClone(obj[key]);
                }
            }
            return clonedObj;
        }
    }

    /**
     * 获取文件扩展名
     * @param {string} filename - 文件名
     * @returns {string} 扩展名
     */
    static getFileExtension(filename) {
        return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2).toLowerCase();
    }

    /**
     * 检查文件类型是否支持
     * @param {string} filename - 文件名
     * @returns {boolean} 是否支持
     */
    static isSupportedFileType(filename) {
        const ext = '.' + this.getFileExtension(filename);
        return CONFIG.SUPPORTED_FILE_TYPES.includes(ext);
    }

    /**
     * 转义HTML字符
     * @param {string} text - 要转义的文本
     * @returns {string} 转义后的文本
     */
    static escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 打字机效果
     * @param {HTMLElement} element - 目标元素
     * @param {string} text - 要显示的文本
     * @param {number} speed - 打字速度
     */
    static typeWriter(element, text, speed = CONFIG.TYPING_DELAY) {
        let i = 0;
        element.textContent = '';
        
        function type() {
            if (i < text.length) {
                element.textContent += text.charAt(i);
                i++;
                setTimeout(type, speed);
            }
        }
        
        type();
    }

    /**
     * 滚动到元素
     * @param {HTMLElement} element - 目标元素
     * @param {string} behavior - 滚动行为
     */
    static scrollToElement(element, behavior = 'smooth') {
        element.scrollIntoView({ behavior, block: 'center' });
    }

    /**
     * 复制文本到剪贴板
     * @param {string} text - 要复制的文本
     * @returns {Promise<boolean>} 是否成功
     */
    static async copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch (err) {
            // 降级方案
            const textArea = document.createElement('textarea');
            textArea.value = text;
            document.body.appendChild(textArea);
            textArea.select();
            const success = document.execCommand('copy');
            document.body.removeChild(textArea);
            return success;
        }
    }
}

/**
 * 本地存储管理类
 */
class Storage {
    /**
     * 设置存储项
     * @param {string} key - 键
     * @param {any} value - 值
     * @param {number} expiry - 过期时间(毫秒)
     */
    static set(key, value, expiry = null) {
        const item = {
            value: value,
            expiry: expiry ? Date.now() + expiry : null
        };
        localStorage.setItem(key, JSON.stringify(item));
    }

    /**
     * 获取存储项
     * @param {string} key - 键
     * @returns {any} 值
     */
    static get(key) {
        const itemStr = localStorage.getItem(key);
        if (!itemStr) return null;

        try {
            const item = JSON.parse(itemStr);
            if (item.expiry && Date.now() > item.expiry) {
                localStorage.removeItem(key);
                return null;
            }
            return item.value;
        } catch (e) {
            return null;
        }
    }

    /**
     * 删除存储项
     * @param {string} key - 键
     */
    static remove(key) {
        localStorage.removeItem(key);
    }

    /**
     * 清空所有存储
     */
    static clear() {
        localStorage.clear();
    }
}

// 导出到全局
window.CONFIG = CONFIG;
window.Utils = Utils;
window.Storage = Storage;

// DOM加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    // 全局错误处理
    window.addEventListener('error', function(e) {
        console.error('Global error:', e.error);
        Utils.showMessage('系统发生错误，请刷新页面重试', 'error');
    });

    // 全局未处理的Promise拒绝
    window.addEventListener('unhandledrejection', function(e) {
        console.error('Unhandled promise rejection:', e.reason);
        Utils.showMessage('网络请求失败，请检查网络连接', 'error');
    });
});