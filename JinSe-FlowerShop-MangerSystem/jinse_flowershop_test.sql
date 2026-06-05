-- ============================================================
-- 锦色花店 - 测试数据库初始化脚本
-- 生成日期: 2026-06-03
-- 基于: 项目实体类 + Mapper XML + 原有SQL结构
-- 说明: 包含建表语句 + 完整测试数据，可直接导入MySQL 8.x
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 创建数据库（如需新建）
-- ----------------------------
-- CREATE DATABASE IF NOT EXISTS jinse_flowershop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE jinse_flowershop;

-- ============================================================
-- 1. 员工表 (employee)
-- ============================================================
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `name` varchar(32) NULL DEFAULT NULL COMMENT '姓名',
  `username` varchar(32) NOT NULL COMMENT '账号',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `phone` varchar(11) NULL DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) NULL DEFAULT NULL COMMENT '性别 0女 1男',
  `id_number` varchar(18) NULL DEFAULT NULL COMMENT '身份证号',
  `status` int NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_username`(`username`)
) ENGINE=InnoDB COMMENT='员工表';

INSERT INTO `employee` VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', '1', '110101199001011234', 1, '2026-05-17 15:51:56', '2026-05-17 15:51:56', 1, 1);
INSERT INTO `employee` VALUES (2, '张经理', 'zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '13900139000', '1', '110101199002021234', 1, '2026-05-18 10:00:00', '2026-05-18 10:00:00', 1, 1);
INSERT INTO `employee` VALUES (3, '李店员', 'lisi', 'e10adc3949ba59abbe56e057f20f883e', '13700137000', '0', '110101199003031234', 1, '2026-05-19 09:00:00', '2026-05-19 09:00:00', 1, 1);

-- ============================================================
-- 2. 分类表 (category)
-- ============================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(32) NULL DEFAULT NULL COMMENT '分类名称',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  `image` varchar(500) NULL DEFAULT NULL COMMENT '分类图片',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='分类表';

INSERT INTO `category` VALUES (1, '花束', 0, 1, '2026-05-17 20:17:52', '2026-05-17 20:17:52', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/category/huashu.jpg');
INSERT INTO `category` VALUES (2, '盆栽', 1, 1, '2026-05-17 20:43:55', '2026-05-17 20:43:55', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/category/penzai.jpg');
INSERT INTO `category` VALUES (3, '园林造景', 2, 1, '2026-05-18 12:11:56', '2026-05-18 12:11:56', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/category/yuanlin.jpg');
INSERT INTO `category` VALUES (4, '多肉', 3, 1, '2026-05-19 18:05:53', '2026-05-19 18:05:53', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/category/duorou.jpg');
INSERT INTO `category` VALUES (5, '促销专区', 4, 0, '2026-05-19 13:09:17', '2026-05-19 13:09:17', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/category/cuxiao.jpg');

-- ============================================================
-- 3. 鲜花表 (flower)
-- ============================================================
DROP TABLE IF EXISTS `flower`;
CREATE TABLE `flower` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鲜花ID',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '鲜花名称',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `image` varchar(500) NULL DEFAULT NULL COMMENT '图片',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '描述信息',
  `status` int NULL DEFAULT 1 COMMENT '状态 0停售 1起售',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='鲜花表';

-- 花束类
INSERT INTO `flower` VALUES (1, '玫瑰花束', 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose.jpg', '经典红玫瑰，11朵装', 1, '2026-05-17 20:29:00', '2026-05-17 20:29:00', 1, 1);
INSERT INTO `flower` VALUES (2, '百合花束', 1, 156.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/lily.jpg', '香水百合，清新淡雅', 1, '2026-05-17 20:29:00', '2026-05-17 20:29:00', 1, 1);
INSERT INTO `flower` VALUES (3, '康乃馨花束', 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/carnation.jpg', '温馨康乃馨，感恩之选', 1, '2026-05-17 20:29:00', '2026-05-17 20:29:00', 1, 1);
INSERT INTO `flower` VALUES (4, '毕业花束', 1, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/graduation.jpg', '毕业季专属花束', 1, '2026-05-17 20:29:00', '2026-05-17 20:29:00', 1, 1);
INSERT INTO `flower` VALUES (5, '满天星花束', 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/gypsophila.jpg', '浪漫满天星', 1, '2026-05-17 20:29:00', '2026-05-17 20:29:00', 1, 1);
-- 盆栽类
INSERT INTO `flower` VALUES (6, '红掌', 2, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/anthurium.jpg', '红掌盆栽，四季常绿', 1, '2026-05-18 10:00:00', '2026-05-18 10:00:00', 1, 1);
INSERT INTO `flower` VALUES (7, '蝴蝶兰', 2, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/orchid.jpg', '蝴蝶兰，高贵典雅', 1, '2026-05-18 10:00:00', '2026-05-18 10:00:00', 1, 1);
INSERT INTO `flower` VALUES (8, '君子兰', 2, 25.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/clivia.jpg', '君子兰，文人雅士之选', 1, '2026-05-18 10:00:00', '2026-05-18 10:00:00', 1, 1);
INSERT INTO `flower` VALUES (9, '白掌', 2, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/peace_lily.jpg', '白掌，净化空气', 0, '2026-05-18 10:00:00', '2026-05-18 10:00:00', 1, 1);
INSERT INTO `flower` VALUES (10, '玫瑰海棠', 2, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/begonia.jpg', '玫瑰海棠，花色艳丽', 1, '2026-05-18 10:00:00', '2026-05-18 10:00:00', 1, 1);
-- 园林造景类
INSERT INTO `flower` VALUES (11, '蔷薇', 3, 50.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose_climbing.jpg', '攀援蔷薇，庭院首选', 1, '2026-05-19 10:00:00', '2026-05-19 10:00:00', 1, 1);
INSERT INTO `flower` VALUES (12, '月季', 3, 100.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/chinese_rose.jpg', '月季花，月月有花', 1, '2026-05-19 10:00:00', '2026-05-19 10:00:00', 1, 1);
-- 多肉类
INSERT INTO `flower` VALUES (13, '多肉拼盘', 4, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/succulent.jpg', '多肉拼盘，萌萌哒', 1, '2026-05-19 18:00:00', '2026-05-19 18:00:00', 1, 1);
INSERT INTO `flower` VALUES (14, '仙人掌', 4, 10.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cactus.jpg', '仙人掌，好养活', 1, '2026-05-19 18:00:00', '2026-05-19 18:00:00', 1, 1);
-- 促销鲜花
INSERT INTO `flower` VALUES (15, '[促销]玫瑰花束', 1, 168.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose_promo.jpg', '520活动促销', 1, '2026-05-24 14:00:00', '2026-05-24 14:00:00', 1, 1);
INSERT INTO `flower` VALUES (16, '[促销]康乃馨', 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/carnation_promo.jpg', '毕业季促销', 1, '2026-05-24 14:00:00', '2026-05-24 14:00:00', 1, 1);
INSERT INTO `flower` VALUES (17, '[促销]月季', 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/chinese_rose_promo.jpg', '毕业季促销', 1, '2026-05-24 14:00:00', '2026-05-24 14:00:00', 1, 1);
INSERT INTO `flower` VALUES (18, '[促销]满天星', 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/gypsophila_promo.jpg', '毕业季促销', 1, '2026-05-24 14:00:00', '2026-05-24 14:00:00', 1, 1);
INSERT INTO `flower` VALUES (19, '[促销]蔷薇', 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose_climbing_promo.jpg', '毕业季促销', 1, '2026-05-24 14:00:00', '2026-05-24 14:00:00', 1, 1);
INSERT INTO `flower` VALUES (20, '[促销]君子兰', 2, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/clivia_promo.jpg', '毕业季促销', 1, '2026-05-24 14:00:00', '2026-05-24 14:00:00', 1, 1);

-- ============================================================
-- 4. 活动表 (activity)
-- ============================================================
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `start_time` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `status` int NULL DEFAULT 0 COMMENT '活动状态 0未开始 1进行中 2已结束',
  `content` varchar(500) NULL DEFAULT NULL COMMENT '活动简介',
  `limit_per` int NULL DEFAULT NULL COMMENT '限购数量',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='活动表';

INSERT INTO `activity` VALUES (1, '2026-05-20 00:00:00', '2026-05-21 23:59:59', 2, '520告白日', 1, '2026-05-18 10:00:00', '2026-05-18 10:00:00', 1, 1);
INSERT INTO `activity` VALUES (2, '2026-06-01 00:00:00', '2026-06-30 23:59:59', 1, '毕业季', 5, '2026-05-25 10:00:00', '2026-05-25 10:00:00', 1, 1);
INSERT INTO `activity` VALUES (3, '2026-07-01 00:00:00', '2026-07-31 23:59:59', 0, '暑期清凉季', 3, '2026-06-01 10:00:00', '2026-06-01 10:00:00', 1, 1);

-- ============================================================
-- 5. 活动销售表 (activity_sale)
-- ============================================================
DROP TABLE IF EXISTS `activity_sale`;
CREATE TABLE `activity_sale` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动销售ID',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '活动ID',
  `flower_id` bigint NULL DEFAULT NULL COMMENT '花束ID',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `discount_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '优惠价',
  `stock` int NULL DEFAULT NULL COMMENT '库存数量',
  `sale` int NULL DEFAULT 0 COMMENT '已售数量',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  `version` int NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='活动销售表';

