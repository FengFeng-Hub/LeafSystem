//对Ajax的封装
const Ajax = {
    /**
     * Ajax 请求
     * @param {object} options {
     *                              method: 'GET'//请求方法（GET/POST）
     *                          } 
     */
    request(options) {
        if(options.method !=null && (options.method == 'POST' || options.method == 'post')) {
            this.post(options);
        } else {
            this.get(options);
        }
    },
    /**
     * Ajax GET请求
     * @param {object} options {
     *                              url: '',//请求URL
     *                              param: {},//请求参数
     *                              success: function(data,xhr) {},//成功响应
     *                              error: function(data,xhr) {},//失败响应
     *                              async: true//是否是异步请求
     *                          } 
     */
    get(options) {
        //默认值
        let defaults = {
            url: '',
            param: {},
            header: {},
            success: function(data,xhr) {},
            error: function(data,xhr) {},
            async: true
        };
        //使用options对象中的属性覆盖defaults对象中的属性
        Object.assign(defaults, options);
        let xhr = new XMLHttpRequest();
        let obj = Object.keys(defaults.param);

        if(obj.length !== 0) {
            let params = obj.map(key => `${encodeURIComponent(key)}=${encodeURIComponent(defaults.param[key]?defaults.param[key]:'')}`).join('&');
            xhr.open('GET',`${defaults.url}?${params}`,defaults.async);
        } else {
            xhr.open('GET',defaults.url,defaults.async);
        }

        for(const key in defaults.header) {
            xhr.setRequestHeader(key, defaults.header[key]);
        }

        xhr.onload = function(e) {
            let result = null;
            try {
                result = JSON.parse(xhr.response);
            } catch(e) {
                result = xhr.response;
            }
            defaults.success(result,xhr);
        };
        xhr.onerror = function(e) {
            defaults.error(xhr.statusText,xhr);
        };
        xhr.send();
    },
    /**
     * Ajax POST请求
     * @param {object} options {
     *                              url: '',//请求URL
     *                              param: {},//请求参数
     *                              header: {},//请求头
     *                              success: function(data,xhr) {},//成功响应
     *                              error: function(data,xhr) {},//失败响应
     *                              async: true//是否是异步请求
     *                          } 
     */
    post(options) {
        //默认值
        let defaults = {
            url: '',
            param: {},
            header: {},
            success: function(data,xhr) {},
            error: function(data,xhr) {},
            async: true
        };
        //使用options对象中的属性覆盖defaults对象中的属性
        Object.assign(defaults, options);
        let xhr = new XMLHttpRequest();
        xhr.open('POST',defaults.url,defaults.async);

        for(const key in defaults.header) {
            xhr.setRequestHeader(key, defaults.header[key]);
        }

        xhr.onload = function(e) {
            let result = null;
            try {
                result = JSON.parse(xhr.response);
            } catch(e) {
                result = xhr.response;
            }
            defaults.success(result,xhr);
        };
        xhr.onerror = function(e) {
            defaults.error(xhr.statusText,xhr);
        };

        if(defaults.header["Content-Type"] != null && defaults.header["Content-Type"].includes('application/json')) {
            xhr.send(JSON.stringify(defaults.param));
        } else {
            // 判断请求参数是否含有文件
            const hasFile = Object.values(defaults.param).some(value =>
                value instanceof File || value instanceof Blob
            );

            if (hasFile) {
                let formData = new FormData();
                for (const key in defaults.param) {
                    formData.append(key,defaults.param[key]?defaults.param[key]:'');
                }
                xhr.send(formData);
            } else {
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                let data = new URLSearchParams();
                for (const key in defaults.param) {
                    data.append(key,defaults.param[key]?defaults.param[key]:'');
                }
                xhr.send(data.toString());
            }
        }
    },
    /**
     * 下载文件
     * @param {string} url URL路径
     * @param {string} fileName 文件名
     * @param {function(xhr)} error 失败时调用的函数
     * @param {boolean} async 是否异步
     */
    downloadFile(url,fileName,error,async) {
        new Promise(resolve => {
            const xhr = new XMLHttpRequest();
            xhr.open('GET', url, async);
            xhr.responseType = 'blob';
            xhr.onload = () => {
                if (xhr.status === 200) {
                    resolve(xhr.response);
                } else {
                    error(xhr);
                }
            };
            xhr.send();
        }).then((blob) => {
            if (window.navigator.msSaveOrOpenBlob) {
                navigator.msSaveBlob(blob, fileName);
            } else {
                const link = document.createElement('a');
                const body = document.querySelector('body');
                link.href = window.URL.createObjectURL(blob);
                link.download = fileName;
                link.style.display = 'none';
                body.appendChild(link);
                link.click();
                body.removeChild(link);
                window.URL.revokeObjectURL(link.href)
            }
        });
    }
};
//工具
const Util = {
    /**
     * 程序休眠
     * @param {number} delay 休眠时间（毫秒）
     */
    sleep(delay) {return new Promise((resolve) => setTimeout(resolve, delay));},
    /**
     * 验证
     */
    Valid: {
        /**
         * 验证当前设备是否是移动端
         * @returns {boolean} 是否是移动端
         */
        isMobile() {
            if(window.navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)) return true;
            return false;
        },
        /**
         * 验证是否为空
         * @param {string} val 需要验证的字符串
         * @returns {boolean} 是否为空
         */
        empty(val) {return val == undefined || val == null || val == '';},
        /**
         * 验证手机号
         * @param {string} val 需要验证的字符串
         * @returns {boolean} 是否为手机号
         */
        phone(val) {return /^1[3-9]\d{9}$/.test(val);},
        /**
         * 验证电子邮箱
         * @param {string} val 需要验证的字符串
         * @returns {boolean} 是否为电子邮箱
         */
        email(val) {return /^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/.test(val);},
        /**
         * 验证身份证号
         * @param {string} val 需要验证的字符串
         * @returns {boolean} 是否为身份证号
         */
        idcard(val) {return /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9]|X)$/.test(val);}
    },
    /**
     * 验证表单
     * @param {object} dom form表单的DOM元素
     * @param {object} options {
     *                              msgOutTagNum: 0,//消息向外标签数
     *                              fields: {},//请求参数
     *                              onsubmit: function() {}//验证成功后的提交事件
     *                          } 
     */
    ValidForm(dom,options) {
        let defaults = {
            msgOutTagNum: 0,//消息向外标签数
            fields: {},
            onsubmit: function() {}//验证成功后的提交事件
        };
        Object.assign(defaults, options);

        for(field in defaults.fields) {
            let element = document.getElementById(field);
            let _field = field;

            if(element == null) {
                continue;
            }

            element.addEventListener('input',function() {
                createValid(_field,element);
            });
        }

        document.querySelector(dom).onsubmit = function(e) {
            e.preventDefault();//阻止表单提交
            let bool = true;
            for(field in defaults.fields) {
                let element = document.getElementById(field);

                if(element == null) {
                    continue;
                }

                if(!createValid(field,element)) {
                    bool = false;
                }
            }

            if(bool) {
                defaults.onsubmit();
            }
        };

        function createValid(field,element) {
            let fieldObj = defaults.fields[field];
            //验证是否为空
            if(fieldObj.empty != undefined && fieldObj.empty != null) {
                if(element.value == null || element.value == '') {
                    noPass(field,element,fieldObj.empty);
                    return false;
                }
            }
            //验证手机号格式
            if(fieldObj.notPhone != undefined && fieldObj.notPhone != null) {
                if(!Util.Valid.phone(element.value)) {
                    noPass(field,element,fieldObj.notPhone);
                    return false;
                }
            }
            //验证电子邮箱格式
            if(fieldObj.notEmail != undefined && fieldObj.notEmail != null) {
                if(!Util.Valid.email(element.value)) {
                    noPass(field,element,fieldObj.notEmail);
                    return false;
                }
            }
            //自定义验证
            if(fieldObj.rule != undefined && fieldObj.rule != null) {
                if(!fieldObj.rule.callback(element) && fieldObj.rule.msg != undefined) {
                    noPass(field,element,fieldObj.rule.msg);
                    return false;
                }
            }
            pass(field,element);
            return true;
        }

        function pass(field,element) {
            if(element.tagName == 'INPUT' || element == 'input') {
                element.classList.remove('lyf-valid-warning');
            }
            let tip = document.querySelector('p[lyf-valid-field='+field+']');
            if(tip != null) {
                tip.remove();
            }
        }

        function noPass(field,element,msg) {
            if(element.tagName == 'INPUT' || element == 'input') {
                element.classList.add('lyf-valid-warning');
            }
            let tip = document.querySelector('p[lyf-valid-field='+field+']');
            if(tip == null) {
                tip = document.createElement('p');
                tip.style.color = 'red';
                tip.setAttribute('lyf-valid-field',field);
                
                for(let i = 0;i < defaults.msgOutTagNum;i ++) {
                    element = element.parentElement;
                }

                element.insertAdjacentElement('afterend',tip);
            }
            tip.innerHTML = msg;
        }
    }
};
/**
 * DOM节点操作
 */
