function init(props, params) {
    let _this = null;

    new Vue({
        el: ".ls-container",
        data: {
            pageParams: params,
            // 表格字段
            tableColumns: [{
                title: '操作', align: 'center', width: 120, fixed: 'left', scopedSlots: { customRender: 'action' }
            }, , {
                title: 'ID', dataIndex: 'timed_task_log_id', align: 'center', sorter: true, width: 100, fixed: "left"
            }, {
                title: '定时任务代码', dataIndex: 'timed_task_id', align: 'center', width: 100
            }, {
                title: '任务描述', dataIndex: 'task_desc', align: 'center', width: 150
            }, {
                title: '任务分组', dataIndex: 'task_group', align: 'center', width: 100
            }, {
                title: '函数路径', dataIndex: 'func_path', align: 'left', minWidth: 200
            }, {
                title: '开始时间', dataIndex: 'start_time', align: 'center', width: 150, sorter: true
            }, {
                title: '结束时间', dataIndex: 'end_time', align: 'center', width: 150, sorter: true
            }, {
                title: '耗时（毫秒）', dataIndex: 'time', align: 'center', width: 100, sorter: true
            }, {
                title: '状态', dataIndex: 'status', align: 'center', width: 100, sorter: true, scopedSlots: { customRender: 'status' }
            }, {
                title: '类型', dataIndex: 'type', align: 'center', width: 100, sorter: true, scopedSlots: { customRender: 'type' }
            }],
            // 表格数据
            tableData: [],
            tableLoading: false,
            // 表格分页配置
            tablePagination: {
                pageSize: 10,  // 每页显示的条数
                showSizeChanger: true,  // 是否可以改变每页显示的条数
                pageSizeOptions: [1, 2, 5, 10, 30, 50, 100, 200, 300, 500, 600, 700, 800, 900, 1000], // 可选的每页显示条数
                showQuickJumper: true,  // 是否可以快速跳转到指定页
                showTotal: total => `共 ${total} 条`,  // 显示总条数和当前数据范围
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
                searchType: 'timed_task_log_id',
                searchKey: '',
                IsEqual: '0',
                model: {
                },
                params: params.backParams?.searchParams
            },
            // 多选框
            // 选中的表格行key
            selectedRowKeys: [],
            selectedIds: []
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
                    url: '/api/sysTimedTaskLog/getSysTimedTaskLogList',
                    param: {...params, ...this.searchConfig.params},
                    success(result) {
                        if(result.IsSuccess === '1') {
                            _this.tableData = result.data;
                            _this.selectedRowKeys = [];
                            _this.selectedIds = [];
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
                switch (operate) {
                    case "Delete":
                        Ajax.post({
                            url: '/api/sysTimedTaskLog/updateSysTimedTaskLog?UpdateType=Delete',
                            param: {
                                timed_task_log_id: id
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
                        break;
                    case 'BatchDelete':
                        console.log(this.selectedIds);
                        if (!this.selectedIds || this.selectedIds.length === 0) {
                            this.$message.error('请选择任务日志');
                            return;
                        }

                        let timedTaskLogIdArr = this.selectedIds.join(',');

                        this.$confirm({
                            title: '警告',
                            content: '确定删除代码为 [' + timedTaskLogIdArr + '] 的项？',
                            okText: '确定',
                            okType: 'danger',
                            cancelText: '取消',
                            onOk() {
                                Ajax.post({
                                    url: '/api/sysTimedTaskLog/updateSysTimedTaskLog?UpdateType=BatchDelete',
                                    param: {
                                        timed_task_log_id_arr: timedTaskLogIdArr
                                    },
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
                        break;
                    case 'Clear':
                        Ajax.post({
                            url: '/api/sysTimedTaskLog/updateSysTimedTaskLog?UpdateType=Clear',
                            success(result) {
                                if(result.IsSuccess === '1') {
                                    _this.$message.success('清空成功');
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
                        break;
                    default:
                        QiankunUtil.toPage(props.parentUrl + 'logEdit.html', {
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
                }
            },
            // 多选框变换事件
            onSelectChange(selectedKeys,selectedItems) {
                this.selectedRowKeys = selectedKeys;
                this.selectedIds = selectedItems.map(item => item.timed_task_log_id);
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