-- 520活动
INSERT INTO `activity_sale` VALUES (1, 1, 15, 188.00, 168.00, 50, 12, '2026-05-20 08:00:00', '2026-05-20 08:00:00', 1, 1, 0);
-- 毕业季活动
INSERT INTO `activity_sale` VALUES (2, 2, 16, 88.00, 68.00, 200, 35, '2026-06-01 08:00:00', '2026-06-01 08:00:00', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (3, 2, 17, 100.00, 80.00, 150, 28, '2026-06-01 08:00:00', '2026-06-01 08:00:00', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (4, 2, 18, 68.00, 28.00, 300, 85, '2026-06-01 08:00:00', '2026-06-01 08:00:00', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (5, 2, 19, 50.00, 48.00, 100, 15, '2026-06-01 08:00:00', '2026-06-01 08:00:00', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (6, 2, 20, 25.00, 20.00, 80, 10, '2026-06-01 08:00:00', '2026-06-01 08:00:00', 1, 1, 0);

-- ============================================================
-- 6. 用户表 (user)
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(32) NULL DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) NULL DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) NULL DEFAULT NULL COMMENT '性别 0女 1男',
  `id_number` varchar(18) NULL DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) NULL DEFAULT NULL COMMENT '头像',
  `create_time` datetime NULL DEFAULT NULL COMMENT '注册时间',
  `username` varchar(32) NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(64) NULL DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='用户表';

INSERT INTO `user` VALUES (1, '张三', '13100001001', '1', '110101200001011234', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/avatar1.jpg', '2026-05-20 09:00:00', 'zhangsan', 'e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `user` VALUES (2, '李四', '13100001002', '1', '110101200002021234', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/avatar2.jpg', '2026-05-21 10:00:00', 'lisi', 'e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `user` VALUES (3, '王五', '13100001003', '0', '110101200003031234', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/avatar3.jpg', '2026-05-22 11:00:00', 'wangwu', 'e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `user` VALUES (4, '赵六', '13100001004', '1', '110101200004041234', NULL, '2026-05-25 14:00:00', 'zhaoliu', 'e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `user` VALUES (5, '孙七', '13100001005', '0', '110101200005051234', NULL, '2026-05-28 16:00:00', 'sunqi', 'e10adc3949ba59abbe56e057f20f883e');
INSERT INTO `user` VALUES (6, '周八', '13100001006', '1', '110101200006061234', NULL, '2026-05-30 08:00:00', 'zhouba', 'e10adc3949ba59abbe56e057f20f883e');

-- ============================================================
-- 7. 地址簿表 (address_book)
-- ============================================================
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址簿ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `consignee` varchar(32) NULL DEFAULT NULL COMMENT '收货人',
  `phone` varchar(20) NULL DEFAULT NULL COMMENT '手机号',
  `province_code` varchar(12) NULL DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) NULL DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) NULL DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) NULL DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) NULL DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) NULL DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(255) NULL DEFAULT NULL COMMENT '详细地址',
  `label` varchar(50) NULL DEFAULT NULL COMMENT '标签',
  `is_default` int NULL DEFAULT 0 COMMENT '是否默认 0否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='地址簿表';