let Dom = {
    /**
     * head
     */
    head: document.head,
    /**
     * body
     */
    body: document.body,
    /**
     * 通过CSS选择器得到第一个HTML元素
     * @param {string} str css选择器
     * @returns HTML元素
     */
    css(str) {return document.querySelector(str);},
    /**
     * 通过CSS选择器得到所有HTML元素
     * @param {string} str css选择器
     * @returns HTML元素数组
     */
    cssAll(str) {return document.querySelectorAll(str);},
    /**
     * 通过id选择器得到HTML元素
     * @param {string} str id
     * @returns HTML元素
     */
    id(str) {return document.getElementById(str);},
    /**
     * 通过class选择器得到所有HTML元素
     * @param {string} str class
     * @returns HTML元素数组
     */
    class(str) {return document.getElementsByClassName(str);},
    /**
     * 通过name选择器得到所有HTML元素
     * @param {string} str name
     * @returns HTML元素数组
     */
    name(str) {return document.getElementsByName(str);},
    /**
     * 通过标签名选择器得到所有HTML元素
     * @param {string} str 标签名
     * @returns HTML元素数组
     */
    tag(str) {return document.getElementsByTagName(str);},
    /**
     * 创建HTML元素
     * @param {string} str 标签
     * @returns HTML元素
     */
    create(str) {return document.createElement(str);},
    /**
     * 通过HTML字符创建HTML元素
     * @param {string} str HTML字符 例如:<h1 title=“一级标题”  class=“red”>DOM文档对象模型</h1>
     * @returns HTML元素
     */
     creates(str) {
        let div = document.createElement('div');
        div.innerHTML = str.trim();//去除首尾的空字符串
        return div.firstChild;
    },
    /**
     * 获取单选框控件被选中的值
     * @param {string} nameStr 单选框控件的name属性值
     * @returns 被选中的value
     */
    getRadioChecked(nameStr) {
        let radios = document.getElementsByName(nameStr);
        for(let i = 0;i < radios.length;i++) {
            if(radios[i].checked) {
                return radios[i].value;
            }
        }
        return null;
    },
    /**
     * 设置单选框控件被选中的值
     * @param {string} nameStr 单选框控件的name属性值
     * @param {string} val 需要被选中的值
     */
    setRadioChecked(nameStr,val) {
        let radios = document.getElementsByName(nameStr);
        for(let i = 0;i < radios.length;i++) {
            if(radios[i].value == val) {
                radios[i].checked = true;
                break;
            }
        }
    },
    /**
     * 获取复选框控件被选中的值
     * @param {string} nameStr 复选框控件的name属性值
     * @returns 被选中的value数组
     */
    getCheckBoxChecked(nameStr) {
        let results = [];
        let checkboxs = document.getElementsByName(nameStr);
        for(let i = 0;i < checkboxs.length;i++) {
            if(checkboxs[i].checked) {
                results.push(checkboxs[i].value);
            }
        }
        return results;
    },
    /**
     * 设置复选框控件被选中的值
     * @param {string} nameStr 复选框控件的name属性值
     * @param {array} vals 需要被选中的值的数组
     */
    setCheckBoxChecked(nameStr,vals) {
       let checkboxs = document.getElementsByName(nameStr);
       for(let i = 0;i < checkboxs.length;i++) {
            if(vals.includes(checkboxs[i].value)) {
                    checkboxs[i].checked = true;
            }
       }
   },
    /**
     * 获取多选下拉框选择的选项
     * @param {string} css选择器
     * @returns [] 被选中的value数组
     */
    getMultipleSelectSelected(css) {
        let arr = [];
        let options = document.querySelector(css).options;
        for(let i = 0; i < options.length; i++) {
            // 如果该option被选中，则将它的value存入数组
            if(options[i].selected) {
                arr.push(options[i].value)
            }
        }
        return arr;
    },
    /**
     * 设置多选下拉框选择的选项
     * @param {string} css
     * @param {Array} arr
     */
    setMultipleSelectSelected(css,arr) {
        let options = document.querySelector(css).options;
        for(let i = 0; i < options.length; i++) {
            if(arr.includes(options[i].value)) {options[i].setAttribute('selected',true);}
        }
    }
};
/**
 * 模态框 
 */
