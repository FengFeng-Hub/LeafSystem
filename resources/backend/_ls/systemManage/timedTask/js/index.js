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
                title: 'ID', dataIndex: 'timed_task_id', align: 'center', sorter: true, width: 100, fixed: "left"
            }, {
                title: '任务描述', dataIndex: 'task_desc', align: 'left', sorter: true, width: 150
            }, {
                title: '任务分组', dataIndex: 'task_group', align: 'center', sorter: true, width: 100
            }, {
                title: '函数路径', dataIndex: 'func_path', align: 'left', sorter: true, minWidth: 200
            }, {
                title: 'Cron表达式', dataIndex: 'cron_expression', align: 'center', sorter: true, width: 100
            }, {
                title: '状态', dataIndex: 'status', align: 'center', sorter: true, width: 100, scopedSlots: { customRender: 'status' }
            }, {
                title: '执行策略', dataIndex: 'misfire_policy', align: 'center', sorter: true, width: 100, scopedSlots: { customRender: 'misfire_policy' }
            }, {
                title: '并发执行', dataIndex: 'concurrent_execute', align: 'center', sorter: true, width: 100, scopedSlots: { customRender: 'concurrent_execute' }
            }, {
                title: '记录日志', dataIndex: 'is_log', align: 'center', sorter: true, width: 100, scopedSlots: { customRender: 'is_log' }
            }, {
                title: '创建时间', dataIndex: 'ls_create_time', align: 'center', width: 150
            }, {
                title: '创建者', dataIndex: 'ls_create_by', align: 'center', width: 100
            }, {
                title: '更新时间', dataIndex: 'ls_update_time', align: 'center', width: 150
            }, {
                title: '更新者', dataIndex: 'ls_update_by', align: 'center', width: 100
            }, {
                title: '', align: 'left', width: 140, fixed: 'right', scopedSlots: { customRender: 'action2' }
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
                searchType: 'task_desc',
                searchKey: '',
                IsEqual: '0',
                model: {
                    status: ''
                },
                params: params.backParams?.searchParams
            }
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
                    url: '/system/api/timedTask/getSysTimedTaskList',
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
                if (operate === 'Delete') {
                    Ajax.post({
                        url: '/system/api/timedTask/updateSysTimedTask?UpdateType=Delete',
                        param: {
                            timed_task_id: id
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
            // 执行
            operate(record, type) {
                Ajax.post({
                    url: '/system/api/timedTask/updateSysTimedTask?UpdateType=' + type,
                    param: {
                        timed_task_id: record.timed_task_id
                    },
                    success(result) {
                        if(result.IsSuccess === '1') {
                            _this.$message.success(result.data);
                            switch (type) {
                                case 'Start':
                                    record.status = '1';
                                    break;
                                case 'Pause':
                                    record.status = '2';
                                    break;
                                case 'Stop':
                                    record.status = '3';
                                    break;
                            }
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