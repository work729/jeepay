#####  表结构及初始化数据SQL  #####
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户资金流水表';

DROP TABLE IF EXISTS `t_mch_account_daily_snapshot`;
CREATE TABLE `t_mch_account_daily_snapshot` (
        `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `snapshot_amount` BIGINT(20) NOT NULL COMMENT '当日账户余额快照,单位分',
        `snapshot_date` DATE NOT NULL COMMENT '快照日期(每天0:00更新)',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        PRIMARY KEY (`id`),
        UNIQUE KEY `Uni_mch_date` (`mch_no`,`snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户账户每日快照表';
--   RBAC设计思路：  [用户] 1<->N [角色] 1<->N [权限]

-- 权限表
DROP TABLE IF EXISTS `t_sys_entitlement`;
CREATE TABLE `t_sys_entitlement` (
  `ent_id` VARCHAR(64) NOT NULL COMMENT '权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD',
  `ent_name` VARCHAR(32) NOT NULL COMMENT '权限名称',
  `menu_icon` VARCHAR(32) COMMENT '菜单图标',
  `menu_uri` VARCHAR(128) COMMENT '菜单uri/路由地址',
  `component_name` VARCHAR(32) COMMENT '组件Name（前后端分离使用）',
  `ent_type` CHAR(2) NOT NULL COMMENT '权限类型 ML-左侧显示菜单, MO-其他菜单, PB-页面/按钮',
  `quick_jump` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '快速开始菜单 0-否, 1-是',
  `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '状态 0-停用, 1-启用',
  `pid` VARCHAR(32) NOT NULL COMMENT '父ID',
  `ent_sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序字段, 规则：正序',
  `sys_type` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`ent_id`, `sys_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 角色表
DROP TABLE IF EXISTS `t_sys_role`;
CREATE TABLE `t_sys_role` (
  `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID, ROLE_开头',
  `role_name` VARCHAR(32) NOT NULL COMMENT '角色名称',
  `sys_type` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `belong_info_id` VARCHAR(64) NOT NULL DEFAULT '0' COMMENT '所属商户ID / 0(平台)',
  `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 角色<->权限 关联表
DROP TABLE IF EXISTS `t_sys_role_ent_rela`;
CREATE TABLE `t_sys_role_ent_rela` (
  `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID',
  `ent_id` VARCHAR(64) NOT NULL COMMENT '权限ID' ,
  PRIMARY KEY (`role_id`, `ent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色权限关联表';

-- 系统用户表
DROP TABLE IF EXISTS `t_sys_user`;
CREATE TABLE `t_sys_user` (
	`sys_user_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '系统用户ID',
    `login_username` VARCHAR(32) NOT NULL COMMENT '登录用户名',
	`realname` VARCHAR(32) NOT NULL COMMENT '真实姓名',
	`telphone` VARCHAR(32) NOT NULL COMMENT '手机号',
	`sex` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '性别 0-未知, 1-男, 2-女',
	`avatar_url` VARCHAR(128) COMMENT '头像地址',
    `user_no` VARCHAR(32) COMMENT '员工编号',
    `is_admin` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '是否超管（超管拥有全部权限） 0-否 1-是',
    `state` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '状态 0-停用 1-启用',
    `sys_type` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
    `belong_info_id` VARCHAR(64) NOT NULL DEFAULT '0' COMMENT '所属商户ID / 0(平台)',
    `google_auth_secret` VARCHAR(128) COMMENT '谷歌验证密钥（加密存储）',
    `google_auth_enabled` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '谷歌验证开启标识 0-否 1-是',
	`created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
	PRIMARY KEY (`sys_user_id`),
    UNIQUE KEY(`sys_type`,`login_username`),
    UNIQUE KEY(`sys_type`,`telphone`),
    UNIQUE KEY(`sys_type`, `user_no`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 系统用户认证表
DROP TABLE IF EXISTS `t_sys_user_auth`;
CREATE TABLE `t_sys_user_auth` (
	`auth_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
	`user_id` BIGINT(20) NOT NULL COMMENT 'user_id',
	`identity_type` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '登录类型  1-登录账号 2-手机号 3-邮箱  10-微信  11-QQ 12-支付宝 13-微博',
	`identifier` VARCHAR(128) NOT NULL COMMENT '认证标识 ( 用户名 | open_id )',
	`credential` VARCHAR(128) NOT NULL COMMENT '密码凭证',
	`salt` VARCHAR(128) NOT NULL COMMENT 'salt',
    `sys_type` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
	PRIMARY KEY (`auth_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户认证表';

-- 操作员<->角色 关联表
DROP TABLE IF EXISTS `t_sys_user_role_rela`;
CREATE TABLE `t_sys_user_role_rela` (
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `role_id`VARCHAR(32) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作员<->角色 关联表';


-- 系统配置表
DROP TABLE IF EXISTS `t_sys_config`;
CREATE TABLE `t_sys_config` (
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置KEY',
    `config_name` VARCHAR(50) NOT NULL COMMENT '配置名称',
    `config_desc` VARCHAR(200) NOT NULL COMMENT '描述信息',
    `group_key` VARCHAR(50) NOT NULL COMMENT '分组key',
    `group_name` VARCHAR(50) NOT NULL COMMENT '分组名称',
    `config_val` TEXT NOT NULL COMMENT '配置内容项',
    `type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '类型: text-输入框, textarea-多行文本, uploadImg-上传图片, switch-开关',
    `sort_num` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 系统操作日志表
DROP TABLE IF EXISTS `t_sys_log`;
CREATE TABLE `t_sys_log` (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '系统用户ID',
  `user_name` varchar(32) DEFAULT NULL COMMENT '用户姓名',
  `user_ip` varchar(128) NOT NULL DEFAULT '' COMMENT '用户IP',
  `sys_type` varchar(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `method_name` varchar(128) NOT NULL DEFAULT '' COMMENT '方法名',
  `method_remark` varchar(128) NOT NULL DEFAULT '' COMMENT '方法描述',
  `req_url` varchar(256) NOT NULL DEFAULT '' COMMENT '请求地址',
  `opt_req_param` TEXT DEFAULT NULL COMMENT '操作请求参数',
  `opt_res_info` TEXT DEFAULT NULL COMMENT '操作响应结果',
  `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`sys_log_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '系统操作日志表';

-- 商户信息表
DROP TABLE IF EXISTS t_mch_info;
CREATE TABLE `t_mch_info` (
        `id` int NOT NULL AUTO_INCREMENT,
        `agent_id` int NOT NULL COMMENT '所属代理商',
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `mch_name` VARCHAR(64) NOT NULL COMMENT '商户名称',
        `recharge_rate` VARCHAR(64) NOT NULL COMMENT '线下充值费率(%)',
        `telegram_group` VARCHAR(64) NOT NULL COMMENT '飞机群ID',
        `telegram_contact` VARCHAR(64) NOT NULL COMMENT '飞机群联络人',
        `mch_short_name` VARCHAR(32) NOT NULL COMMENT '商户简称',
        `type` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
        `isv_no` VARCHAR(64) COMMENT '服务商号',
        `contact_name` VARCHAR(32) COMMENT '联系人姓名',
        `contact_tel` VARCHAR(32) COMMENT '联系人手机号',
        `contact_email` VARCHAR(32) COMMENT '联系人邮箱',
        `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '商户状态: 0-停用, 1-正常',
        `remark` VARCHAR(128) COMMENT '商户备注',
        `mch_secret` VARCHAR(128) DEFAULT NULL COMMENT '商户密钥',
        `pay_password` VARCHAR(100) COMMENT '支付密码哈希',
        `login_security_type` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '登录安全类型: 0-仅密码, 1-密码+谷歌',
        `pay_security_type` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '支付安全类型: 0-无需验证, 1-仅支付密码, 2-仅谷歌',
        `login_ip_whitelist` TEXT COMMENT '登录IP白名单，逗号分隔',
        `login_ip_blacklist` TEXT COMMENT '登录IP黑名单，逗号分隔',
        `pay_ip_whitelist` TEXT COMMENT '支付IP白名单，逗号分隔',
        `pay_ip_blacklist` TEXT COMMENT '支付IP黑名单，逗号分隔',
        `account_balance` DECIMAL NOT NULL DEFAULT 0 COMMENT '商户账户余额,单位分',
        `payout_quota` DECIMAL NOT NULL DEFAULT 0 COMMENT '商户代付额度,单位分',
        `init_user_id` BIGINT(20) DEFAULT NULL COMMENT '初始用户ID（创建商户时，允许商户登录的用户）',
        `created_uid` BIGINT(20) COMMENT '创建者用户ID',
        `created_by` VARCHAR(64) COMMENT '创建者姓名',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户信息表';

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

DROP TABLE IF EXISTS `t_mch_fund_flow`;
CREATE TABLE `t_mch_fund_flow` (
        `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `before_amount` BIGINT(20) NOT NULL COMMENT '变更前金额,单位分',
        `change_amount` BIGINT(20) NOT NULL COMMENT '变更金额,单位分',
        `after_amount` BIGINT(20) NOT NULL COMMENT '变更后金额,单位分',
        `biz_type` TINYINT(6) DEFAULT NULL COMMENT '业务类型: 1-支付入账,2-退款出账,3-人工增加,4-人工减少',
        `biz_order_id` VARCHAR(64) DEFAULT NULL COMMENT '业务订单ID',
        `biz_order_amount` BIGINT(20) DEFAULT NULL COMMENT '业务订单金额,单位分',
        `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作员ID',
        `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作员姓名',
        `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        PRIMARY KEY (`id`),
        KEY `idx_mch_created` (`mch_no`,`created_at`),
        KEY `idx_biz_type` (`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户资金流水表';
DROP TABLE IF EXISTS t_agent_info;
CREATE TABLE `t_agent_info` (
        `id` int NOT NULL AUTO_INCREMENT COMMENT '代理ID',
        `agent_name` VARCHAR(64) NOT NULL COMMENT '代理名称',
        `contact_name` VARCHAR(32) DEFAULT NULL COMMENT '联系人姓名',
        `contact_tel` VARCHAR(32) DEFAULT NULL COMMENT '联系人手机号',
        `contact_email` VARCHAR(64) DEFAULT NULL COMMENT '联系人邮箱',
        `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
        `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
        `created_uid` BIGINT(20) DEFAULT NULL COMMENT '创建者用户ID',
        `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者姓名',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商信息表';

-- 服务商信息表
DROP TABLE IF EXISTS t_isv_info;
CREATE TABLE `t_isv_info` (
        `isv_no` VARCHAR(64) NOT NULL COMMENT '服务商号',
        `isv_name` VARCHAR(64) NOT NULL COMMENT '服务商名称',
        `isv_short_name` VARCHAR(32) NOT NULL COMMENT '服务商简称',
        `contact_name` VARCHAR(32) COMMENT '联系人姓名',
        `contact_tel` VARCHAR(32) COMMENT '联系人手机号',
        `contact_email` VARCHAR(32) COMMENT '联系人邮箱',
        `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
        `remark` VARCHAR(128) DEFAULT NULL COMMENT '备注',
        `created_uid` BIGINT(20) COMMENT '创建者用户ID',
        `created_by` VARCHAR(64) COMMENT '创建者姓名',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
        PRIMARY KEY (`isv_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务商信息表';

-- 支付方式表  pay_way
DROP TABLE IF EXISTS t_pay_way;
CREATE TABLE `t_pay_way` (
        `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式代码  例如： wxpay_jsapi',
        `way_name` VARCHAR(20) NOT NULL COMMENT '支付方式名称',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
        PRIMARY KEY (`way_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付方式表';

-- 支付接口定义表
DROP TABLE IF EXISTS t_pay_interface_define;
CREATE TABLE `t_pay_interface_define` (
          `if_code` VARCHAR(20) NOT NULL COMMENT '接口代码 全小写  wxpay alipay ',
          `if_name` VARCHAR(20) NOT NULL COMMENT '接口名称',
          `config_page_type` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '支付参数配置页面类型:1-JSON渲染,2-自定义',
          `normal_mch_params` VARCHAR(4096) DEFAULT NULL COMMENT '普通商户接口配置定义描述,json字符串',
          `way_codes` JSON NOT NULL COMMENT '支持的支付方式 ["wxpay_jsapi", "wxpay_bar"]',
          `icon` VARCHAR(256) DEFAULT NULL COMMENT '页面展示：卡片-图标',
          `bg_color` VARCHAR(20) DEFAULT NULL COMMENT '页面展示：卡片-背景色',
          `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
          `remark` VARCHAR(128) DEFAULT NULL COMMENT '备注',
          `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
          `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
          PRIMARY KEY (`if_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付接口定义表';

-- 支付接口配置参数表
DROP TABLE IF EXISTS t_pay_interface_config;
CREATE TABLE `t_pay_interface_config` (
          `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
          `info_type` TINYINT(6) NOT NULL COMMENT '账号类型:1-服务商 2-商户 3-商户应用',
          `info_id` VARCHAR(64) NOT NULL COMMENT '服务商号/商户号/应用ID',
          `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口代码',
          `if_params` VARCHAR(4096) NOT NULL COMMENT '接口配置参数,json字符串',
          `if_rate` DECIMAL(20,6) DEFAULT NULL COMMENT '支付接口费率',
          `state` TINYINT(6) NOT NULL default 1 COMMENT '状态: 0-停用, 1-启用',
          `remark` VARCHAR(128) DEFAULT NULL COMMENT '备注',
          `created_uid` BIGINT(20) COMMENT '创建者用户ID',
          `created_by` VARCHAR(64) COMMENT '创建者姓名',
          `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
          `updated_uid` BIGINT(20) COMMENT '更新者用户ID',
          `updated_by` VARCHAR(64) COMMENT '更新者姓名',
          `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
          PRIMARY KEY (`id`),
          UNIQUE KEY `Uni_InfoType_InfoId_IfCode` (`info_type`, `info_id`, `if_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付接口配置参数表';


-- 商户支付通道表 (允许商户  支付方式 对应多个支付接口的配置)
DROP TABLE IF EXISTS t_mch_pay_passage;
CREATE TABLE `t_mch_pay_passage` (
         `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
         `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
         `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
         `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口',
         `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式',
         `rate` DECIMAL(20,6) NOT NULL COMMENT '支付方式费率',
         `risk_config` JSON DEFAULT NULL COMMENT '风控数据',
         `state` TINYINT(6) NOT NULL COMMENT '状态: 0-停用, 1-启用',
         `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
         `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
         PRIMARY KEY (`id`),
         UNIQUE KEY `Uni_AppId_WayCode` (`app_id`,`if_code`, `way_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户支付通道表';


-- 轮询表
-- mch_no, way_code, 轮询策略。


-- 支付订单表
DROP TABLE IF EXISTS t_pay_order;
CREATE TABLE `t_pay_order` (
        `pay_order_id` VARCHAR(30) NOT NULL COMMENT '支付订单号',
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `isv_no` VARCHAR(64) DEFAULT NULL COMMENT '服务商号',
        `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
        `mch_name` VARCHAR(30) NOT NULL COMMENT '商户名称',
        `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
        `mch_order_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
        `if_code` VARCHAR(20) COMMENT '支付接口代码',
        `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式代码',
        `amount` BIGINT(20) NOT NULL COMMENT '支付金额,单位分',
        `product_id` BIGINT(20) DEFAULT NULL COMMENT '产品ID',
        `channel_id` BIGINT(20) DEFAULT NULL COMMENT '通道ID',
        `channel_provider_id` VARCHAR(64) DEFAULT NULL COMMENT '通道商ID（t_pay_interface_define.if_code）',
        `channel_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '产品名称',
        `channel_if_code` varchar(64) NOT NULL DEFAULT '' AFTER `channel_name`,
        `channel_sign` varchar(64) NOT NULL DEFAULT '' AFTER `channel_if_code`,
        `channel_fee_rate` decimal(20,6) DEFAULT NULL COMMENT '通道费率（百分比）',
        `mch_fee_rate` decimal(20,6) NOT NULL COMMENT '商户手续费费率快照',
        `mch_fee_amount` BIGINT(20) NOT NULL COMMENT '商户手续费,单位分',
        `currency` VARCHAR(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
        `state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭',
        `notify_state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '向下游回调状态, 0-未发送,  1-已发送',
        `client_ip` VARCHAR(32) DEFAULT NULL COMMENT '客户端IP',
        `subject` VARCHAR(64) NOT NULL COMMENT '商品标题',
        `body` VARCHAR(256) NOT NULL COMMENT '商品描述信息',
        `channel_extra` VARCHAR(512) DEFAULT NULL COMMENT '特定渠道发起额外参数',
        `channel_user` VARCHAR(64) DEFAULT NULL COMMENT '渠道用户标识,如微信openId,支付宝账号',
        `channel_order_no` VARCHAR(64) DEFAULT NULL COMMENT '渠道订单号',
        `refund_state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '退款状态: 0-未发生实际退款, 1-部分退款, 2-全额退款',
        `refund_times` INT NOT NULL DEFAULT 0 COMMENT '退款次数',
        `refund_amount` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '退款总金额,单位分',
        `err_code` VARCHAR(128) DEFAULT NULL COMMENT '渠道支付错误码',
        `err_msg` VARCHAR(256) DEFAULT NULL COMMENT '渠道支付错误描述',
        `ext_param` VARCHAR(128) DEFAULT NULL COMMENT '商户扩展参数',
        `notify_url` VARCHAR(128) NOT NULL default '' COMMENT '异步通知地址',
        `return_url` VARCHAR(128) DEFAULT '' COMMENT '页面跳转地址',
        `expired_time` DATETIME DEFAULT NULL COMMENT '订单失效时间',
        `success_time` DATETIME DEFAULT NULL COMMENT '订单支付成功时间',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
        PRIMARY KEY (`pay_order_id`),
        UNIQUE KEY `Uni_MchNo_MchOrderNo` (`mch_no`, `mch_order_no`),
        INDEX(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';


-- 商户通知记录表
DROP TABLE IF EXISTS t_mch_notify_record;
CREATE TABLE `t_mch_notify_record` (
        `notify_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商户通知记录ID',
        `order_id` VARCHAR(64) NOT NULL COMMENT '订单ID',
        `order_type` TINYINT(6) NOT NULL COMMENT '订单类型:1-支付,2-退款',
        `mch_order_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `isv_no` VARCHAR(64) COMMENT '服务商号',
        `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
        `notify_url` TEXT NOT NULL COMMENT '通知地址',
        `res_result` TEXT DEFAULT NULL COMMENT '通知响应结果',
        `notify_count` INT(11) NOT NULL DEFAULT '0' COMMENT '通知次数',
        `notify_count_limit` INT(11) NOT NULL DEFAULT '6' COMMENT '最大通知次数, 默认6次',
        `state` TINYINT(6) NOT NULL DEFAULT '1' COMMENT '通知状态,1-通知中,2-通知成功,3-通知失败',
        `last_notify_time` DATETIME DEFAULT NULL COMMENT '最后一次通知时间',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
        PRIMARY KEY (`notify_id`),
        UNIQUE KEY `Uni_OrderId_Type` (`order_id`, `order_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='商户通知记录表';


-- 订单接口数据快照（加密存储）
DROP TABLE IF EXISTS `t_order_snapshot`;
CREATE TABLE `t_order_snapshot` (
        `order_id` VARCHAR(64) NOT NULL COMMENT '订单ID',
        `order_type` TINYINT(6) NOT NULL COMMENT '订单类型: 1-支付, 2-退款',
        `mch_req_data` TEXT DEFAULT NULL COMMENT '下游请求数据',
        `mch_req_time` DATETIME DEFAULT NULL COMMENT '下游请求时间',
        `mch_resp_data` TEXT DEFAULT NULL COMMENT '向下游响应数据',
        `mch_resp_time` DATETIME DEFAULT NULL COMMENT '向下游响应时间',
        `channel_req_data` TEXT DEFAULT NULL COMMENT '向上游请求数据',
        `channel_req_time` DATETIME DEFAULT NULL COMMENT '向上游请求时间',
        `channel_resp_data` TEXT DEFAULT NULL COMMENT '上游响应数据',
        `channel_resp_time` DATETIME DEFAULT NULL COMMENT '上游响应时间',
        `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
        `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
        PRIMARY KEY (`order_id`, `order_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单接口数据快照';


-- 退款订单表
DROP TABLE IF EXISTS t_refund_order;
CREATE TABLE `t_refund_order` (
          `refund_order_id` VARCHAR(30) NOT NULL COMMENT '退款订单号（支付系统生成订单号）',
          `pay_order_id` VARCHAR(30) NOT NULL COMMENT '支付订单号（与t_pay_order对应）',
          `channel_pay_order_no` VARCHAR(64) DEFAULT NULL COMMENT '渠道支付单号（与t_pay_order channel_order_no对应）',
          `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
          `isv_no` VARCHAR(64) COMMENT '服务商号',
          `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
          `mch_name` VARCHAR(30) NOT NULL COMMENT '商户名称',
          `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
          `mch_refund_no` VARCHAR(64) NOT NULL COMMENT '商户退款单号（商户系统的订单号）',
          `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式代码',
          `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口代码',
          `pay_amount` BIGINT(20) NOT NULL COMMENT '支付金额,单位分',
          `refund_amount` BIGINT(20) NOT NULL COMMENT '退款金额,单位分',
          `currency` VARCHAR(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
          `state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭',
          `client_ip` VARCHAR(32) DEFAULT NULL COMMENT '客户端IP',
          `refund_reason` VARCHAR(256) NOT NULL COMMENT '退款原因',
          `channel_order_no` VARCHAR(32) DEFAULT NULL COMMENT '渠道订单号',
          `err_code` VARCHAR(128) DEFAULT NULL COMMENT '渠道错误码',
          `err_msg` VARCHAR(2048) DEFAULT NULL COMMENT '渠道错误描述',
          `channel_extra` VARCHAR(512) DEFAULT NULL COMMENT '特定渠道发起时额外参数',
          `notify_url` VARCHAR(128) DEFAULT NULL COMMENT '通知地址',
          `ext_param` VARCHAR(64) DEFAULT NULL COMMENT '扩展参数',
          `success_time` DATETIME DEFAULT NULL COMMENT '订单退款成功时间',
          `expired_time` DATETIME DEFAULT NULL COMMENT '退款失效时间（失效后系统更改为退款任务关闭状态）',
          `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
          `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
          PRIMARY KEY (`refund_order_id`),
          UNIQUE KEY `Uni_MchNo_MchRefundNo` (`mch_no`, `mch_refund_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款订单表';


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
           `channel_res_data` TEXT DEFAULT NULL COMMENT '渠道响应数据（如微信确认数据包）',
           `err_code` VARCHAR(128) DEFAULT NULL COMMENT '渠道支付错误码',
           `err_msg` VARCHAR(256) DEFAULT NULL COMMENT '渠道支付错误描述',
           `ext_param` VARCHAR(128) DEFAULT NULL COMMENT '商户扩展参数',
           `notify_url` VARCHAR(128) NOT NULL default '' COMMENT '异步通知地址',
           `success_time` DATETIME DEFAULT NULL COMMENT '转账成功时间',
           `created_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
           `updated_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
           PRIMARY KEY (`transfer_id`),
           UNIQUE KEY `Uni_MchNo_MchOrderNo` (`mch_no`, `mch_order_no`),
           INDEX(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账订单表';


CREATE TABLE `t_pay_channel`
(
    `id`                  int          NOT NULL AUTO_INCREMENT,
    `channel_sign`        varchar(64)  NOT NULL COMMENT '通道标识',
    `channel_name`        varchar(128) NOT NULL COMMENT '通道名称',
    `if_code`             varchar(64)  NOT NULL COMMENT '接口代码',
    `channel_rate`        decimal  NOT NULL COMMENT '通道费率%',
    `state`               tinyint(1) NOT NULL DEFAULT 1 COMMENT '通道状态: 0-停用, 1-启用',
    `is_float`            tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否浮动: 0-否, 1-是',
    `remark`              varchar(255) DEFAULT NULL COMMENT '备注信息',
    `weight`              int          DEFAULT 0 COMMENT '轮询权重',
    `account_name`        varchar(128) DEFAULT NULL COMMENT '账户名称',
    `channel_mch_id`      varchar(128) DEFAULT NULL COMMENT '渠道商户ID',
    `channel_sign_config` text         DEFAULT NULL COMMENT '通道标识配置JSON',
    `created_at`          datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付通道表';



#####  ↑↑↑↑↑↑↑↑↑↑  表结构DDL  ↑↑↑↑↑↑↑↑↑↑  #####

#####  ↓↓↓↓↓↓↓↓↓↓  初始化DML  ↓↓↓↓↓↓↓↓↓↓  #####

-- 权限表数据 （ 不包含根目录 ）
insert into t_sys_entitlement values('ENT_COMMONS', '系统通用菜单', 'no-icon', '', 'RouteView', 'MO', 0, 1,  'ROOT', '-1', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_USERINFO', '个人中心', 'no-icon', '/current/userinfo', 'CurrentUserInfo', 'MO', 0, 1,  'ENT_COMMONS', '-1', 'MGR', now(), now());

insert into t_sys_entitlement values('ENT_C_MAIN', '主页', 'home', '/main', 'MainPage', 'ML', 0, 1,  'ROOT', '1', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_PAY_AMOUNT_WEEK', '主页周支付统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_NUMBER_COUNT', '主页数量总统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_PAY_COUNT', '主页交易统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_PAY_TYPE_COUNT', '主页交易方式统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());

-- 商户管理
insert into t_sys_entitlement values('ENT_MCH', '商户管理', 'shop', '', 'RouteView', 'ML', 0, 1,  'ROOT', '30', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_INFO', '商户列表', 'profile', '/mch', 'MchListPage', 'ML', 0, 1,  'ENT_MCH', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_LIST', '页面：商户列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_CONFIG', '应用配置', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_FINANCE', '资金流水', 'profile', '/mch/fund', 'MchFinancePage', 'ML', 0, 1,  'ENT_MCH', '15', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_FINANCE_LIST', '页面：资金流水列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_FINANCE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_FINANCE_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_FINANCE', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_ACCOUNT', '商户账户', 'profile', '/mch/account', 'MchAccountPage', 'ML', 0, 1,  'ENT_MCH', '9', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_ACCOUNT_LIST', '页面：商户账户列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_ACCOUNT', '0', 'MGR', now(), now());

    -- 应用管理
    insert into t_sys_entitlement values('ENT_MCH_APP', '应用列表', 'appstore', '/apps', 'MchAppPage', 'ML', 0, 1,  'ENT_MCH', '20', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_LIST', '页面：应用列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_LIST', '应用支付参数配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_ADD', '应用支付参数配置', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_VIEW', '应用支付参数配置详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_LIST', '应用支付通道配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_CONFIG', '应用支付通道配置入口', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_ADD', '应用支付通道配置保存', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MGR', now(), now());

-- 代理管理
insert into t_sys_entitlement values('ENT_AGENT', '代理商管理', 'block', '', 'RouteView', 'ML', 0, 1,  'ROOT', '40', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_AGENT_INFO', '所有代理商', 'profile', '/agent', 'AgentListPage', 'ML', 0, 1,  'ENT_AGENT', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_AGENT_LIST', '页面：代理商列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_AGENT_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_AGENT_INFO_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_AGENT_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_AGENT_INFO_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1,  'ENT_AGENT_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_AGENT_INFO_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_AGENT_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_AGENT_INFO_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_AGENT_INFO', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_AGENT_FINANCE', '资金流水', 'profile', '/agentFinance', 'AgentFinancePage', 'ML', 0, 1,  'ENT_AGENT_FINANCE', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_AGENT_FINANCE_LIST', '页面：资金流水', 'no-icon', '', '', 'PB', 0, 1,  'ENT_AGENT_FINANCE_INFO', '0', 'MGR', now(), now());
-- 服务商管理
/*insert into t_sys_entitlement values('ENT_ISV', '服务商管理', 'block', '', 'RouteView', 'ML', 0, 1,  'ROOT', '40', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_ISV_INFO', '服务商列表', 'profile', '/isv', 'IsvListPage', 'ML', 0, 1,  'ENT_ISV', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_LIST', '页面：服务商列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_PAY_CONFIG_LIST', '服务商支付参数配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_PAY_CONFIG_ADD', '服务商支付参数配置', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_PAY_CONFIG_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_PAY_CONFIG_VIEW', '服务商支付参数配置详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_PAY_CONFIG_LIST', '0', 'MGR', now(), now());*/

-- 订单管理
insert into t_sys_entitlement values('ENT_ORDER', '订单管理', 'transaction', '', 'RouteView', 'ML', 0, 1,  'ROOT', '50', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_PAY_ORDER', '支付订单', 'account-book', '/pay', 'PayOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ORDER_LIST', '页面：订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_REFUND', '按钮：订单退款', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_SEARCH_PAY_WAY', '筛选项：支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_REFUND_ORDER', '退款订单', 'exception', '/refund', 'RefundOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '20', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_LIST', '页面：退款订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_TRANSFER_ORDER', '转账订单', 'property-safety', '/transfer', 'TransferOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '25', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_LIST', '页面：转账订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_NOTIFY', '回调记录', 'notification', '/notify', 'MchNotifyListPage', 'ML', 0, 1,  'ENT_ORDER', '30', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_NOTIFY_LIST', '页面：商户通知列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_NOTIFY_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_NOTIFY_RESEND', '按钮：重发通知', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY', '0', 'MGR', now(), now());

-- 支付配置菜单
insert into t_sys_entitlement values('ENT_PC', '支付配置', 'file-done', '', 'RouteView', 'ML', 0, 1,  'ROOT', '60', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_PC_IF_DEFINE', '通道标识', 'interaction', '/ifdefines', 'IfDefinePage', 'ML', 0, 1,  'ENT_PC', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_LIST', '页面：支付接口定义列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_PC_IF_CHANNEL', '支付通道', 'interaction', '/channel', 'ChannelPage', 'ML', 0, 1,  'ENT_PC', '11', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_CHANNEL_LIST', '页面：支付通道定义列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_CHANNEL', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_PC_PRODUCT', '支付产品', 'interaction', '/product', 'ProductPage', 'ML', 0, 1,  'ENT_PC', '12', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_CHANNEL_LIST', '页面：支付产品列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_PRODUCT', '0', 'MGR', now(), now());
    /*insert into t_sys_entitlement values('ENT_PC_WAY', '支付方式', 'appstore', '/payways', 'PayWayPage', 'ML', 0, 1,  'ENT_PC', '20', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_LIST', '页面：支付方式列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());*/

insert into t_sys_entitlement values('ENT_RECONCILE', '对账管理', 'audit', '', 'RouteView', 'ML', 0, 1,  'ROOT', '60', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_RECONCILE_MCH', '商户对账', 'profile', '/reconcile/mch', 'MchReconcilePage', 'ML', 0, 1,  'ENT_RECONCILE', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_RECONCILE_MCH_LIST', '页面：商户日对账列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_RECONCILE_MCH', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_RECONCILE_MCH_VIEW', '按钮：查看产品对账详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_RECONCILE_MCH', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_RECONCILE_CHANNEL', '渠道对账', 'profile', '/reconcile/channel', 'ChannelReconcilePage', 'ML', 0, 1,  'ENT_RECONCILE', '20', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_RECONCILE_CHANNEL_LIST', '页面：渠道日对账列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_RECONCILE_CHANNEL', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_RECONCILE_CHANNEL_VIEW', '按钮：查看通道详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_RECONCILE_CHANNEL', '0', 'MGR', now(), now());

-- 系统管理
insert into t_sys_entitlement values('ENT_SYS_CONFIG', '系统管理', 'setting', '', 'RouteView', 'ML', 0, 1,  'ROOT', '200', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_UR', '用户角色管理', 'team', '', 'RouteView', 'ML', 0, 1,  'ENT_SYS_CONFIG', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_UR_USER', '操作员管理', 'contacts', '/users', 'SysUserPage', 'ML', 0, 1,  'ENT_UR', '10', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_LIST', '页面：操作员列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_SEARCH', '按钮：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_ADD', '按钮：添加操作员', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_VIEW', '按钮： 详情', '', 'no-icon', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_DELETE', '按钮： 删除操作员', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_UPD_ROLE', '按钮： 角色分配', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());

        insert into t_sys_entitlement values('ENT_UR_ROLE', '角色管理', 'user', '/roles', 'RolePage', 'ML', 0, 1,  'ENT_UR', '20', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_LIST', '页面：角色列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ADD', '按钮：添加角色', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DIST', '按钮： 分配权限', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DEL', '按钮： 删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());

        insert into t_sys_entitlement values('ENT_UR_ROLE_ENT', '权限管理', 'apartment', '/ents', 'EntPage', 'ML', 0, 1,  'ENT_UR', '30', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ENT_LIST', '页面： 权限列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE_ENT', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ENT_EDIT', '按钮： 权限变更', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE_ENT', '0', 'MGR', now(), now());

    insert into t_sys_entitlement values('ENT_SYS_CONFIG_INFO', '系统配置', 'setting', '/config', 'SysConfigPage', 'ML', 0, 1,  'ENT_SYS_CONFIG', '15', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_SYS_CONFIG_EDIT', '按钮： 修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_CONFIG_INFO', '0', 'MGR', now(), now());

    insert into t_sys_entitlement values('ENT_SYS_LOG', '系统日志', 'file-text', '/log', 'SysLogPage', 'ML', 0, 1,  'ENT_SYS_CONFIG', '20', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_LOG_LIST', '页面：系统日志列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_LOG', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_SYS_LOG_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_LOG', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_SYS_LOG_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_LOG', '0', 'MGR', now(), now());


-- 【商户系统】 主页
insert into t_sys_entitlement values('ENT_COMMONS', '系统通用菜单', 'no-icon', '', 'RouteView', 'MO', 0, 1,  'ROOT', '-1', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_C_USERINFO', '个人中心', 'no-icon', '/current/userinfo', 'CurrentUserInfo', 'MO', 0, 1,  'ENT_COMMONS', '-1', 'MCH', now(), now());

insert into t_sys_entitlement values('ENT_MCH_MAIN', '主页', 'home', '/main', 'MainPage', 'ML', 0, 1,  'ROOT', '1', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_PAY_AMOUNT_WEEK', '主页周支付统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_NUMBER_COUNT', '主页数量总统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_PAY_COUNT', '主页交易统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_PAY_TYPE_COUNT', '主页交易方式统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_USER_INFO', '主页用户信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());

-- 【商户系统】 商户中心
insert into t_sys_entitlement values('ENT_MCH_CENTER', '商户中心', 'team', '', 'RouteView', 'ML', 0, 1, 'ROOT', '10', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_BASIC_INFO', '基本信息', 'idcard', '/mch/basic-info', 'MchBasicInfo', 'ML', 0, 1,  'ENT_MCH_CENTER', '10', 'MCH', now(), now());
    insert into t_sys_entitlement values ('ENT_MCH_PAY_PRODUCT_LIST', '支付通道', 'apartment', '/payProducts', 'MchPayProductPage', 'ML', 0, 1, 'ENT_MCH_CENTER', '40', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_SECURITY_CENTER', '安全中心', 'property-safety', '/security', 'SecurityCenter', 'ML', 0, 1,  'ENT_MCH_CENTER', '50', 'MCH', now(), now());
    /*insert into t_sys_entitlement values('ENT_MCH_APP', '应用管理', 'appstore', '/apps', 'MchAppPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '10', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_LIST', '页面：应用列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_APP_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_LIST', '应用支付参数配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_ADD', '应用支付参数配置', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_VIEW', '应用支付参数配置详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_LIST', '应用支付通道配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_APP', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_CONFIG', '应用支付通道配置入口', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_ADD', '应用支付通道配置保存', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MCH', now(), now());*/

    insert into t_sys_entitlement values('ENT_MCH_PAY_TEST', '支付测试', 'transaction', '/paytest', 'PayTestPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '20', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_PAYWAY_LIST', '页面：获取全部支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_DO', '按钮：支付测试', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());

    insert into t_sys_entitlement values('ENT_MCH_TRANSFER', '转账', 'property-safety', '/doTransfer', 'MchTransferPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '30', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_TRANSFER_IF_CODE_LIST', '页面：获取全部代付通道', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_TRANSFER_CHANNEL_USER', '按钮：获取渠道用户', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_TRANSFER_DO', '按钮：发起转账', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());

    insert into t_sys_entitlement values('ENT_MCH_FINANCE', '资金流水', 'profile', '/mch/fund', 'MchFinancePage', 'ML', 0, 1,  'ENT_MCH_CENTER', '25', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_FINANCE_LIST', '页面：资金流水列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_FINANCE', '0', 'MCH', now(), now());
-- 【商户系统】 订单管理
insert into t_sys_entitlement values('ENT_ORDER', '订单中心', 'transaction', '', 'RouteView', 'ML', 0, 1,  'ROOT', '20', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_PAY_ORDER', '订单管理', 'account-book', '/pay', 'PayOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '10', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_ORDER_LIST', '页面：订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_SEARCH_PAY_WAY', '筛选项：支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_REFUND', '按钮：订单退款', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_REFUND_ORDER', '退款记录', 'exception', '/refund', 'RefundOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '20', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_LIST', '页面：退款订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_TRANSFER_ORDER', '转账订单', 'property-safety', '/transfer', 'TransferOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '30', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_LIST', '页面：转账订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MCH', now(), now());

-- 【商户系统】 系统管理
insert into t_sys_entitlement values('ENT_SYS_CONFIG', '系统管理', 'setting', '', 'RouteView', 'ML', 0, 1,  'ROOT', '200', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_UR', '用户角色管理', 'team', '', 'RouteView', 'ML', 0, 1,  'ENT_SYS_CONFIG', '10', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_UR_USER', '操作员管理', 'contacts', '/users', 'SysUserPage', 'ML', 0, 1,  'ENT_UR', '10', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_LIST', '页面：操作员列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_SEARCH', '按钮：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_ADD', '按钮：添加操作员', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_VIEW', '按钮： 详情', '', 'no-icon', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_DELETE', '按钮： 删除操作员', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_UPD_ROLE', '按钮： 角色分配', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());

        insert into t_sys_entitlement values('ENT_UR_ROLE', '角色管理', 'user', '/roles', 'RolePage', 'ML', 0, 1,  'ENT_UR', '20', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_LIST', '页面：角色列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ADD', '按钮：添加角色', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DIST', '按钮： 分配权限', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_EDIT', '按钮： 修改名称', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DEL', '按钮： 删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());

-- 默认角色
insert into t_sys_role values ('ROLE_ADMIN', '系统管理员', 'MGR', '0', '2021-05-01');
insert into t_sys_role values ('ROLE_OP', '普通操作员', 'MGR', '0', '2021-05-01');
-- 角色权限关联， [超管]用户 拥有所有权限
-- insert into t_sys_role_ent_rela select '801', ent_id from t_sys_entitlement;

-- 超管用户： jeepay / jeepay123
insert into t_sys_user values (801, 'jeepay', '超管', '13000000001', '1', 'https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/defava_m.png', 'D0001', 1, 1, 'MGR', '0', '2020-06-13', '2020-06-13');
insert into t_sys_user_auth values (801, '801', '1', 'jeepay', '$2a$10$WKuPJKE1XhX15ibqDM745eOCaZZVUiRitUjEyX6zVNd9k.cQXfzGa', 'testkey', 'MGR');

-- insert into t_sys_user_role_rela values (801, 801);

INSERT INTO `t_sys_config` VALUES ('mgrSiteUrl', '运营平台网址(不包含结尾/)', '运营平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9217', 'text', 0, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('mchSiteUrl', '商户平台网址(不包含结尾/)', '商户平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9218', 'text', 1, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('paySiteUrl', '支付网关地址(不包含结尾/)', '支付网关地址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9216', 'text', 2, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ossPublicSiteUrl', '公共oss访问地址(不包含结尾/)', '公共oss访问地址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9217/api/anon/localOssFiles', 'text', 3, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('testMchNo', '测试商户号', '支付测试页面默认使用的测试商户号', 'applicationConfig', '系统应用配置', '', 'text', 4, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('testProductId', '测试产品ID', '支付测试页面默认使用的测试产品ID', 'applicationConfig', '系统应用配置', '', 'text', 5, '2021-5-18 14:46:10');

-- 机器人配置
INSERT INTO `t_sys_config` VALUES ('ROBOT_MANAGE_GROUP_ID', '机器人管理群ID', '机器人管理群ID', 'robotConfig', '机器人配置', '', 'text', 0, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_TECH_GROUP_ID', '四方技术群ID', '四方技术群ID', 'robotConfig', '机器人配置', '', 'text', 1, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_POINT_WARNING_AMOUNT', '点数警告(元)', '点数警告(元)', 'robotConfig', '机器人配置', '', 'text', 2, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_CHANNEL_LIMIT_WARNING_AMOUNT', '通道额度警告(元)', '通道额度警告(元)', 'robotConfig', '机器人配置', '', 'text', 3, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_WARN_EXTRA_MSG', '警告附加讯息', '警告附加讯息', 'robotConfig', '机器人配置', '', 'textarea', 4, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_PULL_ORDER_ABNORMAL_NOTICE', '机器人拉单异常通知', '机器人拉单异常通知开关', 'robotConfig', '机器人配置', '0', 'switch', 5, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_MCH_CALLBACK_ABNORMAL_NOTICE', '机器人商户回调异常通知', '机器人商户回调异常通知开关', 'robotConfig', '机器人配置', '0', 'switch', 6, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_QUERY_RETURN_WITH_IMAGE', '查单返回是否传图', '查单返回是否传图开关', 'robotConfig', '机器人配置', '0', 'switch', 7, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_RECONCILE_SHOW_DISPATCH', '机器人对账显示下发', '机器人对账显示下发开关', 'robotConfig', '机器人配置', '0', 'switch', 8, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_RECONCILE_SHOW_TOP', '机器人对账显示置顶', '机器人对账显示置顶开关', 'robotConfig', '机器人配置', '0', 'switch', 9, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ROBOT_MCH_RATE_CHANGE_NOTICE', '商户费率修改通知', '商户费率修改通知开关', 'robotConfig', '机器人配置', '0', 'switch', 10, '2021-5-18 14:46:10');

-- 初始化支付方式
-- 已移除废弃支付方式初始化

INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_APP', '银联App支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_WAP', '银联手机网站支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_QR', '银联二维码(主扫)');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_BAR', '银联二维码(被扫)');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_B2B', '银联企业网银支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_PC', '银联网关支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_JSAPI', '银联Js支付');


-- 初始化支付接口定义
-- 已移除废弃支付接口定义初始化
