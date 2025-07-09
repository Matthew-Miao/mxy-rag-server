/**
 * 知识库管理功能
 * 包括文件上传、文本上传、知识库搜索等功能
 */

class KnowledgeManager {
    constructor() {
        this.uploadQueue = [];
        this.isUploading = false;
        this.searchResults = [];
        this.init();
    }

    /**
     * 初始化知识库管理器
     */
    init() {
        this.setupEventListeners();
    }

    /**
     * 设置事件监听器
     */
    setupEventListeners() {
        // 监听认证状态变化
        window.addEventListener('auth:logout', () => {
            this.clearData();
        });
    }

    /**
     * 清除数据
     */
    clearData() {
        this.uploadQueue = [];
        this.isUploading = false;
        this.searchResults = [];
    }

    /**
     * 上传文件到知识库
     * @param {File|FileList} files - 文件对象或文件列表
     * @returns {Promise<Array>} 上传结果
     */
    async uploadFiles(files) {
        const fileArray = Array.from(files);
        const results = [];
        
        // 验证文件
        for (const file of fileArray) {
            const validation = this.validateFile(file);
            if (!validation.isValid) {
                Utils.showMessage(`文件 ${file.name}: ${validation.error}`, 'error');
                continue;
            }
            
            // 添加到上传队列
            const uploadItem = {
                id: Utils.generateId(),
                file,
                status: 'pending',
                progress: 0,
                error: null
            };
            
            this.uploadQueue.push(uploadItem);
            this.dispatchEvent('fileAdded', { uploadItem });
        }
        
        // 开始上传
        if (!this.isUploading) {
            this.processUploadQueue();
        }
        
        return results;
    }

    /**
     * 验证文件
     * @param {File} file - 文件对象
     * @returns {object} 验证结果
     */
    validateFile(file) {
        // 检查文件大小
        if (file.size > CONFIG.MAX_FILE_SIZE) {
            return {
                isValid: false,
                error: `文件大小超过限制 (${Utils.formatFileSize(CONFIG.MAX_FILE_SIZE)})`
            };
        }
        
        // 检查文件类型
        if (!Utils.isSupportedFileType(file.name)) {
            return {
                isValid: false,
                error: `不支持的文件类型，支持的类型: ${CONFIG.SUPPORTED_FILE_TYPES.join(', ')}`
            };
        }
        
        return { isValid: true };
    }

    /**
     * 处理上传队列
     */
    async processUploadQueue() {
        if (this.isUploading || this.uploadQueue.length === 0) {
            return;
        }
        
        this.isUploading = true;
        
        while (this.uploadQueue.length > 0) {
            const uploadItem = this.uploadQueue.find(item => item.status === 'pending');
            if (!uploadItem) break;
            
            await this.uploadSingleFile(uploadItem);
        }
        
        this.isUploading = false;
        this.dispatchEvent('uploadComplete');
    }

    /**
     * 上传单个文件
     * @param {object} uploadItem - 上传项
     */
    async uploadSingleFile(uploadItem) {
        try {
            uploadItem.status = 'uploading';
            this.dispatchEvent('fileStatusChanged', { uploadItem });
            
            const result = await api.uploadFile(
                uploadItem.file,
                (progress) => {
                    uploadItem.progress = progress;
                    this.dispatchEvent('fileProgress', { uploadItem });
                }
            );
            
            if (result.success) {
                uploadItem.status = 'completed';
                uploadItem.result = result.data;
                Utils.showMessage(`文件 ${uploadItem.file.name} 上传成功`, 'success');
            } else {
                throw new Error(result.message || '上传失败');
            }
        } catch (error) {
            uploadItem.status = 'failed';
            uploadItem.error = error.message;
            Utils.showMessage(`文件 ${uploadItem.file.name} 上传失败: ${error.message}`, 'error');
        }
        
        this.dispatchEvent('fileStatusChanged', { uploadItem });
    }

