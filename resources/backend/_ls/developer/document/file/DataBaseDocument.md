# 系统模块数据库文档

[TOC]

TODO:

在项目中优化mysql的时候字段尽量不要用NULL 值，使用-1或者其他特殊标识来替代，原因如下：

NULL的列会使用更多的存储空间，在Mysql中也需要特殊处理
对Mysql来说更难优化，因为可为NULL的列会使索引统计和值比较都更复杂。
当可为NULL的列被索引时，每个索引记录需要一个额外的字节，在MyISAM里甚至还可能导致固定大小的索引（例如只有一个整数列的索引）变成可变大小的索引。

#### 系统字段

| **字段名称**   | **中文名称** | **数据类型** | **null** | **默认** | **备注** |
| -------------- | ------------ | ------------ | -------- | -------- | -------- |
| ls_create_time | 创建时间     | datetime     |          |          |          |
| ls_create_by   | 创建者       | bigint       |          |          |          |
| ls_update_time | 更新时间     | datetime     |          |          |          |
| ls_update_by   | 更新者       | bigint       |          |          |          |

### 系统模块 sys

#### sys_menu 菜单表

| **字段名称**   | **中文名称** | **数据类型** | **null** | **默认** | **备注**                                              |
| -------------- | ------------ | ------------ | -------- | -------- | ----------------------------------------------------- |
| menu_id        | 菜单代码     | int          | n        |          | 主键                                                  |
| menu_name      | 菜单名称     | varchar(100) | n        |          |                                                       |
| parent_menu_id | 父级菜单代码 | int          |          |          | 外键:sys_menu                                         |
| menu_icon      | 菜单图标     | varchar(100) |          |          |                                                       |
| icon_type      | 图标类型     | tinyint(4)   |          |          |                                                       |
| url            | url          | varchar(500) |          |          | url                                                   |
| type           | 类型         | tinyint(4)   | n        | 2        | 1:菜单目录;2:菜单项;3:iframe;4:此页面;5:新页面;6:控件 |
| permission_key | 权限键       | varchar(200) |          |          | 用于接口绑定按钮                                      |
| is_show        | 是否显示     | tinyint(4)   | n        | 0        |                                                       |
| sort           | 排序         | int          |          |          |                                                       |
| memo           | 备注         | varchar(500) |          |          |                                                       |
| 系统字段       |              |              |          |          |                                                       |

#### sys_user 用户表

| **字段名称**       | **中文名称**     | **数据类型** | **null** | **默认** | **备注**         |
| ------------------ | ---------------- | ------------ | -------- | -------- | ---------------- |
| user_id            | 用户代码         | int          | n        |          | 主键             |
| name               | 名称             | varchar(100) | n        |          |                  |
| account            | 账号             | varchar(100) | n        |          | 唯一索性         |
| password           | 密码             | varchar(100) |          |          | md5加密          |
| personal_signature | 个性签名         | varchar(500) |          |          |                  |
| avatar             | 头像             | varchar(500) |          |          |                  |
| birthday           | 生日             | data         |          |          |                  |
| phone              | 手机号           | varchar(100) |          |          |                  |
| email              | 电子邮箱         | varchar(350) |          |          |                  |
| real_name          | 真实姓名         | varchar(100) |          |          |                  |
| idcard             | 身份证号         | varchar(50)  |          |          |                  |
| sex                | 性别             | tinyint(4)   | n        | 3        | 1:男;2:女;3:未知 |
| is_disable         | 是否禁用         | tinyint(4)   |          |          |                  |
| login_ip           | 登录IP           | varchar(100) |          |          |                  |
| login_time         | 登录时间         | datetime     |          |          |                  |
| pwd_update_date    | 密码最后更新时间 | datetime     |          |          |                  |

#### sys_user_role_rel 用户角色关系表

| **字段名称** | **中文名称** | **数据类型** | **null** | **默认** | **备注**      |
| ------------ | ------------ | ------------ | -------- | -------- | ------------- |
| user_id      | 用户代码     | int          | n        |          | 外键:sys_user |
| role_id      | 角色代码     | int          | n        |          | 外键:sys_role |

> user_id、role_id组合唯一索引

#### sys_role 角色表

