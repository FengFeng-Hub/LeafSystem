// /**
//  * 乾坤微前端
//  */
// QiankunUtil = {
//     toPage(url, params) {
//         this.appManager.loadApp({
//             el: '#ls-main-container',
//             path: url,
//             params: params
//         });
//     },
//     // 获取生命周期
//     getLifecycles: (config) => {
//         // console.log(window.QiankunUtil.appManager.currentApp);
//         // 动态生成全局变量名
//         const globalVarName = window.QiankunUtil.appManager.currentApp;
//         // 确保全局变量存在
//         window[globalVarName] = window[globalVarName] || {};
//
//         // 生命周期函数
//         let QiankunLifecycles = {
//             bootstrap(props) {
//                 // console.log(`[${globalVarName}] bootstrap`, props);
//                 // 隐藏加载图标
//                 window['LeafSystemApp'].isShowLoadIcon = false;
//                 if(config && config.bootstrap) {
//                     config.bootstrap(props);
//                 }
//                 return Promise.resolve();
//             },
//             mount(props) {
//                 // console.log(`[${globalVarName}] mount`, props);
//                 if(config && config.mount) {
//                     config.mount(props);
//                 }
//                 return Promise.resolve();
//             },
//             unmount(props) {
//                 // console.log(`[${globalVarName}] unmount`, props);
//                 if(config && config.unmount) {
//                     config.unmount(props);
//                 }
//                 return Promise.resolve();
//             }
//         };
//
//         window[globalVarName] = QiankunLifecycles;
//     },
//     // 应用管理器
//     appManager: {
//         currentApp: null,
//         microApps: {},
//         // 加载应用
//         loadApp: async function(options) {
//             try {
//                 // 卸载当前应用
//                 if (this.currentApp && this.microApps[this.currentApp]) {
//                     await this.unmountApp(this.currentApp);
//                 }
//
//                 // 自定义配置
//                 const config = {
//                     fetch: (url, ...args) => {
//                         return window.fetch(url, ...args);
//                     }
//                 };
//
//                 // 加载新应用
//                 this.currentApp = `qiankunapp_${Date.now()}${Math.random().toString(36).substring(2, 8)}`; // 时间戳加随机数
//
//                 this.microApps[this.currentApp] = qiankun.loadMicroApp({
//                     name: this.currentApp,
//                     entry: `${options.path}${options.path.includes('?') ? '&' : '?'}appname=${this.currentApp}`,
//                     container: options.el,
//                     props: {
//                         // 自定义参数
//                         params: options.params?options.params:{},
//                         url: options.path,
//                         // url参数
//                         urlParams: new URLSearchParams(options.path.split('?')[1])
//                     }
//                 }, config);
//             } catch (error) {
//                 console.error('加载应用失败:', error);
//             }
//         },
//         // 安全卸载应用
//         unmountApp: async function(appName) {
//             try {
//                 if (this.microApps[appName]) {
//                     await this.microApps[appName].unmount();
//                     delete this.microApps[appName];
//                 }
//             } catch (error) {
//                 console.error(`卸载应用${appName}失败:`, error);
//             }
//         }
//     }
// }

/**
 * 乾坤微前端
 */