    /**
     * 插入文本内容到知识库
     * @param {string} content - 文本内容
     * @param {string} title - 文本标题（可选）
     * @returns {Promise<object>} 插入结果
     */
    async insertText(content, title = '') {
        try {
            if (!content || content.trim().length === 0) {
                throw new Error('文本内容不能为空');
            }
            
            if (content.length > 10000) {
                throw new Error('文本内容过长，请分段上传');
            }
            
            const textData = title ? `${title}\n\n${content}` : content;
            const response = await api.insertText(textData);
            
            if (response.code === 200) {
                Utils.showMessage('文本内容添加成功', 'success');
                this.dispatchEvent('textInserted', { content: textData, result: response.data });
                return { success: true, data: response.data };
            } else {
                throw new Error(response.message || '添加失败');
            }
        } catch (error) {
            console.error('插入文本失败:', error);
            Utils.showMessage(`添加失败: ${error.message}`, 'error');
            throw error;
        }
    }

    /**
     * 搜索知识库
     * @param {string} query - 搜索查询
     * @param {number} topK - 返回结果数量
     * @returns {Promise<Array>} 搜索结果
     */
    async searchKnowledge(query, topK = 5) {
        try {
            if (!query || query.trim().length === 0) {
                throw new Error('搜索内容不能为空');
            }
            
            const response = await api.searchKnowledge(query.trim(), topK);
            
            if (response.code === 200) {
                this.searchResults = response.data || [];
                this.dispatchEvent('searchCompleted', { 
                    query, 
                    results: this.searchResults 
                });
                return this.searchResults;
            } else {
                throw new Error(response.message || '搜索失败');
            }
        } catch (error) {
            console.error('搜索知识库失败:', error);
            Utils.showMessage(`搜索失败: ${error.message}`, 'error');
            throw error;
        }
    }

    /**
     * 移除上传项
     * @param {string} uploadId - 上传项ID
     */
    removeUploadItem(uploadId) {
        const index = this.uploadQueue.findIndex(item => item.id === uploadId);
        if (index !== -1) {
            const uploadItem = this.uploadQueue[index];
            this.uploadQueue.splice(index, 1);
            this.dispatchEvent('fileRemoved', { uploadItem });
        }
    }

    /**
     * 清空上传队列
     */
    clearUploadQueue() {
        this.uploadQueue = [];
        this.dispatchEvent('queueCleared');
    }

    /**
     * 重试上传
     * @param {string} uploadId - 上传项ID
     */
    async retryUpload(uploadId) {
        const uploadItem = this.uploadQueue.find(item => item.id === uploadId);
        if (uploadItem && uploadItem.status === 'failed') {
            uploadItem.status = 'pending';
            uploadItem.progress = 0;
            uploadItem.error = null;
            
            this.dispatchEvent('fileStatusChanged', { uploadItem });
            
            if (!this.isUploading) {
                this.processUploadQueue();
            }
        }
    }

    /**
     * 获取上传队列
     * @returns {Array} 上传队列
     */
    getUploadQueue() {
        return this.uploadQueue;
    }

    /**
     * 获取搜索结果
     * @returns {Array} 搜索结果
     */
    getSearchResults() {
        return this.searchResults;
    }

    /**
     * 获取上传统计
     * @returns {object} 上传统计
     */
    getUploadStats() {
        const total = this.uploadQueue.length;
        const completed = this.uploadQueue.filter(item => item.status === 'completed').length;
        const failed = this.uploadQueue.filter(item => item.status === 'failed').length;
        const pending = this.uploadQueue.filter(item => item.status === 'pending').length;
        const uploading = this.uploadQueue.filter(item => item.status === 'uploading').length;
        
        return {
            total,
            completed,
            failed,
            pending,
            uploading,
            isUploading: this.isUploading
        };
    }

    /**
     * 触发自定义事件
     * @param {string} eventName - 事件名称
     * @param {object} detail - 事件详情
     */
    dispatchEvent(eventName, detail = {}) {
        const event = new CustomEvent(`knowledge:${eventName}`, { detail });
        window.dispatchEvent(event);
    }

    /**
     * 监听知识库事件
     * @param {string} eventName - 事件名称
     * @param {Function} callback - 回调函数
     */
    on(eventName, callback) {
        window.addEventListener(`knowledge:${eventName}`, callback);
    }