| **字段名称**           | **中文名称**     | **数据类型** | **null** | **默认** | **备注** |
| ---------------------- | ---------------- | ------------ | -------- | -------- | -------- |
| role_id                | 角色代码         | int          | n        |          | 主键     |
| role_name              | 角色名称         | varchar(100) | n        |          |          |
| is_allow_login_backend | 是否允许登录后台 | tinyint(4)   |          |          |          |
| is_disable             | 是否禁用         | tinyint(4)   |          |          |          |
| 系统字段               |                  |              |          |          |          |

#### sys_role_menu_rel 角色菜单关系表

| **字段名称** | **中文名称** | **数据类型** | **null** | **默认** | **备注**      |
| ------------ | ------------ | ------------ | -------- | -------- | ------------- |
| role_id      | 角色代码     | int          | n        |          | 外键:sys_role |
| menu_id      | 菜单代码     | int          | n        |          | 外键:sys_menu |

> role_id、menu_id组合唯一索引

#### sys_config 配置表

| **字段名称** | **中文名称** | **数据类型**  | **null** | **默认** | **备注**                                      |
| ------------ | ------------ | ------------- | -------- | -------- | --------------------------------------------- |
| config_id    | 配置代码     | int           | n        |          | 主键                                          |
| config_key   | 配置键       | varchar(50)   | n        |          | 唯一索性                                      |
| config_value | 配置值       | varchar(5000) |          |          |                                               |
| config_desc  | 配置描述     | varchar(300)  |          |          |                                               |
| config_type  | 配置类型     | tinyint(4)    | n        |          | 1:文本;2:图片;3:文件;4:开关;5:日期;6:日期时间 |
| access_level | 访问等级     | tinyint(4)    | n        | 1        | 1:不可访问;2:后台用户;3:前台用户;4:公开;      |
| memo         | 备注         | varchar(500)  |          |          |                                               |
| 系统字段     |              |               |          |          |                                               |

#### sys_config_role_rel 配置角色关系表（弃用）

| **字段名称** | **中文名称** | **数据类型** | **null** | **默认** | **备注**        |
| ------------ | ------------ | ------------ | -------- | -------- | --------------- |
| config_id    | 配置代码     | int          | n        |          | 外键:sys_config |
| role_id      | 角色代码     | int          | n        |          | 外键:sys_role   |

> config_id、role_id组合唯一索引

####   sys_timed_task 定时任务表

| **字段名称**       | **中文名称** | **数据类型** | **null** | **默认** | **备注**                                 |
| ------------------ | ------------ | ------------ | -------- | -------- | ---------------------------------------- |
| timed_task_id      | 定时任务代码 | int          | n        |          | 主键                                     |
| task_desc          | 任务描述     | varchar(200) | n        |          |                                          |
| task_group         | 任务分组     | varchar(200) | n        |          |                                          |
| func_path          | 函数路径     | varchar(200) | n        |          |                                          |
| cron_expression    | Cron表达式   | varchar(100) | n        |          |                                          |
| status             | 状态         | tinyint(4)   | n        | 3        | 1:启动;2:暂停;3:终止;                    |
| misfire_policy     | 执行策略     | tinyint(4)   | n        | 4        | 1:默认;2:立即执行;3:执行一次;4:放弃执行; |
| concurrent_execute | 并发执行     | tinyint(4)   | n        | 0        | 0:否;1:是;                               |
| is_log             | 是否记录日志 | tinyint(4)   | n        | 1        |                                          |
| memo               | 备注         | varchar(500) |          |          |                                          |
| 系统字段           |              |              |          |          |                                          |

#### sys_timed_task_log 定时任务日志表

| **字段名称**      | **中文名称**     | **数据类型** | **null** | **默认** | **备注**               |
| ----------------- | ---------------- | ------------ | -------- | -------- | ---------------------- |
| timed_task_log_id | 定时任务日志代码 | int          | n        |          | 主键                   |
| timed_task_id     | 定时任务代码     | int          | n        |          |                        |
| func_path         | 函数路径         | varchar(200) | n        |          |                        |
| start_time        | 开始时间         | datetime     | n        |          |                        |
| end_time          | 结束时间         | datetime     | n        |          |                        |
| time              | 耗时（毫秒）     | bigint       | n        |          |                        |
| status            | 状态             | tinyint(4)   | n        | 2        | 1:成功;2:失败;         |
| type              | 类型             | tinyint(4)   | n        |          | 1:自动触发;2:手动触发; |
| result            | 结果             | text         |          |          |                        |