QiankunUtil = {
    toPage(url, params) {
        this.appManager.loadApp({
            el: '#ls-main-container',
            path: url,
            params: params
        });
    },
    // 获取生命周期
    getLifecycles: (config) => {
        // console.log(window.QiankunUtil.appManager.currentApp);
        // 动态生成全局变量名
        const globalVarName = window.QiankunUtil.appManager.currentApp;
        // 确保全局变量存在
        window[globalVarName] = window[globalVarName] || {};

        // 生命周期函数
        let QiankunLifecycles = {
            bootstrap(props) {
                // console.log(`[${globalVarName}] bootstrap`, props);
                // 隐藏加载图标
                window['LeafSystemApp'].isShowLoadIcon = false;
                if(config && config.bootstrap) {
                    config.bootstrap(props);
                }
                return Promise.resolve();
            },
            mount(props) {
                // console.log(`[${globalVarName}] mount`, props);
                if(config && config.mount) {
                    config.mount(props);
                }
                return Promise.resolve();
            },
            unmount(props) {
                // console.log(`[${globalVarName}] unmount`, props);
                if(config && config.unmount) {
                    config.unmount(props);
                }
                return Promise.resolve();
            }
        };

        window[globalVarName] = QiankunLifecycles;
    },
    // 应用管理器
    appManager: {
        currentApp: null,
        microApps: {},
        // 加载应用
        loadApp: async function(options) {
            try {
                // 卸载当前应用
                if (this.currentApp && this.microApps[this.currentApp]) {
                    await this.unmountApp(this.currentApp);
                }

                // 自定义配置
                const config = {
                    fetch: (url, ...args) => {
                        return window.fetch(url, ...args);
                    }
                };

                // 加载新应用
                this.currentApp = `qiankunapp_${Date.now()}${Math.random().toString(36).substring(2, 8)}`; // 时间戳加随机数

                const parentUrl = options.path.split('/');
                parentUrl.pop();

                this.microApps[this.currentApp] = qiankun.loadMicroApp({
                    name: this.currentApp,
                    entry: `${options.path}${options.path.includes('?') ? '&' : '?'}appname=${this.currentApp}`,
                    container: options.el,
                    props: {
                        // 自定义参数
                        params: options.params?options.params:{},
                        url: options.path,
                        parentUrl: parentUrl.join('/') + '/',
                        // url参数
                        urlParams: new URLSearchParams(options.path.split('?')[1])
                    }
                }, config);
            } catch (error) {
                console.error('加载应用失败:', error);
            }
        },
        // 安全卸载应用
        unmountApp: async function(appName) {
            try {
                if (this.microApps[appName]) {
                    await this.microApps[appName].unmount();
                    delete this.microApps[appName];
                }
            } catch (error) {
                console.error(`卸载应用${appName}失败:`, error);
            }
        }
    }
}

// Vue 组件
// 递归菜单组件
Vue.component('lf-menu-item', {
    functional: true,  // 声明 lf-menu-item 为函数式组件，避免上下文问题
    props: ["item"],
    render(h, ctx) {
        const item = ctx.props.item;
        if (item.is_show !== '1') {
            return;
        }
        // 是否是目录
        if (item.type  === '1') {
            return h(
                'a-sub-menu',
                {
                    key: item.menu_id,
                    props: { key: item.menu_id }
                },
                // 子组件
                [
                     h(
                        'span',
                        {
                            slot: 'title'
                        },
                        [
                            (item.menu_icon && item.menu_icon !== '') ? h('a-icon', { props: { type: item.menu_icon } }) : null,
                            h('span', [item.menu_name])
                        ]
                    ),
                    item.child_menu?.map(child =>
                        h('lf-menu-item', { props: { item: child }, key: child.menu_id })
                    )
                ]);
        } else {
            return h(
                'a-menu-item',
                {
                    key: item.menu_id,
                    // 添加自定义属性
                    attrs: {
                        'menu-id': item.menu_id,
                        'path': item.url && item.url !== ''?item.url:'/error/404.html'
                    },
                    props: {
                        key: item.menu_id
                    }
                },
                [
                    item.menu_icon && item.menu_icon !== '' ? h('a-icon', { props: { type: item.menu_icon } }) : null,
                    h('span', [item.menu_name])
                ]);
        }
    }
});

// main vue app
let urlParams = new URLSearchParams(window.location.search);
// 权限键列表
let permissionKeyList = null;
// 注册Ant Design Vue组件
Vue.use(antd);
// 解决导航栏openChange事件不生效的问题
Vue.use(window['vue-dash-event']);
// 设置日期选择器为中文
// moment.locale('zh-cn');

// 使用全局函数插件
Vue.use({
    install(Vue) {
        Vue.prototype.$ls = {
            backendUser: {
                showAvatar: '/public/img/system/default_avatar.jpg'
            },
            /**
             * 是否有权限
             * @param permissionKey 权限键
             * @returns true有权限
             */
            hasPermission (permissionKey) {
                return !permissionKey || permissionKey === '' || permissionKeyList.includes(permissionKey);
            },
            table: {
                /**
                 * 更新分页和排序
                 * @param params
                 */
                updatePaginationAndSort(params) {
                    // 更新列的排序状态
                    if (params.SortField && params.SortField !== '') {
                        this.tableColumns = this.tableColumns.map(column => {
                            const newColumn = {...column};
                            if (newColumn.dataIndex === params.SortField) {
                                newColumn.sortOrder = params.SortOrder === 'asc'?'ascend':params.SortOrder === 'desc'?'descend':'';
                            } else {
                                delete newColumn.sortOrder;
                            }
                            return newColumn;
                        });
                    }
                    this.tablePagination.pageSize = params.PageCount;
                    this.tablePagination.current = params.PageNo;
                    this.tableSort.field = params.SortField;
                    this.tableSort.order = params.SortOrder;
                },
                /**
                 * 清除排序状态
                 */
                clearSort() {
                    // 清除所有列的排序状态
                    this.tableColumns = this.tableColumns.map(column => {
                        // 复制列配置，移除排序相关状态
                        const newColumn = {...column};
                        newColumn.sortOrder = null; // 清除排序指示状态
                        return newColumn;
                    });

                    this.tableSort.field = '';
                    this.tableSort.order = '';
                }
            },
            file: {
                /**
                 * 从URL中提取文件名
                 * @param url 需要提取文件名的URL
                 * @returns {string}
                 */
                getNameByUrl(url) {
                    if (!url) return '';
                    return url.substring(url.lastIndexOf('/') + 1);
                }
            }
        };
    }
});

