-- 支付订单表新增回调成功时间字段
ALTER TABLE `t_pay_order` ADD COLUMN `notify_success_time` DATETIME DEFAULT NULL COMMENT '订单回调成功时间（通知下游成功时间）' AFTER `success_time`;

-- 更新运营平台菜单权限（如果之前没有隔日回调相关权限，可以添加）
-- 隔日回调搜索功能不需要额外权限，使用现有的 ENT_ORDER_LIST 即可