INSERT INTO `address_book` VALUES (1, 1, '张三', '13100001001', '440000', '广东省', '440300', '深圳市', '440305', '南山区', '科技园路1号', '公司', 1);
INSERT INTO `address_book` VALUES (2, 1, '张三', '13100001001', '440000', '广东省', '440100', '广州市', '440103', '荔湾区', '花地大道10号', '家', 0);
INSERT INTO `address_book` VALUES (3, 2, '李四', '13100001002', '440000', '广东省', '440600', '佛山市', '440605', '南海区', '佛山大学', '学校', 1);
INSERT INTO `address_book` VALUES (4, 3, '王五', '13100001003', '110000', '北京市', '110100', '北京市', '110105', '朝阳区', '望京SOHO', '公司', 1);
INSERT INTO `address_book` VALUES (5, 4, '赵六', '13100001004', '330000', '浙江省', '330100', '杭州市', '330106', '西湖区', '浙大紫金港', '学校', 1);

-- ============================================================
-- 8. 订单表 (orders)
-- ============================================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `number` varchar(50) NULL DEFAULT NULL COMMENT '订单号',
  `status` int NULL DEFAULT NULL COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已确认收货 6已完成 7已取消 8退货申请中',
  `user_id` bigint NULL DEFAULT NULL COMMENT '下单用户ID',
  `address_book_id` bigint NULL DEFAULT NULL COMMENT '地址簿ID',
  `order_time` datetime NULL DEFAULT NULL COMMENT '下单时间',
  `checkout_time` datetime NULL DEFAULT NULL COMMENT '结账时间',
  `pay_method` int NULL DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
  `pay_status` int NULL DEFAULT NULL COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '实收金额',
  `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `user_name` varchar(32) NULL DEFAULT NULL COMMENT '用户名',
  `phone` varchar(11) NULL DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) NULL DEFAULT NULL COMMENT '地址',
  `consignee` varchar(32) NULL DEFAULT NULL COMMENT '收货人',
  `cancel_reason` varchar(255) NULL DEFAULT NULL COMMENT '取消原因',
  `rejection_reason` varchar(255) NULL DEFAULT NULL COMMENT '拒绝原因',
  `previous_status` int NULL DEFAULT NULL COMMENT '退货前原始状态',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `estimated_delivery_time` datetime NULL DEFAULT NULL COMMENT '预计送达时间',
  `delivery_status` int NULL DEFAULT NULL COMMENT '配送状态 1立即送出 0选择具体时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '送达时间',
  `pack_amount` int NULL DEFAULT 0 COMMENT '打包费',
  `delivery_fee` int NULL DEFAULT 0 COMMENT '配送费',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='订单表';

-- 已完成订单（覆盖多天，用于数据统计）
INSERT INTO `orders` VALUES (1, 'ORD20260528001', 6, 1, 1, '2026-05-28 10:30:00', '2026-05-28 10:31:00', 1, 1, 188.00, '', '张三', '13100001001', '科技园路1号', '张三', NULL, NULL, NULL, NULL, '2026-05-28 18:00:00', 1, '2026-05-28 16:00:00', 0, 0);
INSERT INTO `orders` VALUES (2, 'ORD20260528002', 6, 2, 3, '2026-05-28 14:20:00', '2026-05-28 14:21:00', 1, 1, 68.00, '送花给朋友', '李四', '13100001002', '佛山大学', '李四', NULL, NULL, NULL, NULL, '2026-05-28 20:00:00', 1, '2026-05-28 18:00:00', 0, 0);
INSERT INTO `orders` VALUES (3, 'ORD20260529001', 6, 1, 1, '2026-05-29 09:15:00', '2026-05-29 09:16:00', 1, 1, 256.00, '', '张三', '13100001001', '科技园路1号', '张三', NULL, NULL, NULL, NULL, '2026-05-29 18:00:00', 1, '2026-05-29 15:00:00', 0, 0);
INSERT INTO `orders` VALUES (4, 'ORD20260529002', 6, 3, 4, '2026-05-29 11:00:00', '2026-05-29 11:01:00', 2, 1, 100.00, '', '王五', '13100001003', '望京SOHO', '王五', NULL, NULL, NULL, NULL, '2026-05-29 18:00:00', 1, '2026-05-29 16:00:00', 0, 0);
INSERT INTO `orders` VALUES (5, 'ORD20260529003', 6, 2, 3, '2026-05-29 16:30:00', '2026-05-29 16:31:00', 1, 1, 156.00, '', '李四', '13100001002', '佛山大学', '李四', NULL, NULL, NULL, NULL, '2026-05-29 20:00:00', 1, '2026-05-29 19:00:00', 0, 0);
INSERT INTO `orders` VALUES (6, 'ORD20260530001', 6, 1, 2, '2026-05-30 08:45:00', '2026-05-30 08:46:00', 1, 1, 30.00, '', '张三', '13100001001', '花地大道10号', '张三', NULL, NULL, NULL, NULL, '2026-05-30 18:00:00', 1, '2026-05-30 14:00:00', 0, 0);
INSERT INTO `orders` VALUES (7, 'ORD20260530002', 6, 4, 5, '2026-05-30 10:20:00', '2026-05-30 10:21:00', 1, 1, 188.00, '', '赵六', '13100001004', '浙大紫金港', '赵六', NULL, NULL, NULL, NULL, '2026-05-30 18:00:00', 1, '2026-05-30 15:00:00', 0, 0);
INSERT INTO `orders` VALUES (8, 'ORD20260530003', 6, 2, 3, '2026-05-30 15:00:00', '2026-05-30 15:01:00', 1, 1, 80.00, '', '李四', '13100001002', '佛山大学', '李四', NULL, NULL, NULL, NULL, '2026-05-30 20:00:00', 1, '2026-05-30 18:00:00', 0, 0);
INSERT INTO `orders` VALUES (9, 'ORD20260601001', 6, 1, 1, '2026-06-01 09:00:00', '2026-06-01 09:01:00', 1, 1, 28.00, '', '张三', '13100001001', '科技园路1号', '张三', NULL, NULL, NULL, NULL, '2026-06-01 18:00:00', 1, '2026-06-01 14:00:00', 0, 0);
INSERT INTO `orders` VALUES (10, 'ORD20260601002', 6, 5, 1, '2026-06-01 11:30:00', '2026-06-01 11:31:00', 2, 1, 48.00, '', '孙七', '13100001005', '科技园路1号', '孙七', NULL, NULL, NULL, NULL, '2026-06-01 18:00:00', 1, '2026-06-01 16:00:00', 0, 0);
-- 已确认收货订单（status=5）
INSERT INTO `orders` VALUES (11, 'ORD20260601003', 5, 3, 4, '2026-06-01 14:00:00', '2026-06-01 14:01:00', 1, 1, 50.00, '', '王五', '13100001003', '望京SOHO', '王五', NULL, NULL, NULL, NULL, '2026-06-01 20:00:00', 1, '2026-06-01 18:00:00', 0, 0);
INSERT INTO `orders` VALUES (12, 'ORD20260602001', 5, 6, 1, '2026-06-02 10:00:00', '2026-06-02 10:01:00', 1, 1, 168.00, '', '周八', '13100001006', '科技园路1号', '周八', NULL, NULL, NULL, NULL, '2026-06-02 18:00:00', 1, '2026-06-02 15:00:00', 0, 0);
-- 待接单订单（status=2）
INSERT INTO `orders` VALUES (13, 'ORD20260602002', 2, 1, 1, '2026-06-02 16:30:00', '2026-06-02 16:31:00', 1, 1, 88.00, '尽快送达', '张三', '13100001001', '科技园路1号', '张三', NULL, NULL, NULL, NULL, NULL, 1, NULL, 0, 0);
-- 已取消订单（status=7）
INSERT INTO `orders` VALUES (14, 'ORD20260529004', 7, 4, 5, '2026-05-29 17:00:00', '2026-05-29 17:01:00', 1, 2, 15.00, '', '赵六', '13100001004', '浙大紫金港', '赵六', '不想要了', NULL, NULL, '2026-05-29 17:05:00', NULL, NULL, NULL, 0, 0);
-- 退货申请中（status=8）
INSERT INTO `orders` VALUES (15, 'ORD20260531001', 8, 2, 3, '2026-05-31 09:00:00', '2026-05-31 09:01:00', 1, 1, 68.00, '', '李四', '13100001002', '佛山大学', '李四', NULL, NULL, 5, NULL, NULL, NULL, NULL, 0, 0);
-- 派送中（status=4）
INSERT INTO `orders` VALUES (16, 'ORD20260603001', 4, 3, 4, '2026-06-03 08:00:00', '2026-06-03 08:01:00', 1, 1, 188.00, '', '王五', '13100001003', '望京SOHO', '王五', NULL, NULL, NULL, NULL, '2026-06-03 18:00:00', 1, '2026-06-03 10:00:00', 0, 0);

-- ============================================================
-- 9. 订单明细表 (order_detail)
-- ============================================================
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `flower_id` bigint NULL DEFAULT NULL COMMENT '鲜花ID',
  `number` int NULL DEFAULT NULL COMMENT '数量',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `image` varchar(500) NULL DEFAULT NULL COMMENT '图片',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='订单明细表';

-- 订单1: 玫瑰花束x1
INSERT INTO `order_detail` VALUES (1, '玫瑰花束', 1, 1, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose.jpg');
-- 订单2: [促销]康乃馨x1
INSERT INTO `order_detail` VALUES (2, '[促销]康乃馨', 2, 16, 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/carnation_promo.jpg');
-- 订单3: 毕业花束x1
INSERT INTO `order_detail` VALUES (3, '毕业花束', 3, 4, 1, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/graduation.jpg');
-- 订单4: 月季x1
INSERT INTO `order_detail` VALUES (4, '月季', 4, 12, 1, 100.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/chinese_rose.jpg');
-- 订单5: 百合花束x1
INSERT INTO `order_detail` VALUES (5, '百合花束', 5, 2, 1, 156.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/lily.jpg');
-- 订单6: 红掌x1
INSERT INTO `order_detail` VALUES (6, '红掌', 6, 6, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/anthurium.jpg');
-- 订单7: 蝴蝶兰x1
INSERT INTO `order_detail` VALUES (7, '蝴蝶兰', 7, 7, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/orchid.jpg');
-- 订单8: [促销]月季x1
INSERT INTO `order_detail` VALUES (8, '[促销]月季', 8, 17, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/chinese_rose_promo.jpg');
-- 订单9: [促销]满天星x1
INSERT INTO `order_detail` VALUES (9, '[促销]满天星', 9, 18, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/gypsophila_promo.jpg');
-- 订单10: [促销]蔷薇x1
INSERT INTO `order_detail` VALUES (10, '[促销]蔷薇', 10, 19, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose_climbing_promo.jpg');
-- 订单11: 蔷薇x1
INSERT INTO `order_detail` VALUES (11, '蔷薇', 11, 11, 1, 50.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose_climbing.jpg');
-- 订单12: [促销]玫瑰花束x1
INSERT INTO `order_detail` VALUES (12, '[促销]玫瑰花束', 12, 15, 1, 168.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/rose_promo.jpg');
-- 订单13: 康乃馨花束x1
INSERT INTO `order_detail` VALUES (13, '康乃馨花束', 13, 3, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/carnation.jpg');
-- 订单14: 多肉拼盘x1
INSERT INTO `order_detail` VALUES (14, '多肉拼盘', 14, 13, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/succulent.jpg');
-- 订单15: [促销]康乃馨x1
INSERT INTO `order_detail` VALUES (15, '[促销]康乃馨', 15, 16, 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/carnation_promo.jpg');
-- 订单16: 蝴蝶兰x1
INSERT INTO `order_detail` VALUES (16, '蝴蝶兰', 16, 7, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/orchid.jpg');

-- ============================================================
-- 10. 评论表 (comment)
-- ============================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `flower_id` bigint NULL DEFAULT NULL COMMENT '花束ID',
  `rating` int NULL DEFAULT NULL COMMENT '评价等级',
  `content` varchar(500) NULL DEFAULT NULL COMMENT '评论内容',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='评论表';

INSERT INTO `comment` VALUES (1, 1, 1, 5, '花很新鲜，女朋友很喜欢！', '2026-05-29 10:00:00', 3, 0);
INSERT INTO `comment` VALUES (2, 2, 16, 4, '价格实惠，质量不错', '2026-05-29 12:00:00', 1, 0);
INSERT INTO `comment` VALUES (3, 1, 4, 5, '毕业花束太棒了，同学们都很喜欢', '2026-05-30 09:00:00', 5, 0);
INSERT INTO `comment` VALUES (4, 3, 12, 4, '月季花开得很好', '2026-05-30 11:00:00', 0, 0);
INSERT INTO `comment` VALUES (5, 2, 2, 3, '百合花有点蔫了', '2026-05-30 14:00:00', 0, 0);
INSERT INTO `comment` VALUES (6, 4, 7, 5, '蝴蝶兰非常漂亮，值得购买', '2026-05-31 09:00:00', 2, 0);
INSERT INTO `comment` VALUES (7, 1, 6, 4, '红掌养了一个月还活着，不错', '2026-06-01 10:00:00', 0, 0);
INSERT INTO `comment` VALUES (8, 5, 19, 5, '蔷薇很香，推荐！', '2026-06-01 15:00:00', 1, 0);
INSERT INTO `comment` VALUES (9, 3, 18, 2, '满天星收到时有些压坏了', '2026-06-02 09:00:00', 0, 0);
INSERT INTO `comment` VALUES (10, 6, 15, 5, '促销价格很划算', '2026-06-02 14:00:00', 2, 0);

-- ============================================================
-- 11. 用户点赞评论记录表 (user_comment)
-- ============================================================
DROP TABLE IF EXISTS `user_comment`;
CREATE TABLE `user_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户点赞记录ID',
  `comment_id` bigint NULL DEFAULT NULL COMMENT '评论ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_comment`(`user_id`, `comment_id`)
) ENGINE=InnoDB COMMENT='用户点赞评论记录表';

