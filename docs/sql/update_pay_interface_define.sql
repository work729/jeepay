ALTER TABLE `t_pay_interface_define` ADD COLUMN `telegram_group_id` varchar(64) DEFAULT NULL COMMENT '飞机群ID';
ALTER TABLE `t_pay_interface_define` ADD COLUMN `telegram_contact` varchar(64) DEFAULT NULL COMMENT '飞机群联络人';
ALTER TABLE `t_pay_interface_define` ADD COLUMN `notify_ip` varchar(128) DEFAULT NULL COMMENT '通知IP';
