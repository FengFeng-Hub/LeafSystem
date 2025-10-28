function init(props, params) {
    let _this = null;

    new Vue({
        el: ".ls-container",
        data: {
            pageParams: params,
            // 表格字段
            tableColumns: [{
                title: '操作', align: 'center', width: 120, fixed: 'left', scopedSlots: { customRender: 'action' }
            }, {
                title: '菜单代码', dataIndex: 'menu_id', align: 'center', sorter: true, width: 100, fixed: "left"
            }, {
                title: '菜单名称', dataIndex: 'menu_name', align: 'center', width: 150, scopedSlots: { customRender: 'menu_name' }
            }, {
                title: '父级菜单代码', dataIndex: 'parent_menu_id', align: 'center', sorter: true, width: 100
            }, {
                title: '父级菜单名称', dataIndex: 'parent_menu_name', align: 'center', width: 150
            }, {
                title: '类型', dataIndex: 'type', align: 'center', sorter: true, width: 100, scopedSlots: { customRender: 'type' }
            }, {
                title: '显示', dataIndex: 'is_show', align: 'center', sorter: true, width: 70, scopedSlots: { customRender: 'is_show' }
            }, {
                title: '排序', dataIndex: 'sort', align: 'center', sorter: true, width: 180, scopedSlots: { customRender: 'sort' }
                // width: 150
            }, {
                title: '菜单图标', dataIndex: 'menu_icon', align: 'center', width: 50, scopedSlots: { customRender: 'menu_icon' }
            }, {
                title: 'url', dataIndex: 'url', width: 100, align: 'center', scopedSlots: { customRender: 'url' }
            }, {
                title: '创建时间', dataIndex: 'ls_create_time', width: 150, align: 'center'
            }, {
                title: '创建者', dataIndex: 'ls_create_by', width: 100, align: 'center'
            }, {
                title: '更新时间', dataIndex: 'ls_update_time', width: 150, align: 'center'
            }, {
                title: '更新者', dataIndex: 'ls_update_by', width: 100, align: 'center'
            }, {
                title: '权限标识', dataIndex: 'permission_key', minWidth: 200
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
                searchType: 'menu_name',
                searchKey: '',
                IsEqual: '0',
                model: {
                    is_show: '',
                    type_arr: []
                },
                params: params.backParams?.searchParams
            },
            // 权限键列表
            permissionKeyList: []
        },
        created() {
            // this.edit(null,'Tree');
            // this.edit('4','Edit');
            _this = this;
            // 加载数据
            this.loadTableList(params.backParams?params.backParams:{
                PageNo: this.tablePagination.current,
                PageCount: this.tablePagination.pageSize
            });
            // 加载权限键列表
            Ajax.get({});
        },
        mounted() {
            // 初始化拖拽排序
            new Sortable(document.querySelector('.ls-container .ant-table-tbody'), {
                handle: '.ls-table-handle-drag',
                animation: 150,
                onEnd: this.handleSortEnd
            });
        },
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
                    url: '/system/api/menu/getMenuList',
                    param: {...params, ...this.searchConfig.params},
                    success(result) {
                        if(result.IsSuccess === '1') {
                            // 强制刷新表格
                            _this.tableData = []; // 先清空
                            _this.$nextTick(() => {
                                _this.tableData = result.data; // 使用全新的数组
                            });
                            // _this.tableData = result.data;
                            _this.tablePagination.total = result.dataCount;
                            _this.tableLoading = false;
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
                    this.searchConfig.params.type_arr = this.searchConfig.model.type_arr.join(','),
                    this.tablePagination.current = 1;
                } else {
                    this.searchConfig.model.type_arr = [];
                }

                this.$ls.table.clearSort.call(this);
                this.loadTableList({
                    PageNo: 1,
                    PageCount: this.tablePagination.pageSize,
                    SortField: this.tableSort.field,
                    SortOrder: this.tableSort.order
                });
            },
            // 编辑表格
            edit(id,operate) {
                if (operate === 'Delete') {
                    Ajax.post({
                        url: '/system/api/menu/updateMenu?UpdateType=Delete',
                        param: {
                            menu_id: id
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
            // 修改是否显示
            updateIsShow(id,record) {
                record.is_show = record.is_show === '1'?'0':'1';

                Ajax.post({
                    url: '/system/api/menu/updateMenu?UpdateType=IsShow',
                    param: {
                        menu_id: id,
                        is_show: record.is_show
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
            // 菜单类型变换事件
            handleMenuTypeChange(keys) {
                this.searchConfig.model.type_arr = keys;
                this.search(false);
            },
            handleChangIsShowSort(e) {
                if(e) {
                    if(this.tableColumns.filter((col) => col.key === 'SortAction').length === 0) {
                        this.tableColumns.unshift({
                            title: '排序', key: 'SortAction', align: 'center', width: 80, scopedSlots: { customRender: 'SortAction' }
                        });

                        delete this.tableColumns[1].fixed;
                        delete this.tableColumns[2].fixed;
                    }
                } else {
                    this.tableColumns = this.tableColumns.filter(col => col.key !== 'SortAction');
                    this.tableColumns[0].fixed = 'left';
                    this.tableColumns[1].fixed = 'left';
                }

                console.log(this.tableColumns);
            },
            // 排序
            sort(index, type) {
                if (this.tableSort.field) {
                    this.$confirm({
                        title: '提示',
                        content: '当前有字段属于排序状态，是否重置排序？',
                        okText: '确认',
                        cancelText: '取消',
                        onOk() {
                            _this.search(true);
                        }
                    });
                    return;
                }
                let newSort = null;

                switch (type) {
                    case 'up':
                        if (index === 0) {
                            if (this.tablePagination.current === 1) {
                                _this.$message.info('这已经是第一条数据');
                                return;
                            }
                            // 请求上一页最后一条数据
                            Ajax.get({
                                url: '/system/api/menu/getMenuList',
                                param: {
                                    PageNo: _this.tablePagination.current - 1,
                                    PageCount: _this.tablePagination.pageSize
                                },
                                success(result) {
                                    newSort = result.data?result.data[result.data.length - 1].sort:null;
                                }, async: false
                            });
                        } else {
                            newSort = this.tableData[index - 1].sort;
                        }
                        break;
                    case 'down':
                        if (index === this.tableData.length - 1) {
                            if (this.tablePagination.current === Math.ceil(this.tablePagination.total / this.tablePagination.pageSize)) {
                                _this.$message.info('这已经是最后一条数据');
                                return;
                            }
                            // 请求下一页第一条数据
                            Ajax.get({
                                url: '/system/api/menu/getMenuList',
                                param: {
                                    PageNo: _this.tablePagination.current + 1,
                                    PageCount: _this.tablePagination.pageSize
                                },
                                success(result) {
                                    newSort = result.data?result.data[0].sort:null;
                                }, async: false
                            });
                        } else {
                            newSort = this.tableData[index + 1].sort;
                        }
                        break;
                    case 'top':
                        if (index !== 0) {
                            newSort = this.tableData[0].sort;
                        }
                        break;
                    case 'bottom':
                        if (index !== this.tableData.length - 1) {
                            newSort = this.tableData[this.tableData.length - 1].sort;
                        }
                        break;
                }
                this.updateMenuSortApi(this.tableData[index].menu_id, newSort);
            },
            handleSortEnd(evt) {
                if (this.tableSort.field) {
                    this.$info({
                        title: '提示',
                        content: '当前有字段属于排序状态，将会重置排序？',
                        okText: '确认',
                        onOk() {
                            _this.search(true);
                        }
                    });
                    return;
                }
                const { newIndex, oldIndex } = evt;
                this.updateMenuSortApi(this.tableData[oldIndex].menu_id, this.tableData[newIndex].sort);
            },
            updateMenuSortApi(menuId, newSort) {
                if (menuId && newSort) {
                    Ajax.post({
                        url: '/system/api/menu/updateMenu?UpdateType=Sort',
                        param: {menu_id: menuId,new_sort: newSort},
                        success: function(result) {
                            _this.loadTableList({
                                PageNo: _this.tablePagination.current,
                                PageCount: _this.tablePagination.pageSize,
                            });

                            if(result.IsSuccess === '1') {
                                _this.$message.info('修改成功');
                            } else {
                                _this.$message.error(result.Msg);
                            }
                        }
                    });
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