INSERT INTO `user_comment` VALUES (1, 1, 2);
INSERT INTO `user_comment` VALUES (2, 1, 3);
INSERT INTO `user_comment` VALUES (3, 3, 2);
INSERT INTO `user_comment` VALUES (4, 3, 4);
INSERT INTO `user_comment` VALUES (5, 3, 5);
INSERT INTO `user_comment` VALUES (6, 3, 6);
INSERT INTO `user_comment` VALUES (7, 6, 1);
INSERT INTO `user_comment` VALUES (8, 6, 5);
INSERT INTO `user_comment` VALUES (9, 10, 1);
INSERT INTO `user_comment` VALUES (10, 10, 4);

-- ============================================================
-- 12. 购物车表 (shopping_cart)
-- ============================================================
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `name` varchar(64) NULL DEFAULT NULL COMMENT '名称',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `flower_id` bigint NULL DEFAULT NULL COMMENT '鲜花ID',
  `number` int NULL DEFAULT NULL COMMENT '数量',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `image` varchar(500) NULL DEFAULT NULL COMMENT '图片',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='购物车表';

INSERT INTO `shopping_cart` VALUES (1, '[促销]月季', 1, 17, 2, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/chinese_rose_promo.jpg', '2026-06-02 10:00:00');
INSERT INTO `shopping_cart` VALUES (2, '蝴蝶兰', 3, 7, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/orchid.jpg', '2026-06-02 15:00:00');

-- ============================================================
-- 13. 活动参与表 (participation)
-- ============================================================
DROP TABLE IF EXISTS `participation`;
CREATE TABLE `participation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与ID',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '活动ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `quantity` int NULL DEFAULT NULL COMMENT '数量',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='活动参与表';

INSERT INTO `participation` VALUES (1, 1, 1, 1, 1, '2026-05-28 10:30:00');
INSERT INTO `participation` VALUES (2, 2, 2, 1, 2, '2026-05-28 14:20:00');
INSERT INTO `participation` VALUES (3, 2, 2, 1, 5, '2026-05-29 16:30:00');
INSERT INTO `participation` VALUES (4, 2, 1, 1, 9, '2026-06-01 09:00:00');
INSERT INTO `participation` VALUES (5, 2, 5, 1, 10, '2026-06-01 11:30:00');
INSERT INTO `participation` VALUES (6, 2, 6, 1, 12, '2026-06-02 10:00:00');

SET FOREIGN_KEY_CHECKS = 1;
