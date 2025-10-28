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
                title: 'ID', dataIndex: 'student_id', align: 'center', sorter: true, width: 100, fixed: "left"
            }, {
                title: '姓名', dataIndex: 'name', align: 'center',
            }, {
                title: '用户代码', dataIndex: 'user_id', align: 'right',
            }, {
                title: '学号', dataIndex: 'stuNo', align: 'right', sorter: true
            }, {
                title: '班级', dataIndex: 'class', align: 'left', sorter: true
            }, {
                title: '性别', dataIndex: 'sex', align: 'left', sorter: true
            }, {
                title: '毕业', dataIndex: 'is_graduate', align: 'center', sorter: true
            }, {
                title: '生日', dataIndex: 'birthday', align: 'center', sorter: true
            }, {
                title: '创建时间', dataIndex: 'create_time', align: 'center', sorter: true
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
                searchType: 'student_id',
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
                    url: '/api/testStudent/getTestStudentList',
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
                        url: '/api/testStudent/updateTestStudent?UpdateType=Delete',
                        param: {
                            student_id: id
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
            // 多选框变换事件
            onSelectChange(selectedKeys,selectedItems) {
                this.selectedRowKeys = selectedKeys;
                this.selectedIds = selectedItems.map(item => item.student_id);
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