// 主容器元素
let lsMainContainer;

function createLeafSystemApp() {
    let _this;
    window['LeafSystemApp'] = new Vue({
        el: "#ls-main-app",
        menuId: null,
        data: {
            // 菜单树结构
            menuTreeData: [],
            // 选中的菜单项
            selectedKeys: [],
            // 打开的菜单目录
            openKeys: [],
            // 菜单主题
            menuTheme: 'dark',
            // 面包屑导航
            breadcrumb: [],
            // 是否收缩侧边栏
            collapsed: false,
            // 是否固定头部
            isHeaderFixed: true,
            // 是否显示加载图标
            isShowLoadIcon: false,
            // 是否打开抽屉
            isShowDrawer: false,
            // 当前菜单类型
            currentMenu: {},
            // 是否启用多页签功能
            enableMultiTab: true,
            // 页签列表
            tabList: [],
            // 当前激活的页签key
            activeTabKey: '',
            // 显示页签右键菜单
            showTabContextmenu: false,
            // 页签右键菜单位置
            tabContextmenuPosition: {},
            // 激活右键菜单的页签key
            activeContextmenuTabKey: ''
        },
        beforeCreate() {
            _this = this;
            Ajax.get({
                url: '/system/api/user/isLogin',
                param: {
                    IsBackend: 1
                },
                success(result) {
                    if(result.IsSuccess === '1') {
                        _this.$ls.backendUser = result.data;

                        if(result.data.avatar != null && result.data.avatar !== '') {
                            _this.$ls.backendUser.showAvatar = result.data.avatar;
                        }
                    } else {
                        location.href = './login';
                    }
                }
            });
            // 获取权限键列表
            Ajax.get({
                url: '/system/api/role/getRoleMenuPermissionKeyList',
                success(result) {
                    if(result.IsSuccess === '1') {
                        permissionKeyList = result.data.map(item => {
                            return item.permission_key;
                        });

                    } else {
                        _this.message.error('初始化错误');
                    }
                }
            });
        },
        created() {
            // 菜单
            Ajax.get({
                url: '/system/api/menu/getMenuTree',
                success(result) {
                    if (result.IsSuccess == '1') {
                        _this.menuTreeData = result.data;
                    } else {
                        _this.$error({
                            title: '系统错误',
                            content: result.Msg,
                        });
                    }
                },
                async: false
            });
            // 系统配置
            Ajax.get({
                url: '/system/api/systemConfig/getSystemConfig',
                param: {
                    ConfigKeys: 'SysMenuTheme,SysFixHeader,SysEnableMultiTab'
                },
                success(result) {
                    console.log(result)
                    if (result.IsSuccess == '1') {
                        _this.menuTheme = result.data.SysMenuTheme;
                        _this.isHeaderFixed = result.data.SysFixHeader === '1';
                        _this.enableMultiTab = result.data.SysEnableMultiTab === '1';
                    } else {
                        _this.message.error('获取系统配置失败，将使用默认配置');
                    }
                }
            });
        },
        // 挂载完成后调用
        mounted() {
            this.menuId = urlParams.get('mid');
            lsMainContainer = document.getElementById('ls-main-container');
            this.showMenuView();
        },
        methods: {
            onOpenChange(openKeys) {
                if (openKeys.length > 0) {
                    // 保证只有一个菜单展开
                    const latestOpenKey = openKeys.find(key => this.openKeys.indexOf(key) === -1);

                    if (latestOpenKey) {
                        this.setOpenKeys(latestOpenKey);
                    } else {
                        this.openKeys = openKeys;
                    }
                } else {
                    this.openKeys = openKeys;
                }
            },
            toPage(menuId) {
                if (menuId !== this.menuId) {
                    this.menuId = menuId;
                    this.showMenuView();
                }
            },
            // 导航栏点击事件
            handleMenuClick(e) {
                this.menuId = e.key;
                this.showMenuView();
            },
            // 显示菜单视图
            showMenuView() {
                QiankunUtil.appManager.unmountApp(QiankunUtil.appManager.currentApp);
                let findMenu;
                if (this.menuId) {
                    // 递归查找菜单
                    findMenu = (menus) => {
                        for (const menu of menus) {
                            if (menu.menu_id == this.menuId) {
                                return menu;
                            }
                            if (menu.child_menu && menu.child_menu.length) {
                                const found = findMenu(menu.child_menu);
                                if (found) return found;
                            }
                        }
                        return null;
                    };
                } else {
                    // 递归查找第一个菜单项或者iframe
                    findMenu = (menus) => {
                        for (const menu of menus) {
                            if (menu.type == '2' || menu.type == '3') {
                                return menu;
                            }
                            if (menu.child_menu && menu.child_menu.length) {
                                const found = findMenu(menu.child_menu);
                                if (found) return found;
                            }
                        }
                        return null;
                    };
                }

                this.currentMenu = findMenu(this.menuTreeData);

                if (this.currentMenu) {
                    this.isShowLoadIcon = false;
                    // lsMainContainer.innerHTML = '';

                    if (!this.currentMenu.url || this.currentMenu.url === '') {
                        if (this.menuId) setUrl(this.menuId);
                        this.currentMenu.type = '3';
                        this.currentMenu.url = '/error/404.html';
                        return;
                    }

                    switch (this.currentMenu.type) {
                        case '1':
                            // console.log('菜单目录');
                            return;
                        case '2':
                            // console.log('菜单项');
                            this.isShowLoadIcon = true;
                            QiankunUtil.toPage(this.currentMenu.url, {
                                menu_name: this.currentMenu.menu_name
                            });
                            if (this.menuId) setUrl(this.menuId);
                            break;
                        case '3':
                            // console.log('iframe');
                            // lsMainContainer.innerHTML = `<iframe src="${this.currentMenu.url}" scrolling="yes" style="width: calc(100% - 5px);height: calc(100% - 5px);border: none;"></iframe>`;
                            if (this.menuId) setUrl(this.menuId);
                            break;
                        case '4':
                            location.href = this.currentMenu.url;
                            break;
                        case '5':
                            window.open(this.currentMenu.url, "_blank");
                            break;
                    }
                    // 设置面包屑
                    const findPath = (menus, id, path = []) => {
                        for (const item of menus) {
                            if (item.menu_id === id) {
                                return [...path, item.menu_name];
                            }
                            if (item.child_menu && item.child_menu.length) {
                                const found = findPath(item.child_menu, id, [...path, item.menu_name]);
                                if (found) return found;
                            }
                        }
                        return null;
                    };

                    this.breadcrumb = findPath(this.menuTreeData, this.currentMenu.menu_id) || [];
                    // 设置打开的父菜单
                    this.setOpenKeys();
                    // 设置选中的菜单项
                    this.selectedKeys = [this.currentMenu.menu_id];

                    if (this.enableMultiTab) {
                        this.addTab(this.currentMenu);
                    }
                }
            },
            // 设置打开的父菜单
            setOpenKeys(menuId) {
                const findParentKeys = (menus, id, parentKeys = []) => {
                    for (const menu of menus) {
                        if (menu.menu_id === id) {
                            return parentKeys;
                        }
                        if (menu.child_menu && menu.child_menu.length) {
                            const found = findParentKeys(menu.child_menu, id, [...parentKeys, menu.menu_id]);
                            if (found) return found;
                        }
                    }
                    return null;
                };

                if (menuId) {
                    this.openKeys = [...findParentKeys(this.menuTreeData, menuId), menuId] || [];
                } else {
                    this.openKeys = findParentKeys(this.menuTreeData, this.menuId) || [];
                }
            },
            // 刷新
            handleRefresh() {
                this.showMenuView();
            },
            // 退出登录
            handleLogout() {
                this.$confirm({
                    title: '提示',
                    content: '确定要退出登录吗？',
                    onOk: () => {
                        Ajax.get({
                            url: '/system/api/user/logout',
                            param: {
                                IsBackend: 1
                            },
                            success(result) {
                                if(result.IsSuccess === '1') {
                                    top.location.replace('./login');
                                }
                            }
                        });
                    },
                    onCancel: () => {},
                });
            },
            // 多页签
            // 添加页签
            addTab(menu) {
                // 检查标签是否已存在
                if (!this.tabList.find(tab => tab.key === menu.menu_id)) {
                    // 添加新标签
                    this.tabList.push({
                        key: menu.menu_id,
                        title: menu.menu_name,
                        path: menu.path
                    });
                }

                // 激活标签并导航
                this.activeTabKey = menu.menu_id;
            },
            // 关闭标签页
            closeTab(menuId, type) {
                // 不能关闭最后一个
                if (this.tabList.length <= 1) return;
                const tabs = this.tabList;

                switch (type) {
                    case 'other':
                        this.toPage(menuId);
                        this.tabList = tabs.filter(tab => tab.key === menuId);
                        break;
                    case 'left':
                        this.toPage(menuId);
                        const index = this.tabList.findIndex(item => item.key === menuId);
                        if (index === -1) break;
                        // 从数组开头删除到目标元素的前一个（共删除 index 个元素）
                        this.tabList.splice(0, index);
                        break;
                    case 'right':
                        this.toPage(menuId);
                        const index2 = this.tabList.findIndex(item => item.key === menuId);
                        if (index2 === -1) break;
                        // 从数组开头删除到目标元素的前一个（共删除 index 个元素）
                        this.tabList.splice(index2 + 1);
                        break;
                    default:
                        // 如果关闭的是当前激活的标签
                        if (this.activeTabKey === menuId) {
                            tabs.forEach((tab, index) => {
                                if (tab.key === menuId) {
                                    const nextTab = tabs[index + 1] || tabs[index - 1];
                                    if (nextTab) {
                                        this.activeTabKey = nextTab.key;
                                    }
                                }
                            });
                        }

                        this.tabList = tabs.filter(tab => tab.key !== menuId);
                        this.toPage(this.activeTabKey);
                }
            },
            // 打开多页签右键菜单
            openTabContextmenu(e, key) {
                this.showTabContextmenu = true;
                this.tabContextmenuPosition = {
                    left: e.clientX + 'px',
                    top: e.clientY + 'px',
                }
                this.activeContextmenuTabKey = key;

                // 点击其他地方关闭菜单
                document.addEventListener('click', this.closeTabContextmenu);
            },
            // 关闭多页签右键菜单
            closeTabContextmenu() {
                this.showTabContextmenu = false;
                document.removeEventListener('click', this.closeTabContextmenu);
            },
            // 标签滑动
            tabScroll(type) {
                const tabWrapper = this.$el.querySelector('.ls-multi-tab-container .tab-scroll-container');
                const scrollStep = 200; // 每次滚动的距离
                if (type === 'left') {
                    tabWrapper.scrollLeft -= scrollStep;
                } else if (type === 'right') {
                    tabWrapper.scrollLeft += scrollStep;
                }
            },
            // 全屏
            containerFullscreen(menuId) {
                if (menuId) {
                    this.toPage(menuId);
                }

                let target = document.getElementById("ls-main-container");
                if (!target) {
                    target = document.getElementById("ls-main-container-iframe");
                }
                if (!target) return;

                // 兼容不同浏览器的全屏 API
                if (target.requestFullscreen) {
                    target.requestFullscreen();
                } else if (target.webkitRequestFullscreen) { // Chrome, Safari
                    target.webkitRequestFullscreen();
                } else if (target.mozRequestFullScreen) { // Firefox
                    target.mozRequestFullScreen();
                } else if (target.msRequestFullscreen) { // IE/Edge
                    target.msRequestFullscreen();
                }
            },
        }
    });
}

//构建新的URL
function setUrl(mid) {
    if(mid) {
        urlParams.set('mid', mid);
    } else {
        urlParams.delete('mid');
    }
    let urlParamsStr = `${urlParams.toString()}${window.location.hash}`;
    let newUrl = `${window.location.pathname}${urlParamsStr?'?':''}${urlParamsStr}`;
    window.history.pushState({ path: newUrl }, '', newUrl);// 使用 history.pushState() 修改 URL 但不刷新页面
}
