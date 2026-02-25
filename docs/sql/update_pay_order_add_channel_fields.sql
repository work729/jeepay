-- 新增 t_pay_order 字段：通道ID、通道商ID
ALTER TABLE `t_pay_order`
    ADD COLUMN `channel_id` BIGINT(20) DEFAULT NULL COMMENT '通道ID' AFTER `product_id`,
    ADD COLUMN `channel_provider_id` VARCHAR(64) DEFAULT NULL COMMENT '通道商ID（t_pay_interface_define.if_code）' AFTER `channel_id`;