    /**
     * 移除知识库事件监听
     * @param {string} eventName - 事件名称
     * @param {Function} callback - 回调函数
     */
    off(eventName, callback) {
        window.removeEventListener(`knowledge:${eventName}`, callback);
    }
}

/**
 * 文件拖拽处理器
 */
class FileDragHandler {
    /**
     * 设置拖拽区域
     * @param {HTMLElement} element - 拖拽区域元素
     * @param {Function} onFilesDropped - 文件拖拽回调
     */
    static setupDragArea(element, onFilesDropped) {
        let dragCounter = 0;
        
        element.addEventListener('dragenter', (e) => {
            e.preventDefault();
            dragCounter++;
            element.classList.add('drag-over');
        });
        
        element.addEventListener('dragleave', (e) => {
            e.preventDefault();
            dragCounter--;
            if (dragCounter === 0) {
                element.classList.remove('drag-over');
            }
        });
        
        element.addEventListener('dragover', (e) => {
            e.preventDefault();
        });
        
        element.addEventListener('drop', (e) => {
            e.preventDefault();
            dragCounter = 0;
            element.classList.remove('drag-over');
            
            const files = Array.from(e.dataTransfer.files);
            if (files.length > 0) {
                onFilesDropped(files);
            }
        });
    }
}

/**
 * 上传进度渲染器
 */
class UploadProgressRenderer {
    /**
     * 渲染上传项
     * @param {object} uploadItem - 上传项
     * @returns {HTMLElement} 上传项元素
     */
    static renderUploadItem(uploadItem) {
        const item = document.createElement('div');
        item.className = `upload-item ${uploadItem.status}`;
        item.dataset.uploadId = uploadItem.id;
        
        const info = document.createElement('div');
        info.className = 'upload-info';
        
        const name = document.createElement('div');
        name.className = 'file-name';
        name.textContent = uploadItem.file.name;
        
        const size = document.createElement('div');
        size.className = 'file-size';
        size.textContent = Utils.formatFileSize(uploadItem.file.size);
        
        info.appendChild(name);
        info.appendChild(size);
        
        const progress = document.createElement('div');
        progress.className = 'upload-progress';
        
        const progressBar = document.createElement('div');
        progressBar.className = 'progress-bar';
        
        const progressFill = document.createElement('div');
        progressFill.className = 'progress-fill';
        progressFill.style.width = `${uploadItem.progress}%`;
        
        progressBar.appendChild(progressFill);
        
        const status = document.createElement('div');
        status.className = 'upload-status';
        status.textContent = this.getStatusText(uploadItem);
        
        progress.appendChild(progressBar);
        progress.appendChild(status);
        
        const actions = document.createElement('div');
        actions.className = 'upload-actions';
        
        if (uploadItem.status === 'failed') {
            const retryBtn = document.createElement('button');
            retryBtn.className = 'btn btn-sm btn-secondary';
            retryBtn.innerHTML = '<i class="fas fa-redo"></i> 重试';
            retryBtn.onclick = () => {
                window.knowledgeManager.retryUpload(uploadItem.id);
            };
            actions.appendChild(retryBtn);
        }
        
        const removeBtn = document.createElement('button');
        removeBtn.className = 'btn btn-sm btn-danger';
        removeBtn.innerHTML = '<i class="fas fa-times"></i>';
        removeBtn.onclick = () => {
            window.knowledgeManager.removeUploadItem(uploadItem.id);
        };
        actions.appendChild(removeBtn);
        
        item.appendChild(info);
        item.appendChild(progress);
        item.appendChild(actions);
        
        return item;
    }

