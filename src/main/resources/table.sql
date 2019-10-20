DROP TABLE IF EXISTS `batch_no_seq`;
CREATE TABLE `batch_no_seq` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `current_value` int(18) DEFAULT NULL COMMENT '当前值',
  `company_code` varchar(10) DEFAULT NULL COMMENT '公司代码',
  `curr_date` varchar(20) DEFAULT NULL COMMENT '当前日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='批次号序列';