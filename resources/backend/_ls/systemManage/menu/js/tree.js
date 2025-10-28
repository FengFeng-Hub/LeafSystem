function init(props, params) {
    let _this = null;

    new Vue({
        el: ".ls-container",
        data: {
            id: params.id,
            pageParams: params,
            menuTree: {
                data: [],
                // 存储当前展开的
                expandedKeys: [],
                // Tree 组件的属性
                treeProps: {
                    title: 'menu_name',
                    key: 'menu_id',
                    children: 'children',
                },
                // 右键下拉框菜单
                visibleDropdownMenuId: null
            },
            formData: {
                menu_name: '',
                parent_menu_id: '',
                parent_menu_name: '',
                menu_icon: '',
                url: '',
                type: '',
                permission_key: '',
                is_show: '1',
                sort: '',
            },
            // 表单验证
            formDataRules: {
                menu_name: [
                    { required: true, message: '此项为必填项', trigger: 'change' },
                    { max: 50, message: '长度不能超过 50 个字符', trigger: 'change' }
                ],
                url: [
                    { max: 250, message: '长度不能超过 250 个字符', trigger: 'change' }
                ],
                permission_key: [
                    { max: 100, message: '长度不能超过 50 个字符', trigger: 'change' },
                ],
                type: [
                    { required: true, message: '此项为必填项', trigger: 'change' },
                ],
                is_show: [
                    { required: true, message: '此项为必填项', trigger: 'change' },
                ]
            },
            menuSelect: {
                list: []
            }
        },
        created() {
            _this = this;
            this.loadTreeData(true);

            // 全局点击事件
            document.addEventListener('click', (e) => {
                // 如果不是右键菜单则关闭右键菜单
                if (!e.target.closest('.menu-tree-contextmenu')) {
                    _this.menuTree.visibleDropdownMenuId = null;
                }
            });
        },
        methods: {
            loadTreeData(first) {
                Ajax.get({
                    url: '/system/api/menu/getMenuList',
                    success: (result) => {
                        if (result.IsSuccess === "1") {
                            _this.menuTree.data = _this.transformMenuData(result.data);
                            // 展开全部
                            if (first) {
                                _this.expandAll(true);
                            }
                        } else {
                            this.$message.error(result.Msg);
                        }
                    },
                });
            },
            // 编辑
            edit() {
                this.$refs.form.validate(valid => {
                    const type = this.id && this.id !== ''?'Edit':'Add'
                    if (valid) {
                        let param = this.formData;

                        if (type === 'Edit') {
                            param.menu_id = this.id;
                        }

                        Ajax.post({
                            url: '/system/api/menu/updateMenu?UpdateType=' + type,
                            param: param,
                            success(result) {
                                if(result.IsSuccess === '1') {
                                    _this.$message.success('修改成功');
                                    _this.loadTreeData();
                                } else {
                                    _this.$message.error(result.Msg);
                                }
                            }
                        });
                    } else {
                        this.$message.error('表单校验失败，请检查输入');
                    }
                });
            },
            // 将接口返回的数据转换为树形结构
            transformMenuData(menuData) {
                const menuMap = {};

                // 构建每个菜单的父子关系
                menuData.forEach(menu => {
                    let icon = '';

                    switch(menu.type) {
                        case '1':
                            icon = 'appstore';
                            break;
                        case '2':
                            icon = 'file';
                            break;
                        case '3':
                            icon = 'iframe';
                            break;
                        case '4':
                        case '5':
                            icon = 'link';
                            break;
                        case '6':
                            icon = 'check-square';
                            break;
                        default:
                            icon = 'question';
                    }

                    menuMap[menu.menu_id] = { ...menu, ...{ slots: {icon: icon} }, children: [] };
                });

                // 构建树结构
                const treeData = [];
                menuData.forEach(menu => {
                    if (menu.parent_menu_id) {
                        if (menuMap[menu.parent_menu_id]) {
                            menuMap[menu.parent_menu_id].children.push(menuMap[menu.menu_id]);
                        }
                    } else {
                        treeData.push(menuMap[menu.menu_id]);
                    }
                });
                return treeData;
            },
            // 展开所有节点
            expandAll(onlyDir) {
                this.menuTree.expandedKeys = this.getAllMenuIds(this.menuTree.data, onlyDir);
            },
            // 获取所有有子节点的菜单ID，用于展开所有节点
            getAllMenuIds(menuData, onlyDir) {
                let ids = [];
                menuData.forEach(item => {
                    if (item.children && item.children.length) {
                        if (onlyDir) {
                            if (item.type === '1') {
                                ids.push(item.menu_id);
                            }
                        } else {
                            ids.push(item.menu_id);
                        }

                        ids = ids.concat(this.getAllMenuIds(item.children, onlyDir));
                    }
                });
                return ids;
            },
            // 折叠所有节点
            collapseAll() {
                this.menuTree.expandedKeys = [];
            },
            handleMenuTreeSelect(node) {
                this.id = node.menu_id;
                this.formData = {
                    menu_name: node.menu_name,
                    parent_menu_id: node.parent_menu_id,
                    parent_menu_name: node.parent_menu_name,
                    menu_icon: node.menu_icon,
                    url: node.url,
                    type: node.type,
                    permission_key: node.permission_key,
                    is_show: node.is_show,
                    sort: node.sort
                };
                this.loadMenuSelect();
            },
            handleMenuTreeAdd(node) {
                this.loadMenuSelect();
                this.id = '';
                this.formData = {
                    menu_name: '',
                    parent_menu_id: node?node.menu_id:'',
                    parent_menu_name: node?node.parent_menu_name:'',
                    menu_icon: '',
                    url: '',
                    type: '',
                    permission_key: '',
                    is_show: '1'
                };
                this.menuTree.visibleDropdownMenuId = null;
            },
            handleMenuTreeDelete(node) {
                this.$confirm({
                    title: '提示',
                    content: '确认删除菜单 ' + node.menu_name,
                    okText: '确认',
                    cancelText: '取消',
                    onOk() {
                        Ajax.post({
                            url: '/system/api/menu/updateMenu?UpdateType=Delete',
                            param: { menu_id: node.menu_id },
                            success(result) {
                                if(result.IsSuccess === '1') {
                                    _this.$message.success('删除成功');
                                    _this.loadTreeData();
                                } else {
                                    _this.$message.error(result.Msg);
                                }
                            }
                        });
                    },
                    onCancel() {}
                });
            },
            handleMenuTreeExpand(keys) {
                this.menuTree.expandedKeys = keys;
            },
            // 加载菜单
            loadMenuSelect(searchKey) {
                let param = {
                    PageNo: 1,
                    PageCount: 10
                };

                if (searchKey) {
                    param = {
                        ...param,
                        ...{
                            menu_id: searchKey,
                            menu_name: searchKey
                        }
                    };
                }

                Ajax.get({
                    url: '/system/api/menu/getMenuList',
                    param: param,
                    success(result) {
                        if (result.IsSuccess === '1') {
                            _this.menuSelect.list = result.data;

                            // 如果列表里面没有该选项
                            if (_this.formData.parent_menu_id &&
                                !result.data.some(menu => menu.menu_id === _this.formData.parent_menu_id)) {
                                _this.menuSelect.list.push({
                                    menu_id: _this.formData.parent_menu_id,
                                    menu_name: _this.formData.parent_menu_name
                                });
                            }
                        }
                    }
                });
            },
            menuSelectChange(selectedId) {
                this.formData.parent_menu_id = selectedId;
            },
            menuSelectSearch(key) {
                this.loadMenuSelect(key);
            },
            menuSelectFocus() {
                this.loadMenuSelect();
            },
            onDragEnter(info) {
                // expandedKeys 需要受控时设置
                this.menuTree.expandedKeys = info.expandedKeys
            },
            // 拖拽排序
            onDrop(info) {
                // 拖动菜单
                const dragMenu = info.dragNode;
                // 目标菜单
                const targetMenu = info.node;
                // 父级菜单代码
                let parentMenuId;
                // 新顺序
                let newSort;
                // 原顺序
                const oldSort = parseInt(dragMenu.dataRef.sort);

                // 是否是移到间隙里面
                if (info.dropToGap) {
                    parentMenuId = targetMenu.$parent.eventKey;

                    // dropPosition 拖动位置 上:-1;内:0;下:1
                    const dropPos = targetMenu.pos.split('-');
                    const dropPosition = info.dropPosition - Number(dropPos[dropPos.length - 1]);
                    newSort = parseInt(targetMenu.dataRef.sort);

                    if (dropPosition === 1) {  // 上
                        if (oldSort < newSort) {
                            newSort -= 1;
                        }
                    } else if (dropPosition === -1) {  // 下
                        if (oldSort > targetMenu.dataRef.sort) {
                            newSort += 1;
                        }
                    }
                } else {
                    parentMenuId = targetMenu.eventKey;

                    // 如果有子节点
                    if (targetMenu.dataRef.children && targetMenu.dataRef.children.length > 0) {
                        newSort = parseInt(targetMenu.dataRef.children[0].sort);

                        if (oldSort < newSort) {
                            newSort -= 1;
                        } else {
                            newSort = null;
                        }
                    }
                }

                let updateSuccess = false;

                dragMenu.dataRef.parent_menu_id = parentMenuId?parentMenuId:'';
                Ajax.post({
                    url: '/system/api/menu/updateMenu?UpdateType=Edit',
                    param: dragMenu.dataRef,
                    success(result) {
                        if(result.IsSuccess === '1') {
                            updateSuccess = true;
                        } else {
                            _this.$message.error(result.Msg);
                        }
                    }, async: false
                });

                if (newSort && newSort !== '') {
                    if (updateSuccess) {
                        Ajax.post({
                            url: '/system/api/menu/updateMenu?UpdateType=Sort',
                            param: {menu_id: dragMenu.dataRef.menu_id,new_sort: newSort},
                            success: function(result) {
                                if(result.IsSuccess === '1') {
                                    _this.loadTreeData();
                                    _this.$message.info('操作成功');
                                } else {
                                    updateSuccess = false;
                                    _this.$message.error(result.Msg);
                                }
                            }, async: false
                        });
                    }
                } else {
                    if (updateSuccess) {
                        _this.loadTreeData();
                        _this.$message.info('操作成功');
                    }
                }
            }
        }
    });
}

// 获取qiankun子应用生命周期
QiankunUtil.getLifecycles({
    mount(props) {
        init(props, props.params);
    }
});