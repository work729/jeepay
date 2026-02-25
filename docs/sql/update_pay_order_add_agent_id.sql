-- 为支付订单表增加代理ID字段
ALTER TABLE `t_pay_order`
    ADD COLUMN `agent_id` INT DEFAULT NULL COMMENT '代理ID' AFTER `mch_no`;

-- 可选数据回填（如需）
-- UPDATE `t_pay_order` p
--   JOIN `t_mch_info` m ON m.mch_no = p.mch_no
--   SET p.agent_id = m.agent_id
-- WHERE p.agent_id IS NULL;