const Modal = {
    /**
     * 显示模态框
     * @param {string} content 内容
     * @param {string} style 模态框类型
     * @param {object} options {
     *                              time: -1,//显示时间，-1表示不限时(单位毫秒)
     *                              top: '30%',//模态框距离顶端的距离
     * 								default: '',输入框默认值
     * 								isUseButton: true,//是否使用按钮
     * 								close: true,//开启关闭功能
     *								buttons: [],//按钮
     *								afterClickClose: true,//点击按钮后关闭
     *								click(index,e,result) {}//按钮点击回调函数
     *                          } 
     * @returns 模态框元素
     */
    show(content,type = 'custom',options) {
        let defaults = {
            time: -1,
            top: '30%',
            default: '',
            isUseButton: true,
            buttons: [],
            close: true,
            afterClickClose: true,
            click(index) {}
        };
        Object.assign(defaults, options);
        const modalDom = document.createElement("div");
        const rdmId = 'lyf-modal_' + (Math.random().toString(36).slice(2));//产生随机ID
        modalDom.id = rdmId;
        let icon = '';
        let widthStr = '';

        if (window.innerWidth > 500 && type != 'custom') {
            widthStr = 'max-width:35%;';
        } else {
            widthStr = 'max-width:85%;';
        }

        let html = `<div id="${rdmId}-cover" style="position: fixed;z-index: 5000;top: 0;right: 0;left: 0;bottom: 0;background: rgba(0,0,0,0.3);"></div>
            <style>
                .${rdmId}-box-1 {
                    margin:0 auto;
                    width: fit-content;
                    ${widthStr}
                    max-height: 90%;
                    position: fixed;
                    left: 16px;
                    top: ${defaults.top};
                    right: 16px;
                    background-color: #fff;
                    border-radius: 10px;
                    animation: ${rdmId}-alert .4s;
                    overflow: hidden;
                    z-index: 5001;
                }
                .${rdmId}-box-2 {
                    min-height: 40px;
                    padding: 30px;
                    display: flex;
                    overflow-y: auto;
                    font-size: 12px;
                    line-height: 1.4;
                    word-wrap: break-word;
                    color: rgba(0, 0, 0, 0.65);
                }
                @keyframes ${rdmId}-alert {
                    0% {opacity: 0;transform: scale(0);}
                    100% {opacity: 1;transform: scale(1);}
                }
                .${rdmId}-close {position: absolute;right: 10px;top: 5px;cursor:pointer;}
                .${rdmId}-close:hover {color: red;}
                .${rdmId}-icon {min-width: 25px;width: 25px;height: 25px;}
                #${rdmId}-content {margin-left: 10px;}
                .${rdmId}-btns {border-top: 1px solid #ccc;display: flex;}
                .${rdmId}-btn {
                    width: 100%;
                    background: none;
                    border: none;
                    padding: 8px 0;
                    color: #007bff;
                    overflow: hidden;
                    cursor:pointer;
                    font-size: 14px;
                    font-weight: 700;
                }
                .${rdmId}-btn:hover {background: #007bff;color: white;transition: 0.3s all 0s ease;}
            </style>
            <div class="${rdmId}-box-1">
                ${defaults.close?`<div class="${rdmId}-close" onclick="document.body.removeChild(document.getElementById('${rdmId}'))">&#10005</div>`:''}`;
        if(type == 'success' || type == 'info' || type == 'warning' || type == 'error') {
            html += `
                <div class="${rdmId}-box-2">
                    <span class="${rdmId}-icon lyf-icon-${type}"></span>
                    <div id="${rdmId}-content">${content}</div>
                </div>`;
            if(defaults.isUseButton) {
                html += `<div class="${rdmId}-btns">`;
                    if (defaults.buttons.length > 0) {
                    for (let x in defaults.buttons) {
                        html += `<button class="${rdmId}-btn"${(x > 0 ? ' style="border-left:1px solid #ccc;"' : '')}>${defaults.buttons[x]}</button>`;
                    }
                } else {
                    html += `<button class="${rdmId}-btn" default="true">确定</button>`;
                }
                html += '</div>';
            }
        } else if(type == 'input') {
            html += `
                <div class="${rdmId}-box-2">
                    <div>
                        <div id="${rdmId}-content" style="margin: 0;color: rgba(0,0,0,0.5);">${content}</div>
                        <input type="text" id="${rdmId}-input" style="width:100%;height:35px;outline: none;border: 1px solid #ccc;border-radius: 5px;font-size:20px; padding:0 5px;font-weight: bold;"
                            value="${(defaults.default == '' ? '' : defaults.default)}">
                    </div>
                </div>
                <div class="${rdmId}-btns">`;
            if (defaults.buttons.length > 0) {
                for (let x in defaults.buttons) {
                    html += `<button class="${rdmId}-btn"${(x > 0 ? ' style="border-left:1px solid #ccc;"' : '')}>${defaults.buttons[x]}</button>`;
                }
            } else {
                html += `
                    <button class="${rdmId}-btn">取消</button>
                    <button class="${rdmId}-btn" style="border-left:1px solid #ccc;">确定</button>`;
            }
            html += '</div>';
        } else {
            html += content;
            if (defaults.buttons.length > 0) {
                html += `<div class="${rdmId}-btns">`;
                for (let x in defaults.buttons) {
                    html += `<button class="${rdmId}-btn"${(x > 0 ? ' style="border-left:1px solid #ccc;"' : '')}>${defaults.buttons[x]}</button>`;
                }
                html += '</div>';
            }
        }
        
        html += '</div>';
        modalDom.innerHTML = html;
        document.body.appendChild(modalDom);
        document.activeElement.blur();//使具有焦点的元素失去焦点

        const btns = document.getElementsByClassName(rdmId + '-btn');
                
        for(let i in btns) {
            if(typeof btns[i] == 'object') {
                if(btns[i].getAttribute('default') == 'true') {
                    btns[i].focus();
                }

                btns[i].addEventListener('click', () => {
                    let result = null;
                    if(type == 'input') {
                        result = document.getElementById(rdmId + '-input').value;
                    }
                    if(defaults.afterClickClose) {
                        modalDom.remove();
                    }
                    defaults.click(i,modalDom,result);
                });
            }
        }

        if(defaults.close) {
            document.getElementById(rdmId + '-cover').addEventListener('click', () => {
                modalDom.remove();
            });
        }

        if(defaults.time != -1) {
            setTimeout(() => {
                if(document.getElementById(rdmId) != null) {
                    modalDom.remove();
                }
            }, defaults.time);
        }

        return modalDom;
    },
    /**
     * 设置模态框消息体
     * @param {string} e 需要设置消息体的模态框元素
     * @param {string} content 内容
     */
    setText(e,content) {
        let contentDom = document.getElementById(e + '-content');
        if(contentDom != null) {
            contentDom.innerHTML = content;
        }
    },
    /**
     * 成功模态框
     * @param {string} content 内容
     * @param {object} options 模态框选项
     * @returns 模态框元素
     */
    success(content,options) {
        return this.show(content,'success',options);
    },
    /**
     * 信息模态框
     * @param {string} content 内容
     * @param {object} options 模态框选项
     * @returns 模态框元素
     */
    info(content,options) {
        return this.show(content,'info',options);
    },
    /**
     * 警告模态框
     * @param {string} content 内容
     * @param {object} options 模态框选项
     * @returns 模态框元素
     */
    warning(content,options) {
        return this.show(content,'warning',options);
    },
    /**
     * 错误模态框
     * @param {string} content 内容
     * @param {object} options 模态框选项
     * @returns 模态框元素
     */
    error(content,options) {
        return this.show(content,'error',options);
    },
    /**
     * 输入框模态框
     * @param {string} content 内容
     * @param {object} options 模态框选项
     * @returns 模态框元素的id
     */
    input(content,options) {
        return this.show(content,'input',options);
    },
    /**
     * 自定义模态框
     * @param {string} content 内容
     * @param {object} options 模态框选项
     * @returns 模态框元素的id
     */
    custom(content,options) {
        return this.show(content,'custom',options);
    },
};
/**
 * 消息提示条
 */