    /**
     * 更新上传项
     * @param {HTMLElement} element - 上传项元素
     * @param {object} uploadItem - 上传项
     */
    static updateUploadItem(element, uploadItem) {
        element.className = `upload-item ${uploadItem.status}`;
        
        const progressFill = element.querySelector('.progress-fill');
        if (progressFill) {
            progressFill.style.width = `${uploadItem.progress}%`;
        }
        
        const status = element.querySelector('.upload-status');
        if (status) {
            status.textContent = this.getStatusText(uploadItem);
        }
        
        // 更新操作按钮
        const actions = element.querySelector('.upload-actions');
        if (actions) {
            actions.innerHTML = '';
            
            if (uploadItem.status === 'failed') {
                const retryBtn = document.createElement('button');
                retryBtn.className = 'btn btn-sm btn-secondary';
                retryBtn.innerHTML = '<i class="fas fa-redo"></i> 重试';
                retryBtn.onclick = () => {
                    window.knowledgeManager.retryUpload(uploadItem.id);
                };
                actions.appendChild(retryBtn);
            }
            
            const removeBtn = document.createElement('button');
            removeBtn.className = 'btn btn-sm btn-danger';
            removeBtn.innerHTML = '<i class="fas fa-times"></i>';
            removeBtn.onclick = () => {
                window.knowledgeManager.removeUploadItem(uploadItem.id);
            };
            actions.appendChild(removeBtn);
        }
    }

    /**
     * 获取状态文本
     * @param {object} uploadItem - 上传项
     * @returns {string} 状态文本
     */
    static getStatusText(uploadItem) {
        switch (uploadItem.status) {
            case 'pending':
                return '等待上传';
            case 'uploading':
                return `上传中 ${Math.round(uploadItem.progress)}%`;
            case 'completed':
                return '上传完成';
            case 'failed':
                return `上传失败: ${uploadItem.error || '未知错误'}`;
            default:
                return '未知状态';
        }
    }
}

/**
 * 搜索结果渲染器
 */
class SearchResultRenderer {
    /**
     * 渲染搜索结果
     * @param {Array} results - 搜索结果
     * @returns {HTMLElement} 搜索结果容器
     */
    static renderSearchResults(results) {
        const container = document.createElement('div');
        container.className = 'search-results';
        
        if (results.length === 0) {
            const empty = document.createElement('div');
            empty.className = 'empty-state';
            empty.innerHTML = `
                <i class="fas fa-search"></i>
                <p>没有找到相关内容</p>
            `;
            container.appendChild(empty);
            return container;
        }
        
        results.forEach((result, index) => {
            const item = this.renderSearchResult(result, index);
            container.appendChild(item);
        });
        
        return container;
    }

    /**
     * 渲染单个搜索结果
     * @param {object} result - 搜索结果
     * @param {number} index - 索引
     * @returns {HTMLElement} 搜索结果项
     */
    static renderSearchResult(result, index) {
        const item = document.createElement('div');
        item.className = 'search-result-item';
        
        const header = document.createElement('div');
        header.className = 'result-header';
        
        const title = document.createElement('h4');
        title.textContent = result.title || `结果 ${index + 1}`;
        
        const score = document.createElement('span');
        score.className = 'result-score';
        score.textContent = `相似度: ${(result.score * 100).toFixed(1)}%`;
        
        header.appendChild(title);
        header.appendChild(score);
        
        const content = document.createElement('div');
        content.className = 'result-content';
        content.textContent = result.content;
        
        const meta = document.createElement('div');
        meta.className = 'result-meta';
        
        if (result.source) {
            const source = document.createElement('span');
            source.className = 'result-source';
            source.innerHTML = `<i class="fas fa-file"></i> ${result.source}`;
            meta.appendChild(source);
        }
        
        if (result.timestamp) {
            const time = document.createElement('span');
            time.className = 'result-time';
            time.innerHTML = `<i class="fas fa-clock"></i> ${Utils.formatTime(result.timestamp)}`;
            meta.appendChild(time);
        }
        
        item.appendChild(header);
        item.appendChild(content);
        item.appendChild(meta);
        
        return item;
    }
}

// 创建全局知识库管理器实例
const knowledgeManager = new KnowledgeManager();

// 导出到全局
window.knowledgeManager = knowledgeManager;
window.KnowledgeManager = KnowledgeManager;
window.FileDragHandler = FileDragHandler;
window.UploadProgressRenderer = UploadProgressRenderer;
window.SearchResultRenderer = SearchResultRenderer;