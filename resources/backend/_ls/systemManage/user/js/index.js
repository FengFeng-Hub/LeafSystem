function init(props, params) {
    let _this = null,viewer = null;

    new Vue({
        el: ".ls-container",
        data: {
            pageParams: params,
            // 表格字段
            tableColumns: [{
                title: '操作', align: 'center', width: 200, fixed: 'left', scopedSlots: { customRender: 'action' }
            }, {
                title: '用户代码', dataIndex: 'user_id', align: 'center', sorter: true, width: 100, fixed: "left"
            }, {
                title: '名称', dataIndex: 'name', align: 'center', width: 120
            }, {
                title: '账号', dataIndex: 'account', width: 100
            }, {
                title: '头像', dataIndex: 'avatar', width: 70, scopedSlots: { customRender: 'avatar' }
            }, {
                title: '生日', dataIndex: 'birthday', align: 'center', sorter: true, width: 100
            }, {
                title: '性别', dataIndex: 'sex', align: 'center',sorter: true,
                filters: [{ text: '男', value: '1' }, { text: '女', value: '2' }, { text: '未知', value: '3' }],
                width: 100, scopedSlots: { customRender: 'sex' }
            }, {
                title: '角色', dataIndex: 'role_list', key: 'role_list', width: 150, scopedSlots: { customRender: 'role_list' }
            }, {
                title: '手机号', dataIndex: 'phone', sorter: true, width: 100
            }, {
                title: '电子邮箱', dataIndex: 'email', sorter: true, width: 150
            }, {
                title: '真实姓名', dataIndex: 'real_name', width: 80
            }, {
                title: '身份证号', dataIndex: 'idcard', sorter: true, width: 150
            }, {
                title: '登录IP', dataIndex: 'login_ip', sorter: true, width: 120
            }, {
                title: '登录时间', dataIndex: 'login_time', sorter: true, minWidth: 150
            }, {
                title: '禁用', dataIndex: 'is_disable', align: 'center', sorter: true, width: 70, fixed: "right", scopedSlots: { customRender: 'is_disable' }
            }],
            // 表格数据
            tableData: [],
            tableLoading: false,
            // 表格分页配置
            tablePagination: {
                pageSize: 10, // 每页显示的条数
                showSizeChanger: true, // 是否可以改变每页显示的条数
                pageSizeOptions: [1, 2, 5, 10, 30, 50, 100, 200, 300, 500, 600, 700, 800, 900, 1000], // 可选的每页显示条数
                showQuickJumper: true, // 是否可以快速跳转到指定页
                showTotal: total => `共 ${total} 条`, // 显示总条数和当前数据范围
                current: 1, // 当前页数
                total: 0 // 总条数
            },
            // 表格排序配置
            tableSort: {
                field: '',
                order: ''
            },
            // 搜索配置
            searchConfig: {
                searchType: 'name',
                searchKey: '',
                IsEqual: '0',
                role_id_arr: [],
                model: {
                    is_disable: ''
                },
                params: params.backParams?.searchParams
            },
            // 角色选择器
            roleSelect: {
                list: [],
                selectedIds: [],
                selectedItem: [],
                // 存储选中但不在options中的ID
                ghostOptions: [],
            },
            // 多选框
            // 选中的表格行key
            selectedRowKeys: [],
            selectedIds: [],
            showResetPwdModal: false,
            resetPwdUserId: '',
            resetPwdNewPassword: ''
        },
        created() {
            _this = this;
            // 加载数据
            this.loadTableList(params.backParams?params.backParams:{
                PageNo: this.tablePagination.current,
                PageCount: this.tablePagination.pageSize
            });
        },
        mounted() {},
        methods: {
            handleTableChange(pagination, filters, sorter) {
                this.loadTableList({
                    ...{
                        PageNo: pagination.current,
                        PageCount: pagination.pageSize,
                        SortField: sorter.field,
                        SortOrder: sorter.order === 'ascend' ? 'asc' : sorter.order === 'descend' ? 'desc' : ''
                    }, ...filters
                });
            },
            // 加载表格列表
            loadTableList(params) {
                this.tableLoading = true;
                Ajax.get({
                    url: '/system/api/user/getUserList',
                    param: {...params, ...this.searchConfig.params},
                    success(result) {
                        if(result.IsSuccess === '1') {
                            _this.tableData = result.data;
                            _this.selectedRowKeys = [];
                            _this.selectedIds = [];
                            _this.tablePagination.total = result.dataCount;
                            _this.tableLoading = false;

                            // 等 DOM 渲染完再初始化 Viewer
                            _this.$nextTick(() => {
                                _this.initViewer();
                            });
                        } else {
                            _this.$message.error(result.Msg);
                        }
                    }
                });
                this.$ls.table.updatePaginationAndSort.call(this ,params);
            },
            // 搜索
            search(isReset) {
                for (const key in this.searchConfig.params) {
                    delete this.searchConfig.params[key]
                }

                if (!isReset) {
                    this.searchConfig.params = Object.assign({},this.searchConfig.model);
                    this.searchConfig.params[this.searchConfig.searchType] = this.searchConfig.searchKey;
                    this.searchConfig.params.role_id_arr = this.roleSelect.selectedIds.join(',');
                    this.tablePagination.current = 1;
                }

                this.$ls.table.clearSort.call(this);
                this.loadTableList({
                    PageNo: 1,
                    PageCount: params.pageSize?params.pageSize:this.tablePagination.pageSize,
                    SortField: this.tableSort.field,
                    SortOrder: this.tableSort.order
                });
            },
            // 编辑表格
            edit(id,operate) {
                if (operate === 'Delete') {
                    Ajax.post({
                        url: '/system/api/user/updateUser?UpdateType=Delete',
                        param: {
                            user_id: id
                        },
                        success(result) {
                            if(result.IsSuccess === '1') {
                                _this.$message.success('删除成功');
                                _this.loadTableList({
                                    PageNo: _this.tablePagination.current,
                                    PageCount: _this.tablePagination.pageSize,
                                    SortField: _this.tableSort.field,
                                    SortOrder: _this.tableSort.order
                                });
                            } else {
                                _this.$message.error(result.Msg);
                            }
                        }
                    });
                    return;
                }

                QiankunUtil.toPage(props.parentUrl + 'edit.html', {
                    id: id,
                    operate: operate,
                    backParams: {
                        PageNo: this.tablePagination.current,
                        PageCount: this.tablePagination.pageSize,
                        SortField: this.tableSort.field,
                        SortOrder: this.tableSort.order,
                        searchParams: this.searchConfig.params
                    }
                });
            },
            // 修改是否禁用
            updateIsDisable(id,record) {
                record.is_disable = record.is_disable === '1'?'0':'1';

                Ajax.post({
                    url: '/system/api/user/updateUser?UpdateType=IsDisable',
                    param: {
                        user_id: id,
                        is_disable: record.is_disable
                    },
                    success(result) {
                        if(result.IsSuccess === '1') {
                            _this.$message.success('操作成功');
                        } else {
                            _this.$message.error(result.Msg);
                            _this.loadTableList({
                                PageNo: _this.tablePagination.current,
                                PageCount: _this.tablePagination.pageSize,
                                SortField: _this.tableSort.field,
                                SortOrder: _this.tableSort.order
                            });
                        }
                    }
                });
            },
            // 批量修改用户
            batchUpdateUser(updateType,value) {
                if (!this.selectedIds || this.selectedIds.length === 0) {
                    this.$message.error('请选择用户');
                    return;
                }

                let userIdArr = this.selectedIds.join(',');
                let param = {
                    UpdateType: updateType,
                    user_id_arr: userIdArr
                };

                if (updateType === 'IsDisable') {
                    param.is_disable = value;
                } else if (updateType === 'Sex') {
                    param.sex = value;
                } else if (updateType === 'Delete') {
                    this.$confirm({
                        title: '警告',
                        content: '确定删除代码为 [' + userIdArr + '] 的项？',
                        okText: '确定',
                        okType: 'danger',
                        cancelText: '取消',
                        onOk() {
                            Ajax.post({
                                url: '/system/api/user/batchUpdateUser?UpdateType=Delete',
                                param: param,
                                success(result) {
                                    if(result.IsSuccess === '1') {
                                        _this.$message.success('操作成功');
                                        _this.loadTableList({
                                            PageNo: _this.tablePagination.current,
                                            PageCount: _this.tablePagination.pageSize,
                                            SortField: _this.tableSort.field,
                                            SortOrder: _this.tableSort.order
                                        });
                                    } else {
                                        _this.$message.error(result.Msg);
                                    }
                                }
                            });
                        },
                        onCancel() {},
                    });
                    return;
                }

                Ajax.post({
                    url: '/system/api/user/batchUpdateUser?UpdateType=' + updateType,
                    param: param,
                    success(result) {
                        if(result.IsSuccess === '1') {
                            _this.$message.success('操作成功');
                            _this.loadTableList({
                                PageNo: _this.tablePagination.current,
                                PageCount: _this.tablePagination.pageSize,
                                SortField: _this.tableSort.field,
                                SortOrder: _this.tableSort.order
                            });
                        } else {
                            _this.$message.error(result.Msg);
                        }
                    }
                });
            },
            // 多选框变换事件
            onSelectChange(selectedKeys,selectedItems) {
                this.selectedRowKeys = selectedKeys;
                this.selectedIds = selectedItems.map(item => item.user_id);
            },
            // 初始化Viewer
            initViewer() {
                // 如果 viewer 已存在，销毁旧实例
                if (viewer) {
                    viewer.destroy();
                    viewer = null;
                }

                // 创建新的 Viewer 实例绑定当前页图片
                viewer = new Viewer(document.querySelector('.ant-table-tbody'), {
                    url: 'src',
                    filter(image) {
                        return image.tagName.toLowerCase() === 'img'; // 只预览 img 标签
                    }
                });

            },
            // 加载角色
            loadRoleSelect(searchKey) {
                let param = {
                    // PageNo: 1,
                    // PageCount: 2
                };

                if (searchKey) {
                    param = {
                        role_id: searchKey,
                        role_name: searchKey
                    };
                }

                Ajax.get({
                    url: '/system/api/role/getRoleList',
                    param: param,
                    success(result) {
                        if (result.IsSuccess === '1') {
                            _this.roleSelect.list = result.data;

                            // 分离出尚未加载的ID
                            // _this.roleSelect.selectedIds = _this.formData.role_list.map(item => item.role_id);
                            _this.roleSelect.ghostOptions = _this.roleSelect.selectedItem.filter(item =>
                                !result.data.some(role => role.role_id === item.role_id)
                            );
                        }
                    }
                });
            },
            roleSelectChange(selectedIds) {
                this.roleSelect.selectedIds = selectedIds;
                this.roleSelect.selectedItem = [...this.roleSelect.ghostOptions,...this.roleSelect.list]
                    .filter(role => selectedIds.some(id => role.role_id === id));
            },
            roleSelectSearch(key) {
                this.loadRoleSelect(key);
            },
            roleSelectFocus() {
                this.loadRoleSelect();
            },
            handleShowResetPwdModal(userId) {
                this.resetPwdUserId = userId;
                this.resetPwdNewPassword = '';
                this.showResetPwdModal = true;
            },
            handleResetPwd() {
                Ajax.post({
                    url: '/system/api/user/updateUser?UpdateType=ResetPwd',
                    param: {
                        user_id: _this.resetPwdUserId,
                        new_password: _this.resetPwdNewPassword
                    },
                    success(result) {
                        if (result.IsSuccess === '1') {
                            _this.$message.success('操作成功');
                            _this.showResetPwdModal = false;
                        } else {
                            _this.$message.error(result.Msg);
                        }
                    }
                });
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