const Message = {
    messageList: [],
    /**
     * 显示一条消息提示条
     * @param {string} content 内容
     * @param {string} type 消息类型 "info"|"success"|"warning"|"error"
     * @param {number} duration 持续时间，优先级比默认值高，-1表示不限时(单位毫秒)
     */
    show(content, type = "info", duration) {
        const rdmId = 'lyf-message_' + (Math.random().toString(36).slice(2));//产生随机ID
        const messageDom = document.createElement("div");
        messageDom.id = rdmId;
        messageDom.style.top = `${this.getItemTop()}px`;
        messageDom.style.zIndex = 5000;
        messageDom.innerHTML = `
            <style>
                #${rdmId},#${rdmId} span {box-sizing: border-box;}
                #${rdmId} {
                    position: fixed;
                    top: 0;
                    left: 50%;
                    display: flex;
                    padding: 10px 15px;
                    border-radius: 5px;
                    background-color: #fff;
                    box-shadow: var(--lyf-box-shadow-4);
                    white-space: nowrap;
                    animation: ${rdmId}-move .4s;
                    transition: .4s all;
                    transform: translate3d(-50%, 0%, 0);
                    opacity: 1;
                    overflow: hidden;
                }
                #${rdmId}::after {content: "";position: absolute;left: 0;top: 0;height: 100%;width: 4px;background-color: var(--lyf-color-${type});}
                @keyframes ${rdmId}-move {
                    0% {opacity: 0;transform: translate3d(-50%, -100%, 0);}
                    100% {opacity: 1;transform: translate3d(-50%, 0%, 0);}
                }
                #${rdmId}.${rdmId}-hide {opacity: 0;transform: translate3d(-50%, -100%, 0) scale(0);}
            </style>
            <span class="lyf-icon-${type}" style="width: 18px;height: 18px;"></span>
            <span style="margin-left: 5px;font-size: 14px;line-height: 18px;color: #555;">${content}</span>`;
        this.messageList.push(messageDom);
        document.body.appendChild(messageDom);
        // 添加动画监听事件
        function animationEnd() {
            messageDom.removeEventListener("animationend", animationEnd);
            if(duration != -1) setTimeout(Message.removeItem, duration || 3000, messageDom);
        }
        messageDom.addEventListener("animationend", animationEnd);
        function transitionEnd() {
            if (getComputedStyle(messageDom).opacity !== "0") return;
            messageDom.removeEventListener("transitionend", transitionEnd);
            messageDom.remove();
        }
        messageDom.addEventListener("transitionend", transitionEnd);
    },
    /**
     * 获取指定`item`的定位`top`
     * @param {HTMLElement} el 需要获取高度的消息提示条元素
     */
    getItemTop(el) {
        let top = 10;
        for (let i = 0; i < Message.messageList.length; i++) {
            const item = Message.messageList[i];
            if (el && el === item) {
                break;
            }
            top += item.clientHeight + 20;
        }
        return top;
    },
    /**
     * 删除指定列表项
     * @param {HTMLElement} el 需要删除的消息提示条元素
     */
    removeItem(el) {
        for (let i = 0; i < Message.messageList.length; i++) {
            const item = Message.messageList[i];
            if (item === el) {
                Message.messageList.splice(i, 1);
                break;
            }
        }
        el.classList.add(el.id + '-hide');
        Message.messageList.forEach(function(item) {
            item.style.top = `${Message.getItemTop(item)}px`;
        });
    },
    /**
     * 显示一条信息消息提示条
     * @param {string} content 内容
     * @param {number} duration 持续时间，优先级比默认值高
     */
    info(content,duration) {
        this.show(content,'info',duration);
    },
    /**
     * 显示一条成功消息提示条
     * @param {string} content 内容
     * @param {number} duration 持续时间，优先级比默认值高
     */
    success(content,duration) {
        this.show(content,'success',duration);
    },
    /**
     * 显示一条警告消息提示条
     * @param {string} content 内容
     * @param {number} duration 持续时间，优先级比默认值高
     */
    warning(content,duration) {
        this.show(content,'warning',duration);
    },
    /**
     * 显示一条失败消息提示条
     * @param {string} content 内容
     * @param {number} duration 持续时间，优先级比默认值高
     */
    error(content,duration) {
        this.show(content,'error',duration);
    }
}
