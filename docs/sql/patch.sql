#####    增量发布SQL   #####

## -- ++++ [v1.1.0] ===> [v1.1.1] ++++
## -- 新增： 支付测试， 重发通知， 通知最大次数保存到数据库
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST', '支付测试', 'transaction', '/paytest', 'PayTestPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '20', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_PAYWAY_LIST', '页面：获取全部支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_DO', '按钮：支付测试', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_NOTIFY_RESEND', '按钮：重发通知', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY', '0', 'MGR', now(), now());
ALTER TABLE `t_mch_notify_record` ADD COLUMN `notify_count_limit` INT(11) NOT NULL DEFAULT '6' COMMENT '最大通知次数, 默认6次' after `notify_count`;
## -- ++++ ++++

## -- ++++ [robot-config] ++++
-- 机器人设置菜单（运营平台-系统管理-系统配置 下）
insert into t_sys_entitlement values('ENT_SYS_ROBOT_CONFIG', '机器人设置', 'robot', '/robotConfig', 'RobotConfigPage', 'ML', 0, 1,  'ENT_SYS_CONFIG', '16', 'MGR', now(), now());

-- 机器人配置默认项（如已手动创建，请忽略执行错误）
INSERT INTO `t_sys_config` VALUES ('ROBOT_MANAGE_GROUP_ID', '机器人管理群ID', '机器人管理群ID', 'robotConfig', '机器人配置', '', 'text', 0, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_TECH_GROUP_ID', '四方技术群ID', '四方技术群ID', 'robotConfig', '机器人配置', '', 'text', 1, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_POINT_WARNING_AMOUNT', '点数警告(元)', '点数警告(元)', 'robotConfig', '机器人配置', '', 'text', 2, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_CHANNEL_LIMIT_WARNING_AMOUNT', '通道额度警告(元)', '通道额度警告(元)', 'robotConfig', '机器人配置', '', 'text', 3, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_WARN_EXTRA_MSG', '警告附加讯息', '警告附加讯息', 'robotConfig', '机器人配置', '', 'textarea', 4, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_PULL_ORDER_ABNORMAL_NOTICE', '机器人拉单异常通知', '机器人拉单异常通知开关', 'robotConfig', '机器人配置', '0', 'switch', 5, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_MCH_CALLBACK_ABNORMAL_NOTICE', '机器人商户回调异常通知', '机器人商户回调异常通知开关', 'robotConfig', '机器人配置', '0', 'switch', 6, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_QUERY_RETURN_WITH_IMAGE', '查单返回是否传图', '查单返回是否传图开关', 'robotConfig', '机器人配置', '0', 'switch', 7, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_RECONCILE_SHOW_DISPATCH', '机器人对账显示下发', '机器人对账显示下发开关', 'robotConfig', '机器人配置', '0', 'switch', 8, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_RECONCILE_SHOW_TOP', '机器人对账显示置顶', '机器人对账显示置顶开关', 'robotConfig', '机器人配置', '0', 'switch', 9, now());
INSERT INTO `t_sys_config` VALUES ('ROBOT_MCH_RATE_CHANGE_NOTICE', '商户费率修改通知', '商户费率修改通知开关', 'robotConfig', '机器人配置', '0', 'switch', 10, now());
## -- ++++ ++++

## -- ++++ [v1.4.0] ++++
-- 支付接口定义表 新增支付参数配置页面是否为自定义
ALTER TABLE `t_pay_interface_define` ADD COLUMN `config_page_type` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '支付参数配置页面类型:1-JSON渲染,2-自定义' after `is_isv_mode`;

-- 优化支付接口定义初始化，新增是否为脱敏数据


## -- ++++ ++++


## -- ++++ [v1.5.1] ===> [v1.6.0] ++++
## -- 新增： 转账接口

-- 转账订单表
DROP TABLE IF EXISTS t_transfer_order;
CREATE TABLE `t_transfer_order` (
                                    `transfer_id` VARCHAR(32) NOT NULL COMMENT '转账订单号',
                                    `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                    `isv_no` VARCHAR(64) COMMENT '服务商号',
                                    `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
                                    `mch_name` VARCHAR(30) NOT NULL COMMENT '商户名称',
                                    `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
                                    `mch_order_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
                                    `if_code` VARCHAR(20)  NOT NULL COMMENT '支付接口代码',
                                    `entry_type` VARCHAR(20) NOT NULL COMMENT '入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡',
                                    `amount` BIGINT(20) NOT NULL COMMENT '转账金额,单位分',
                                    `currency` VARCHAR(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
                                    `account_no` VARCHAR(64) NOT NULL COMMENT '收款账号',
                                    `account_name` VARCHAR(64) COMMENT '收款人姓名',
                                    `bank_name` VARCHAR(32) COMMENT '收款人开户行名称',
                                    `transfer_desc` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '转账备注信息',
                                    `client_ip` VARCHAR(32) DEFAULT NULL COMMENT '客户端IP',
                                    `state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '支付状态: 0-订单生成, 1-转账中, 2-转账成功, 3-转账失败, 4-订单关闭',
                                    `channel_extra` VARCHAR(512) DEFAULT NULL COMMENT '特定渠道发起额外参数',
                                    `channel_order_no` VARCHAR(64) DEFAULT NULL COMMENT '渠道订单号',
                                    `err_code` VARCHAR(128) DEFAULT NULL COMMENT '渠道支付错误码',
                                    `err_msg` VARCHAR(256) DEFAULT NULL COMMENT '渠道支付错误描述',
                                    `ext_param` VARCHAR(128) DEFAULT NULL COMMENT '商户扩展参数',
                                    `notify_url` VARCHAR(128) NOT NULL default '' COMMENT '异步通知地址',
                                    `success_time` DATETIME DEFAULT NULL COMMENT '转账成功时间',
                                    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                    PRIMARY KEY (`transfer_id`),
                                    UNIQUE KEY `Uni_MchNo_MchOrderNo` (`mch_no`, `mch_order_no`),
                                    INDEX(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账订单表';

-- 菜单项
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER', '转账订单', 'property-safety', '/transfer', 'TransferOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '25', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_LIST', '页面：转账订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER', '转账订单', 'property-safety', '/transfer', 'TransferOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_LIST', '页面：转账订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER', '转账', 'property-safety', '/doTransfer', 'MchTransferPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER_IF_CODE_LIST', '页面：获取全部代付通道', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER_CHANNEL_USER', '按钮：获取渠道用户', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER_DO', '按钮：发起转账', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());

## -- ++++ ++++

ALTER TABLE `t_mch_info`
    ADD COLUMN `account_balance` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户账户余额,单位分' AFTER `remark`,
    ADD COLUMN `payout_quota` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商户代付额度,单位分' AFTER `account_balance`;

DROP TABLE IF EXISTS `t_mch_account_change_log`;
CREATE TABLE `t_mch_account_change_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
    `account_type` TINYINT(6) NOT NULL COMMENT '账户类型: 1-账户余额,2-代付额度',
    `change_direction` TINYINT(6) NOT NULL COMMENT '变动方向: 1-增加,2-减少',
    `amount` BIGINT(20) NOT NULL COMMENT '变动金额,单位分',
    `before_amount` BIGINT(20) NOT NULL COMMENT '变动前金额,单位分',
    `after_amount` BIGINT(20) NOT NULL COMMENT '变动后金额,单位分',
    `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作员ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作员姓名',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_mch_created` (`mch_no`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户账户变动记录表';

## -- ++++ [v1.6.0] ===> [v1.7.0] ++++

-- 订单页的支付方式筛选项添加权限并可分配： 避免API权限导致页面出现异常
insert into t_sys_entitlement values('ENT_PAY_ORDER_SEARCH_PAY_WAY', '筛选项：支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_PAY_ORDER_SEARCH_PAY_WAY', '筛选项：支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());


-- 插入表结构，并插入默认数据（默认费率 0）
alter table `t_pay_order` add column `mch_fee_rate` decimal(20,6) NOT NULL COMMENT '商户手续费费率快照' after `amount`;
alter table `t_pay_order` add column `mch_fee_amount` BIGINT(20) NOT NULL COMMENT '商户手续费,单位分' after `mch_fee_rate`;
update `t_pay_order` set mch_fee_rate = 0;
update `t_pay_order` set mch_fee_amount = 0;

alter table `t_pay_order` drop column `division_flag`;
alter table `t_pay_order` drop column `division_time`;

alter table `t_pay_order` add column `division_mode` TINYINT(6) DEFAULT 0 COMMENT '订单分账模式：0-该笔订单不允许分账, 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额)' after `refund_amount`;
alter table `t_pay_order` add column `division_state` TINYINT(6) DEFAULT 0 COMMENT '订单分账状态：0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)' after `division_mode`;
alter table `t_pay_order` add column `division_last_time` DATETIME COMMENT '最新分账时间' after `division_state`;


-- 商户分账接收者账号组
DROP TABLE IF EXISTS `t_mch_division_receiver_group`;
CREATE TABLE `t_mch_division_receiver_group` (
                                                 `receiver_group_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '组ID',
                                                 `receiver_group_name` VARCHAR(64) NOT NULL COMMENT '组名称',
                                                 `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                                 `auto_division_flag` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '自动分账组（当订单分账模式为自动分账，改组将完成分账逻辑） 0-否 1-是',
                                                 `created_uid` BIGINT(20) NOT NULL COMMENT '创建者用户ID',
                                                 `created_by` VARCHAR(64) NOT NULL COMMENT '创建者姓名',
                                                 `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                                 `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                                 PRIMARY KEY (`receiver_group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8mb4 COMMENT='分账账号组';

-- 商户分账接收者账号绑定关系表
DROP TABLE IF EXISTS `t_mch_division_receiver`;
CREATE TABLE `t_mch_division_receiver` (
                                           `receiver_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分账接收者ID',
                                           `receiver_alias` VARCHAR(64) NOT NULL COMMENT '接收者账号别名',
                                           `receiver_group_id` BIGINT(20) COMMENT '组ID（便于商户接口使用）',
                                           `receiver_group_name` VARCHAR(64) COMMENT '组名称',
                                           `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                           `isv_no` VARCHAR(64) COMMENT '服务商号',
                                           `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
                                           `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口代码',
                                           `acc_type` TINYINT(6) NOT NULL COMMENT '分账接收账号类型: 0-个人(对私) 1-商户(对公)',
                                           `acc_no` VARCHAR(50) NOT NULL COMMENT '分账接收账号',
                                           `acc_name` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '分账接收账号名称',
                                           `relation_type` VARCHAR(30) NOT NULL COMMENT '分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等',
                                           `relation_type_name` VARCHAR(30) NOT NULL COMMENT '当选择自定义时，需要录入该字段。 否则为对应的名称',
                                           `division_profit` DECIMAL(20,6) COMMENT '分账比例',
                                           `state` TINYINT(6) NOT NULL COMMENT '分账状态（本系统状态，并不调用上游关联关系）: 1-正常分账, 0-暂停分账',
                                           `channel_bind_result` TEXT COMMENT '上游绑定返回信息，一般用作查询账号异常时的记录',
                                           `channel_ext_info` TEXT COMMENT '渠道特殊信息',
                                           `bind_success_time` DATETIME DEFAULT NULL COMMENT '绑定成功时间',
                                           `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                           `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                           PRIMARY KEY (`receiver_id`)
) ENGINE=InnoDB AUTO_INCREMENT=800001 DEFAULT CHARSET=utf8mb4 COMMENT='商户分账接收者账号绑定关系表';

-- 分账记录表
DROP TABLE IF EXISTS `t_pay_order_division_record`;
CREATE TABLE `t_pay_order_division_record` (
                                               `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分账记录ID',
                                               `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                               `isv_no` VARCHAR(64) COMMENT '服务商号',
                                               `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
                                               `mch_name` VARCHAR(30) NOT NULL COMMENT '商户名称',
                                               `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
                                               `if_code` VARCHAR(20)  NOT NULL COMMENT '支付接口代码',
                                               `pay_order_id` VARCHAR(30) NOT NULL COMMENT '系统支付订单号',
                                               `pay_order_channel_order_no` VARCHAR(64) COMMENT '支付订单渠道支付订单号',
                                               `pay_order_amount` BIGINT(20) NOT NULL COMMENT '订单金额,单位分',
                                               `pay_order_division_amount` BIGINT(20) NOT NULL COMMENT '订单实际分账金额, 单位：分（订单金额 - 商户手续费 - 已退款金额）',
                                               `batch_order_id` VARCHAR(30) NOT NULL COMMENT '系统分账批次号',
                                               `channel_batch_order_id` VARCHAR(64) COMMENT '上游分账批次号',
                                               `state` TINYINT(6) NOT NULL COMMENT '状态: 0-待分账 1-分账成功, 2-分账失败',
                                               `channel_resp_result` TEXT COMMENT '上游返回数据包',
                                               `receiver_id` BIGINT(20) NOT NULL COMMENT '账号快照》 分账接收者ID',
                                               `receiver_group_id` BIGINT(20) COMMENT '账号快照》 组ID（便于商户接口使用）',
                                               `receiver_alias` VARCHAR(64) COMMENT '接收者账号别名',
                                               `acc_type` TINYINT(6) NOT NULL COMMENT '账号快照》 分账接收账号类型: 0-个人 1-商户',
                                               `acc_no` VARCHAR(50) NOT NULL COMMENT '账号快照》 分账接收账号',
                                               `acc_name` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '账号快照》 分账接收账号名称',
                                               `relation_type` VARCHAR(30) NOT NULL COMMENT '账号快照》 分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等',
                                               `relation_type_name` VARCHAR(30) NOT NULL COMMENT '账号快照》 当选择自定义时，需要录入该字段。 否则为对应的名称',
                                               `division_profit` DECIMAL(20,6) NOT NULL COMMENT '账号快照》 配置的实际分账比例',
                                               `cal_division_amount` BIGINT(20) NOT NULL COMMENT '计算该接收方的分账金额,单位分',
                                               `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                               `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                               PRIMARY KEY (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='分账记录表';

-- 权限表扩容
alter table `t_sys_entitlement` modify column `ent_id` VARCHAR(64) NOT NULL COMMENT '权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD';

-- 【商户系统】 分账管理
insert into t_sys_entitlement values('ENT_DIVISION', '分账管理', 'apartment', '', 'RouteView', 'ML', 0, 1,  'ROOT', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP', '账号组管理', 'team', '/divisionReceiverGroup', 'DivisionReceiverGroupPage', 'ML', 0, 1,  'ENT_DIVISION', '10', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_LIST', '页面：数据列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_DELETE', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER', '收款账号管理', 'trademark', '/divisionReceiver', 'DivisionReceiverPage', 'ML', 0, 1,  'ENT_DIVISION', '20', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_LIST', '页面：数据列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_ADD', '按钮：新增收款账号', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_DELETE', '按钮：删除收款账号', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_EDIT', '按钮：修改账号信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECORD', '分账记录', 'unordered-list', '/divisionRecord', 'DivisionRecordPage', 'ML', 0, 1,  'ENT_DIVISION', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECORD_LIST', '页面：数据列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECORD', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECORD_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECORD', '0', 'MCH', now(), now());

## -- ++++ ++++

## -- ++++ [v1.7.0] ===> [v1.8.0] ++++
-- 添加商户系统的退款功能权限配置项
insert into t_sys_entitlement values('ENT_PAY_ORDER_REFUND', '按钮：订单退款', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());

## -- ++++ [v1.8.0] ===> [v1.9.0] ++++

## -- ++++ [v1.9.0] ===> [v1.10.0] ++++
alter table t_refund_order modify err_msg varchar(2048) null comment '渠道错误描述';

-- 增加角色权限字段长度
alter table `t_sys_role_ent_rela` MODIFY `ent_id` VARCHAR(64) NOT NULL COMMENT '权限ID' after `role_id`;

## -- ++++ [v1.10.0] ===> [v1.11.0] ++++

## -- ++++ [v1.11.0] ===> [v1.12.0] ++++
-- 分账重试
insert into t_sys_entitlement values('ENT_DIVISION_RECORD_RESEND', '按钮：重试', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECORD', '0', 'MCH', now(), now());

## -- ++++ [v1.12.0] ===> [v1.13.0] ++++
DELETE FROM t_pay_interface_define WHERE if_code = 'wxpay';
INSERT INTO t_pay_interface_define (if_code, if_name, is_isv_mode, config_page_type, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('wxpay', '微信支付官方', 1, 2,
        '[{"name":"mchId", "desc":"微信支付商户号", "type": "text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"appSecret","desc":"应用AppSecret","type":"text","verify":"required","star":"1"},{"name":"oauth2Url", "desc":"oauth2地址（置空将使用官方）", "type": "text"},{"name":"apiVersion", "desc":"微信支付API版本", "type": "radio","values":"V2,V3","titles":"V2,V3","verify":"required"},{"name":"key", "desc":"APIv2密钥", "type": "textarea","verify":"required","star":"1"},{"name":"apiV3Key", "desc":"APIv3密钥（V3接口必填）", "type": "textarea","verify":"","star":"1"},{"name":"serialNo", "desc":"序列号（V3接口必填）", "type": "textarea","verify":"","star":"1" },{"name":"cert", "desc":"API证书(apiclient_cert.p12)", "type": "file","verify":""},{"name":"apiClientCert", "desc":"证书文件(apiclient_cert.pem) ", "type": "file","verify":""},{"name":"apiClientKey", "desc":"私钥文件(apiclient_key.pem)", "type": "file","verify":""}]',
        '[{"wayCode": "WX_APP"}, {"wayCode": "WX_H5"}, {"wayCode": "WX_NATIVE"}, {"wayCode": "WX_JSAPI"}, {"wayCode": "WX_BAR"}, {"wayCode": "WX_LITE"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/wxpay.png', '#04BE02', 1, '微信官方通道');

## -- ++++ [v1.13.0] ===> [v1.14.0] ++++
-- 日志请求参数、响应参数长度修改
alter table t_sys_log modify `opt_req_param` TEXT DEFAULT NULL COMMENT '操作请求参数';
alter table t_sys_log modify `opt_res_info` TEXT DEFAULT NULL COMMENT '操作响应结果';

## -- ++++ [v1.14.0] ===> [v1.15.0] ++++

## -- ++++ [v1.15.0] ===>

-- 增加银联支付
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_APP', '银联App支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_WAP', '银联手机网站支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_QR', '银联二维码(主扫)');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_BAR', '银联二维码(被扫)');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_B2B', '银联企业网银支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_PC', '银联网关支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_JSAPI', '银联Js支付');


## -- ++++ [v2.1.0] ===>

-- 分账状态新增： 已受理
alter table t_pay_order_division_record modify column `state` TINYINT(6) NOT NULL COMMENT '状态: 0-待分账 1-分账成功（明确成功）, 2-分账失败（明确失败）, 3-分账已受理（上游受理）';

##
-- 支付方式新增支付宝订单码


## -- ++++ [v3.0.0] ===> [v3.1.0]  ====

-- 增加转账渠道响应数据字段
alter table t_transfer_order add column `channel_res_data` TEXT DEFAULT NULL COMMENT '渠道响应数据（如微信确认数据包）' after `channel_order_no`;


## -- ++++ [v3.1.0] ===> NEXT
