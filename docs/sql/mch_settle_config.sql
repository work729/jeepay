CREATE TABLE IF NOT EXISTS `t_mch_settle_config` (
  `mch_no` varchar(64) NOT NULL COMMENT '商户号',
  `inherit_system` tinyint(3) NOT NULL DEFAULT 1 COMMENT '是否继承系统配置: 1-继承,0-自定义',
  `withdraw_enable` tinyint(3) DEFAULT NULL COMMENT '提现开关: 0-关闭,1-开启',
  `withdraw_days` varchar(64) DEFAULT NULL COMMENT '允许星期几提现, 逗号分隔1-7',
  `withdraw_start_time` varchar(16) DEFAULT NULL COMMENT '每日提现开始时间 HH:mm:ss',
  `withdraw_end_time` varchar(16) DEFAULT NULL COMMENT '每日提现结束时间 HH:mm:ss',
  `daily_times` int(11) DEFAULT NULL COMMENT '每日提现次数限制',
  `daily_max_amount` bigint(20) DEFAULT NULL COMMENT '每日提现最大金额,单位分',
  `single_max_amount` bigint(20) DEFAULT NULL COMMENT '单笔最大提现金额,单位分',
  `single_min_amount` bigint(20) DEFAULT NULL COMMENT '单笔最小提现金额,单位分',
  `fee_type` varchar(16) DEFAULT NULL COMMENT '结算手续费类型: PERCENT/FIXED',
  `fee_amount` bigint(20) DEFAULT NULL COMMENT '每笔手续费, 百分比时存费率(万分比)或固定金额(分)',
  `fee_max_amount` bigint(20) DEFAULT NULL COMMENT '单笔手续费上限,单位分',
  `settle_type` varchar(16) DEFAULT NULL COMMENT '结算类型: AUTO/MANUAL',
  `settle_mode` varchar(16) DEFAULT NULL COMMENT '结算方式: D0/D1',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`mch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户结算配置表';

