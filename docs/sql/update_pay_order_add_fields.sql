-- 新增 t_pay_order 字段：产品ID、产品名称、通道费率（百分比）
ALTER TABLE `t_pay_order`
    ADD COLUMN `product_id` BIGINT(20) DEFAULT NULL COMMENT '产品ID' AFTER `amount`,
    ADD COLUMN `channel_name` VARCHAR(64) DEFAULT NULL COMMENT '通道名称' AFTER `product_id`,
    ADD COLUMN `channel_fee_rate` DECIMAL(20,6) DEFAULT NULL COMMENT '通道费率（百分比）' AFTER `channel_name`;

