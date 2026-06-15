/*
 Navicat Premium Data Transfer

 Source Server         : MySql
 Source Server Type    : MySQL
 Source Server Version : 80400 (8.4.0)
 Source Host           : localhost:3306
 Source Schema         : jinse_flowershop

 Target Server Type    : MySQL
 Target Server Version : 80400 (8.4.0)
 File Encoding         : 65001

 Date: 14/06/2026 11:49:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `start_time` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `status` int NULL DEFAULT 0 COMMENT '活动状态 0未开始 1进行中 2已结束',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动简介',
  `limit_per` int NULL DEFAULT NULL COMMENT '限购数量',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (5, '2026-05-13 00:00:00', '2027-05-20 00:00:00', 1, '520', 1, '2026-05-20 20:16:11', '2026-06-05 15:14:26', 1, 1);
INSERT INTO `activity` VALUES (7, '2026-05-31 00:00:00', '2026-06-30 00:00:00', 1, '毕业季', 5, '2026-05-21 10:38:35', '2026-05-29 19:26:51', 1, 1);
INSERT INTO `activity` VALUES (9, '2026-05-21 00:00:00', '2027-05-18 00:00:00', 1, '毕业大礼包', 5, '2026-05-21 21:29:02', '2026-06-05 15:14:38', 1, 1);
INSERT INTO `activity` VALUES (11, '2026-06-01 00:00:00', '2026-06-07 00:00:00', 2, '6.1儿童节', 6, '2026-06-05 14:37:34', '2026-06-05 14:37:51', 1, 1);
INSERT INTO `activity` VALUES (12, '2026-12-16 00:00:00', '2027-02-23 00:00:00', 0, '年花', 5, '2026-06-05 14:38:32', '2026-06-05 14:38:32', 1, 1);
INSERT INTO `activity` VALUES (13, '2026-06-02 00:00:00', '2026-06-30 00:00:00', 1, '端午促销', 10, '2026-06-05 14:38:56', '2026-06-05 15:14:49', 1, 1);

-- ----------------------------
-- Table structure for activity_sale
-- ----------------------------
DROP TABLE IF EXISTS `activity_sale`;
CREATE TABLE `activity_sale`  (
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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动销售表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_sale
-- ----------------------------
INSERT INTO `activity_sale` VALUES (37, 5, 32, 100.00, 80.00, 110, 0, '2026-05-24 14:22:12', '2026-05-24 14:22:12', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (38, 5, 33, 30.00, 20.00, 220, 2, '2026-05-24 14:23:38', '2026-05-24 14:23:38', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (39, 9, 34, 250.00, 244.00, 103, 7, '2026-05-24 14:45:00', '2026-05-24 14:45:00', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (40, 7, 35, 250.00, 233.00, 109, 1, '2026-05-24 14:50:16', '2026-05-24 14:50:16', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (41, 9, 36, 100.00, 80.00, 1105, 5, '2026-05-24 14:51:33', '2026-05-24 14:51:33', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (42, 7, 37, 30.00, 20.00, 110, 1, '2026-05-24 14:51:48', '2026-05-24 14:51:48', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (44, 9, 39, 50.00, 48.00, 53, 2, '2026-05-24 16:19:06', '2026-06-05 13:59:22', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (45, 9, 40, 30.00, 28.00, 231, 13, '2026-05-24 16:19:47', '2026-05-24 16:19:47', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (48, 9, 43, 25.00, 20.00, 0, 1, '2026-05-28 11:42:40', '2026-05-28 11:42:40', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (50, 13, 45, 100.00, 80.00, 50, 0, '2026-06-05 14:39:21', '2026-06-05 14:39:21', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (51, 13, 46, 30.00, 20.00, 19, 1, '2026-06-05 14:39:29', '2026-06-05 14:39:29', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (52, 13, 47, 25.00, 15.00, 15, 0, '2026-06-05 14:39:37', '2026-06-05 14:39:37', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (53, 13, 48, 15.00, 8.00, 1108, 2, '2026-06-05 14:39:48', '2026-06-05 14:39:48', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (54, 13, 49, 20.00, 10.00, 1103, 7, '2026-06-05 14:39:57', '2026-06-05 14:39:57', 1, 1, 0);

-- ----------------------------
-- Table structure for address_book
-- ----------------------------
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址簿ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `consignee` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `province_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `is_default` int NULL DEFAULT 0 COMMENT '是否默认 0否 1是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地址簿表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of address_book
-- ----------------------------
INSERT INTO `address_book` VALUES (2, 7, '张三', '13131313123', '130000', '河北省', '130200', '唐山市', '130204', '古冶区', '123', '公司', 1);
INSERT INTO `address_book` VALUES (3, 7, '张三', '13121212123', '440000', '广东省', '440300', '深圳市', '440305', '南山区', '提瓦特', '公司', 0);
INSERT INTO `address_book` VALUES (5, 9, '李四', '13132323532', '440000', '广东省', '440600', '佛山市', '440605', '南海区', '佛山大学', '学校', 0);
INSERT INTO `address_book` VALUES (7, 10, '蹦蹦小圆帽', '13424353532', '110000', '北京市', '110100', '市辖区', '110101', '东城区', '123', '公司', 0);
INSERT INTO `address_book` VALUES (8, 4, '张三', '14423243234', '120000', '天津市', '120100', '市辖区', '120102', '河东区', '213', '公司', 0);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (6, '花束', 0, 1, '2026-05-17 20:17:52', '2026-05-19 20:36:12', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/1b0e4358-3a78-45e3-baf0-2bb47204ed70.jpg');
INSERT INTO `category` VALUES (9, '园林造景', 1, 1, '2026-05-17 20:43:55', '2026-05-19 13:08:22', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/3656fa8e-7428-4906-86e2-5cbfb721e424.jpg');
INSERT INTO `category` VALUES (11, '盆栽', 0, 1, '2026-05-18 12:11:56', '2026-05-19 13:08:56', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/2ab71c5a-ddfd-4a6d-b45c-81001c6ba037.jpg');
INSERT INTO `category` VALUES (13, '促销', 2, 0, '2026-05-19 13:09:17', '2026-05-19 20:22:48', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/fb9c7431-abce-4d47-a093-339f07e99770.jpg');
INSERT INTO `category` VALUES (14, '多肉', 2, 1, '2026-05-19 18:05:53', '2026-05-19 18:19:27', 1, 1, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/f03c934d-7286-434b-a439-bd2503f688c9.jpg');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `flower_id` bigint NULL DEFAULT NULL COMMENT '花束ID',
  `rating` int NULL DEFAULT NULL COMMENT '评价等级',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论内容',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (3, NULL, 39, 4, '还行\n', '2026-05-28 10:19:31', 0, 0);
INSERT INTO `comment` VALUES (4, NULL, 39, 5, 'ok', '2026-05-28 11:31:55', 0, 0);
INSERT INTO `comment` VALUES (5, 9, 40, 5, '好', '2026-05-28 15:12:31', 0, 0);
INSERT INTO `comment` VALUES (6, 9, 35, 5, '好', '2026-05-28 15:37:05', 0, 0);
INSERT INTO `comment` VALUES (7, 9, 33, 4, '一般般', '2026-05-28 21:54:09', 0, 0);
INSERT INTO `comment` VALUES (10, 9, 40, 1, '差评', '2026-05-29 12:53:45', 1, 0);
INSERT INTO `comment` VALUES (12, 9, 40, 5, '也就那样', '2026-05-29 12:54:04', 0, 0);
INSERT INTO `comment` VALUES (13, 9, 40, 3, '还行', '2026-05-29 12:54:14', 0, 0);
INSERT INTO `comment` VALUES (15, 9, 40, 5, 'OK', '2026-05-29 12:54:29', 0, 0);
INSERT INTO `comment` VALUES (17, 9, 32, 5, '孩子很喜欢\n', '2026-06-01 14:42:34', 0, 0);
INSERT INTO `comment` VALUES (18, 9, 32, 5, '好吃', '2026-06-01 14:42:45', 0, 0);
INSERT INTO `comment` VALUES (19, 9, 32, 1, '不能吃', '2026-06-01 14:42:53', 0, 0);
INSERT INTO `comment` VALUES (20, 7, 32, 1, '孩子不喜欢', '2026-06-01 14:43:17', 1, 0);
INSERT INTO `comment` VALUES (21, 7, 32, 2, '怎么是一包种子', '2026-06-01 14:43:59', 0, 0);
INSERT INTO `comment` VALUES (22, 7, 33, 1, '谢太快', '2026-06-01 14:48:42', 0, 0);
INSERT INTO `comment` VALUES (23, 7, 34, 5, '贵', '2026-06-01 14:48:52', 0, 0);

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别 0女 1男',
  `id_number` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `status` int NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of employee
-- ----------------------------
INSERT INTO `employee` VALUES (1, '管理员', 'admin', '21232f297a57a5a743894a0e4a801fc3', '13800138000', '1', '110101199001011234', 1, '2026-05-17 15:51:56', '2026-05-19 11:48:51', 1, 1);
INSERT INTO `employee` VALUES (19, '李四', '123', 'e10adc3949ba59abbe56e057f20f883e', '111111111', '1', '52555555', 1, '2026-05-18 21:36:57', '2026-05-19 11:47:05', 1, 1);

-- ----------------------------
-- Table structure for flower
-- ----------------------------
DROP TABLE IF EXISTS `flower`;
CREATE TABLE `flower`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鲜花ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鲜花名称',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述信息',
  `status` int NULL DEFAULT 1 COMMENT '状态 0停售 1起售',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鲜花表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flower
-- ----------------------------
INSERT INTO `flower` VALUES (1, '玫瑰', 11, 60.01, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/ea575cdf-ac9a-4105-9cfe-3cdc2ab0edf7.jpg', '玫瑰', 0, '2026-05-17 20:29:00', '2026-05-17 20:29:00', 1, 1);
INSERT INTO `flower` VALUES (3, '多肉', 14, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg', '肉肉', 1, '2026-05-19 18:45:24', '2026-05-19 18:45:24', 1, 1);
INSERT INTO `flower` VALUES (9, '白掌', 11, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/4faede0d-cd5f-4aa8-81b5-be157046668a.jpg', '白毛浮绿水', 0, '2026-05-19 19:35:31', '2026-05-19 19:35:31', 1, 1);
INSERT INTO `flower` VALUES (10, '百合', 6, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/b889ea94-b2dc-4704-a4d3-9c1c99877ac7.jpg', '香水百合', 0, '2026-05-19 19:36:18', '2026-05-19 19:36:18', 1, 1);
INSERT INTO `flower` VALUES (11, '毕业花束', 6, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg', '毕业季', 1, '2026-05-19 19:41:41', '2026-05-19 19:41:41', 1, 1);
INSERT INTO `flower` VALUES (12, '红掌', 11, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg', '红掌', 1, '2026-05-19 19:42:06', '2026-05-19 19:42:06', 1, 1);
INSERT INTO `flower` VALUES (13, '蝴蝶兰', 11, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cb34b0d2-e6b3-468d-ae12-47e0446fc07a.jpg', '蝴蝶兰', 1, '2026-05-19 19:42:52', '2026-05-19 19:42:52', 1, 1);
INSERT INTO `flower` VALUES (14, '彩菊', 11, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/1c015028-e165-4dba-b5bd-261076f85a14.jpg', '菊花', 1, '2026-05-19 19:43:49', '2026-05-19 19:43:49', 1, 1);
INSERT INTO `flower` VALUES (15, '君子兰', 11, 25.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/a8fc64d0-a6f6-4d12-a71a-7531e94cc1eb.jpg', '君子兰', 0, '2026-05-19 19:44:37', '2026-05-19 19:44:37', 1, 1);
INSERT INTO `flower` VALUES (16, '康乃馨', 6, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg', '康乃馨', 1, '2026-05-19 19:45:09', '2026-05-19 19:45:09', 1, 1);
INSERT INTO `flower` VALUES (17, '满天星', 11, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png', '满天星', 1, '2026-05-19 19:45:33', '2026-05-19 19:45:33', 1, 1);
INSERT INTO `flower` VALUES (19, '玫瑰海棠', 11, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg', '玫瑰海棠', 1, '2026-05-19 19:46:45', '2026-05-19 19:46:45', 1, 1);
INSERT INTO `flower` VALUES (20, '玫瑰花束', 6, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg', '玫瑰花束', 1, '2026-05-19 19:47:09', '2026-05-19 19:47:09', 1, 1);
INSERT INTO `flower` VALUES (21, '蔷薇', 9, 50.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg', '蔷薇', 1, '2026-05-19 19:47:30', '2026-05-19 19:47:30', 1, 1);
INSERT INTO `flower` VALUES (24, '水仙花', 11, 40.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/43c9585e-24f4-484c-9bbd-6121ca7ebb89.jpg', '水仙花', 1, '2026-05-19 19:48:43', '2026-05-19 19:48:43', 1, 1);
INSERT INTO `flower` VALUES (25, '月季', 9, 100.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg', '月季', 1, '2026-05-19 19:49:04', '2026-05-19 19:49:04', 1, 1);
INSERT INTO `flower` VALUES (32, '[促销]月季', 9, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg', '月季', 1, '2026-05-24 14:22:12', '2026-05-24 14:22:12', 1, 1);
INSERT INTO `flower` VALUES (33, '[促销]玫瑰海棠', 11, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg', '玫瑰海棠', 1, '2026-05-24 14:23:38', '2026-05-24 14:23:38', 1, 1);
INSERT INTO `flower` VALUES (34, '[促销]情人节花束', 6, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg', '情人节', 1, '2026-05-24 14:45:00', '2026-05-24 14:45:00', 1, 1);
INSERT INTO `flower` VALUES (35, '[促销]情人节花束', 6, 233.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg', '情人节', 1, '2026-05-24 14:50:16', '2026-05-24 14:50:16', 1, 1);
INSERT INTO `flower` VALUES (36, '[促销]月季', 9, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg', '月季', 1, '2026-05-24 14:51:33', '2026-05-24 14:51:33', 1, 1);
INSERT INTO `flower` VALUES (37, '[促销]满天星', 11, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png', '满天星', 1, '2026-05-24 14:51:48', '2026-05-24 14:51:48', 1, 1);
INSERT INTO `flower` VALUES (39, '[促销]蔷薇', 9, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/1eb1b3d4-b04a-47f7-b8da-58c3ebadaa88.jpg', '虹彩蔷薇', 1, '2026-05-24 16:19:05', '2026-05-24 16:19:05', 1, 1);
INSERT INTO `flower` VALUES (40, '[促销]满天星', 11, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png', '满天星', 1, '2026-05-24 16:19:47', '2026-05-24 16:19:47', 1, 1);
INSERT INTO `flower` VALUES (43, '[促销]君子兰', 11, 18.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/a8fc64d0-a6f6-4d12-a71a-7531e94cc1eb.jpg', '君子兰', 1, '2026-05-28 11:42:40', '2026-05-28 11:42:40', 1, 1);
INSERT INTO `flower` VALUES (45, '[促销]月季', 9, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg', '月季', 1, '2026-06-05 14:39:21', '2026-06-05 14:39:21', 1, 1);
INSERT INTO `flower` VALUES (46, '[促销]满天星', 11, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png', '满天星', 1, '2026-06-05 14:39:29', '2026-06-05 14:39:29', 1, 1);
INSERT INTO `flower` VALUES (47, '[促销]君子兰', 11, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/a8fc64d0-a6f6-4d12-a71a-7531e94cc1eb.jpg', '君子兰', 1, '2026-06-05 14:39:37', '2026-06-05 14:39:37', 1, 1);
INSERT INTO `flower` VALUES (48, '[促销]多肉', 14, 8.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg', '肉肉', 1, '2026-06-05 14:39:48', '2026-06-05 14:39:48', 1, 1);
INSERT INTO `flower` VALUES (49, '[促销]白掌', 11, 10.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/4faede0d-cd5f-4aa8-81b5-be157046668a.jpg', '白毛浮绿水', 1, '2026-06-05 14:39:57', '2026-06-05 14:39:57', 1, 1);
INSERT INTO `flower` VALUES (50, '山茶花', 9, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/10fbc3ae-d5a8-4f0a-9a4f-c69baaecc0d0.jpg', '', NULL, '2026-06-14 11:47:23', '2026-06-14 11:47:23', 1, 1);

-- ----------------------------
-- Table structure for order_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `flower_id` bigint NULL DEFAULT NULL COMMENT '鲜花ID',
  `number` int NULL DEFAULT NULL COMMENT '数量',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 211 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_detail
-- ----------------------------
INSERT INTO `order_detail` VALUES (1, '玫瑰海棠', 3, 19, 2, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (2, '多肉', 3, 3, 2, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (3, '月季', 3, 25, 3, 100.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (4, '[促销]满天星', 3, 40, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (5, '[促销]蔷薇', 3, 39, 2, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (6, '蔷薇', 3, 21, 1, 50.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (7, '[促销]月季', 3, 36, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (8, '玫瑰', 3, 1, 4, 60.01, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/ea575cdf-ac9a-4105-9cfe-3cdc2ab0edf7.jpg');
INSERT INTO `order_detail` VALUES (9, '[促销]康乃馨', 3, 38, 4, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (10, '[促销]康乃馨', 4, 38, 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (11, '[促销]月季180', 4, 41, 1, 48.29, 'https://loremflickr.com/400/400?lock=728997696122934');
INSERT INTO `order_detail` VALUES (12, '[促销]蔷薇', 4, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (13, '[促销]满天星', 4, 40, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (14, '[促销]月季', 5, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (15, '[促销]情人节花束', 5, 34, 2, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (16, '[促销]蔷薇', 5, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (17, '[促销]蔷薇', 6, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (18, '[促销]康乃馨', 7, 38, 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (19, '[促销]蔷薇', 7, 39, 2, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (20, '[促销]满天星', 7, 40, 4, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (21, '[促销]蔷薇', 8, 39, 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (22, '[促销]蔷薇', 9, 39, 5, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (23, '[促销]蔷薇', 10, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (24, '[促销]康乃馨', 11, 38, 3, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (25, '[促销]蔷薇', 12, 39, 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (26, '[促销]情人节花束', 13, 34, 4, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (27, '[促销]蔷薇', 14, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (28, '[促销]月季', 15, 36, 4, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (29, '[促销]月季', 16, 36, 4, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (30, '[促销]蔷薇', 17, 39, 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (31, '[促销]蔷薇', 18, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (32, '[促销]月季', 19, 36, 4, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (33, '[促销]康乃馨', 20, 38, 5, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (34, '[促销]月季', 21, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (35, '[促销]蔷薇', 22, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (36, '[促销]蔷薇', 23, 39, 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (37, '[促销]情人节花束', 24, 34, 4, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (38, '[促销]情人节花束', 25, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (39, '[促销]蔷薇', 26, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (40, '[促销]蔷薇', 27, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (41, '[促销]月季', 28, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (42, '[促销]康乃馨', 29, 38, 4, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (43, '[促销]月季', 30, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (44, '[促销]月季', 31, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (45, '[促销]情人节花束', 32, 34, 2, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (46, '[促销]月季180', 33, 41, 1, 48.29, 'https://loremflickr.com/400/400?lock=728997696122934');
INSERT INTO `order_detail` VALUES (47, '[促销]月季', 33, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (48, '[促销]情人节花束', 34, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (49, '[促销]满天星', 35, 40, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (50, '[促销]满天星', 35, 40, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (51, '[促销]月季', 36, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (52, '[促销]情人节花束', 37, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (53, '[促销]情人节花束', 38, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (54, '[促销]月季', 39, 36, 4, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (55, '[促销]月季', 40, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (56, '[促销]蔷薇', 41, 39, 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (57, '[促销]蔷薇', 41, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (58, '[促销]月季', 42, 36, 4, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (59, '[促销]情人节花束', 43, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (60, '[促销]月季', 44, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (61, '[促销]蔷薇', 45, 39, 2, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (62, '[促销]蔷薇', 45, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (63, '[促销]蔷薇', 46, 39, 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (64, '[促销]情人节花束', 47, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (65, '[促销]月季', 48, 32, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (66, '[促销]情人节花束', 48, 34, 2, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (67, '[促销]蔷薇', 48, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (68, '[促销]蔷薇', 49, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (69, '[促销]月季180', 50, 41, 1, 48.29, 'https://loremflickr.com/400/400?lock=728997696122934');
INSERT INTO `order_detail` VALUES (70, '[促销]情人节花束', 50, 35, 1, 233.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (71, '[促销]月季', 50, 32, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (72, '[促销]玫瑰海棠', 50, 33, 1, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (73, '[促销]满天星', 50, 40, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (74, '[促销]月季', 50, 36, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (75, '[促销]蔷薇', 50, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (76, '[促销]情人节花束', 50, 34, 1, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (77, '[促销]情人节花束', 51, 34, 1, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (78, '[促销]月季', 52, 36, 4, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (79, '月季', 53, 25, 1, 100.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (80, '水仙花', 53, 24, 1, 40.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/43c9585e-24f4-484c-9bbd-6121ca7ebb89.jpg');
INSERT INTO `order_detail` VALUES (81, '[促销]月季', 53, 32, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (82, '[促销]情人节花束', 53, 35, 1, 233.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (83, '[促销]月季', 53, 36, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (84, '[促销]康乃馨', 53, 38, 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (85, '[促销]蔷薇', 53, 39, 2, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (86, '[促销]蔷薇', 54, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (87, '[促销]月季', 54, 36, 2, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (88, '[促销]情人节花束', 54, 34, 2, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (89, '[促销]月季', 55, 36, 2, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (90, '[促销]满天星', 56, 37, 2, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (91, '[促销]康乃馨', 56, 38, 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (92, '[促销]月季', 56, 36, 2, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (93, '[促销]蔷薇', 56, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (94, '[促销]情人节花束', 56, 35, 2, 233.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (95, '[促销]满天星', 56, 40, 3, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (96, '[促销]月季', 57, 32, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (97, '[促销]蔷薇', 57, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (98, '[促销]满天星', 57, 40, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (99, '[促销]情人节花束', 57, 34, 1, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (100, '[促销]月季', 57, 36, 1, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (101, '[促销]情人节花束', 58, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (102, '[促销]满天星', 58, 40, 1, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (103, '[促销]蔷薇', 58, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (104, '[促销]蔷薇', 59, 39, 4, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (105, '[促销]情人节花束', 60, 34, 1, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (106, '[促销]蔷薇', 61, 39, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (107, '[促销]康乃馨', 61, 38, 2, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (108, '[促销]月季', 62, 36, 2, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (109, '[促销]君子兰', 63, 43, 1, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/a8fc64d0-a6f6-4d12-a71a-7531e94cc1eb.jpg');
INSERT INTO `order_detail` VALUES (110, '[促销]月季', 63, 36, 3, 80.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (111, '[促销]康乃馨', 64, 38, 9, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (112, '[促销]康乃馨', 64, 38, 1, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (113, '[促销]情人节花束', 65, 35, 1, 233.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (114, '红掌', 66, 12, 3, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (115, '[促销]满天星', 66, 37, 1, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (116, '毕业花束', 67, 11, 4, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (117, '[促销]康乃馨', 68, 38, 4, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (118, '[促销]玫瑰海棠', 68, 33, 1, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (119, '[促销]情人节花束', 68, 34, 1, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (120, '[促销]满天星', 69, 40, 5, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (121, '红掌', 70, 12, 8, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (122, '多肉', 71, 3, 5, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (123, '[促销]满天星', 72, 40, 5, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (124, '[促销]满天星', 73, 40, 5, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (125, '[促销]满天星', 74, 40, 5, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (126, '[促销]蔷薇', 75, 44, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (127, '蔷薇', 76, 21, 2, 50.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (128, '[促销]玫瑰海棠', 77, 33, 1, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (129, '毕业花束', 78, 11, 1, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (130, '多肉', 78, 3, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (131, '红掌', 78, 12, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (132, '玫瑰海棠', 78, 19, 2, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (133, '满天星', 78, 17, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (134, '[促销]康乃馨', 79, 38, 3, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (135, '康乃馨', 79, 16, 2, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (136, '满天星', 79, 17, 3, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (137, '多肉', 80, 3, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (138, '毕业花束', 80, 11, 1, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (139, '玫瑰海棠', 80, 19, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (140, '玫瑰花束', 80, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (141, '红掌', 80, 12, 3, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (142, '[促销]蔷薇', 81, 44, 3, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (143, '[促销]月季180', 82, 41, 2, 48.29, 'https://loremflickr.com/400/400?lock=728997696122934');
INSERT INTO `order_detail` VALUES (144, '毕业花束', 83, 11, 3, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (145, '多肉', 83, 3, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (146, '多肉', 83, 3, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (147, '康乃馨', 84, 16, 5, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (148, '水仙花', 85, 24, 2, 40.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/43c9585e-24f4-484c-9bbd-6121ca7ebb89.jpg');
INSERT INTO `order_detail` VALUES (149, '蔷薇', 85, 21, 4, 50.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (150, '康乃馨', 86, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (151, '多肉', 87, 3, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (152, '[促销]蔷薇', 88, 44, 1, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (153, '玫瑰花束', 88, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (154, '月季', 88, 25, 1, 100.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (155, '满天星', 88, 17, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (156, '玫瑰海棠', 88, 19, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (157, '水仙花', 88, 24, 2, 40.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/43c9585e-24f4-484c-9bbd-6121ca7ebb89.jpg');
INSERT INTO `order_detail` VALUES (158, '康乃馨', 89, 16, 2, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (159, '红掌', 89, 12, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (160, '玫瑰花束', 90, 20, 4, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (161, '多肉', 91, 3, 3, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (162, '康乃馨', 92, 16, 3, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (163, '[促销]蔷薇', 93, 39, 2, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (164, '[促销]满天星', 93, 40, 3, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (165, '多肉', 94, 3, 2, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (166, '毕业花束', 94, 11, 4, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (167, '玫瑰海棠', 95, 19, 3, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (168, '红掌', 96, 12, 2, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (169, '毕业花束', 96, 11, 6, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (170, '多肉', 97, 3, 11, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (171, '月季', 98, 25, 10, 100.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8e8d9e91-fb74-4eea-ae88-b179e37ec32b.jpg');
INSERT INTO `order_detail` VALUES (172, '水仙花', 98, 24, 2, 40.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/43c9585e-24f4-484c-9bbd-6121ca7ebb89.jpg');
INSERT INTO `order_detail` VALUES (173, '玫瑰花束', 98, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (174, '玫瑰海棠', 98, 19, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (175, '蔷薇', 98, 21, 2, 50.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg');
INSERT INTO `order_detail` VALUES (176, '[促销]白掌', 99, 49, 7, 10.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/4faede0d-cd5f-4aa8-81b5-be157046668a.jpg');
INSERT INTO `order_detail` VALUES (177, '[促销]情人节花束', 99, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (178, '多肉', 100, 3, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (179, '毕业花束', 100, 11, 1, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (180, '红掌', 100, 12, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (181, '康乃馨', 100, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (182, '满天星', 100, 17, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (183, '毕业花束', 101, 11, 1, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (184, '康乃馨', 101, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (185, '玫瑰花束', 101, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (186, '[促销]情人节花束', 101, 34, 3, 244.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/8574d39e-8336-4aa0-a728-450b7dd2885c.jpg');
INSERT INTO `order_detail` VALUES (187, '多肉', 102, 3, 1, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (188, '毕业花束', 102, 11, 1, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg');
INSERT INTO `order_detail` VALUES (189, '红掌', 102, 12, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (190, '康乃馨', 102, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (191, '满天星', 102, 17, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (192, '玫瑰海棠', 103, 19, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (193, '玫瑰花束', 103, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (194, '玫瑰海棠', 104, 19, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (195, '玫瑰花束', 104, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (196, '玫瑰海棠', 105, 19, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg');
INSERT INTO `order_detail` VALUES (197, '玫瑰花束', 105, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (198, '康乃馨', 107, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (199, '红掌', 107, 12, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (200, '满天星', 107, 17, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (201, '康乃馨', 108, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (202, '红掌', 108, 12, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (203, '满天星', 108, 17, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (204, '康乃馨', 109, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (205, '红掌', 109, 12, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg');
INSERT INTO `order_detail` VALUES (206, '满天星', 109, 17, 1, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');
INSERT INTO `order_detail` VALUES (207, '[促销]多肉', 110, 48, 2, 8.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg');
INSERT INTO `order_detail` VALUES (208, '康乃馨', 111, 16, 1, 88.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg');
INSERT INTO `order_detail` VALUES (209, '玫瑰花束', 112, 20, 1, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/0219d7d3-b180-4402-b804-a149ef7032e6.jpg');
INSERT INTO `order_detail` VALUES (210, '[促销]满天星', 112, 46, 1, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  `status` int NULL DEFAULT NULL COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
  `user_id` bigint NULL DEFAULT NULL COMMENT '下单用户ID',
  `address_book_id` bigint NULL DEFAULT NULL COMMENT '地址簿ID',
  `order_time` datetime NULL DEFAULT NULL COMMENT '下单时间',
  `checkout_time` datetime NULL DEFAULT NULL COMMENT '结账时间',
  `pay_method` int NULL DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
  `pay_status` int NULL DEFAULT NULL COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '实收金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `user_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `consignee` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `rejection_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `estimated_delivery_time` datetime NULL DEFAULT NULL COMMENT '预计送达时间',
  `delivery_status` int NULL DEFAULT NULL COMMENT '配送状态 1立即送出 0选择具体时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '送达时间',
  `pack_amount` int NULL DEFAULT 0 COMMENT '打包费',
  `delivery_fee` int NULL DEFAULT 0 COMMENT '配送费',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 113 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (85, '20260530-0009', 7, 9, 5, '2026-05-30 16:39:52', '2026-05-30 16:39:52', 1, 0, 290.00, '', NULL, '13132323532', '佛山大学', '李四', '库存不足：缺货', NULL, '2026-05-30 19:01:22', '2026-06-01 16:39:52', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (86, '20260530-0010', 6, 9, 5, '2026-05-30 16:40:15', '2026-05-30 16:40:16', 1, 1, 108.00, '', NULL, '13132323532', '佛山大学', '李四', '6|||用户申请退货', '1', NULL, '2026-06-01 16:40:15', 1, '2026-05-31 21:02:55', 0, 0);
INSERT INTO `orders` VALUES (87, '20260530-0011', 5, 9, 5, '2026-05-30 16:40:33', '2026-05-30 16:40:34', 1, 1, 35.00, '', NULL, '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-01 16:40:33', 1, '2026-06-01 20:19:15', 0, 0);
INSERT INTO `orders` VALUES (88, '20260530-0012', 3, 9, 5, '2026-05-30 16:41:47', '2026-05-30 16:41:48', 1, 1, 486.00, '', NULL, '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-01 16:41:47', 1, NULL, 0, 0);
INSERT INTO `orders` VALUES (89, '20260530-0013', 1, 9, 5, '2026-05-30 16:55:36', '2026-05-30 16:55:36', 1, 0, 770.45, 'esse laboris dolor incididunt tempor', '9527', '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-01 16:55:36', 1, NULL, 10, 27);
INSERT INTO `orders` VALUES (90, '20260530-0014', 7, 9, 5, '2026-05-30 20:09:50', '2026-05-30 20:09:50', 1, 0, 762.00, '', '9527', '13132323532', '佛山大学', '李四', '用户取消', NULL, '2026-05-30 20:10:00', '2026-06-01 20:09:50', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (91, '20260531-0001', 5, 9, 5, '2026-05-31 11:35:25', '2026-05-31 11:36:06', 1, 1, 65.00, '', '9527', '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-02 11:35:25', 1, '2026-06-01 20:21:50', 0, 0);
INSERT INTO `orders` VALUES (92, '20260531-0002', 5, 7, 3, '2026-05-31 11:46:15', '2026-05-31 11:47:00', 1, 1, 274.00, '', '风吹月满楼', '13121212123', '提瓦特', '张三', NULL, NULL, NULL, '2026-06-02 11:46:15', 1, '2026-06-01 20:22:28', 0, 0);
INSERT INTO `orders` VALUES (93, '20260531-0003', 5, 7, 2, '2026-05-31 11:47:12', '2026-06-01 20:23:18', 1, 1, 200.00, '', '风吹月满楼', '13131313123', '123', '张三', NULL, NULL, NULL, '2026-06-02 11:47:12', 1, '2026-06-01 20:29:18', 0, 0);
INSERT INTO `orders` VALUES (94, '20260601-0001', 4, 9, 5, '2026-06-01 20:20:51', '2026-06-01 20:21:40', 1, 1, 1064.00, '', '9527', '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-03 20:20:51', 1, '2026-06-01 20:26:27', 0, 0);
INSERT INTO `orders` VALUES (95, '20260601-0002', 5, 7, 2, '2026-06-01 20:25:47', '2026-06-01 20:26:20', 1, 1, 110.00, '', '风吹月满楼', '13131313123', '123', '张三', NULL, NULL, NULL, '2026-06-03 20:25:47', 1, '2026-06-01 20:29:00', 0, 0);
INSERT INTO `orders` VALUES (96, '20260601-0003', 4, 7, 2, '2026-06-01 20:29:33', '2026-06-01 20:29:59', 1, 1, 1606.00, '', '风吹月满楼', '13131313123', '123', '张三', NULL, NULL, NULL, '2026-06-03 20:29:33', 1, '2026-06-01 20:33:24', 0, 0);
INSERT INTO `orders` VALUES (97, '20260601-0004', 5, 7, 3, '2026-06-01 20:30:25', '2026-06-01 20:32:06', 1, 1, 185.00, '', '风吹月满楼', '13121212123', '提瓦特', '张三', NULL, NULL, NULL, '2026-06-03 20:30:25', 1, '2026-06-01 20:33:28', 0, 0);
INSERT INTO `orders` VALUES (98, '20260601-0005', 5, 7, 2, '2026-06-01 20:34:07', '2026-06-01 20:34:08', 1, 1, 1408.00, '', '风吹月满楼', '13131313123', '123', '张三', NULL, NULL, NULL, '2026-06-03 20:34:07', 1, '2026-06-01 20:34:16', 0, 0);
INSERT INTO `orders` VALUES (99, '20260605-0001', 5, 9, 5, '2026-06-05 16:01:41', '2026-06-05 16:01:44', 1, 1, 812.00, '', '9527', '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-07 16:01:41', 1, '2026-06-05 16:02:09', 0, 0);
INSERT INTO `orders` VALUES (100, '20260606-0001', 1, 4, 8, '2026-06-06 22:19:43', '2026-06-06 22:19:43', 1, 0, 429.00, 'AI导购推荐方案', '张三', '14423243234', '213', '张三', NULL, NULL, NULL, '2026-06-08 22:19:43', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (101, '20260606-0002', 1, 4, 8, '2026-06-06 22:22:37', '2026-06-06 22:22:37', 1, 0, 1274.00, '', '张三', '14423243234', '213', '张三', NULL, NULL, NULL, '2026-06-08 22:22:37', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (102, '20260606-0003', 1, 4, 8, '2026-06-06 22:25:21', '2026-06-06 22:25:21', 1, 0, 429.00, 'AI导购推荐方案', '张三', '14423243234', '213', '张三', NULL, NULL, NULL, '2026-06-08 22:25:21', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (103, '20260607-0001', 1, 4, 8, '2026-06-07 10:38:37', '2026-06-07 10:38:37', 1, 0, 228.00, 'AI导购推荐方案', '张三', '14423243234', '213', '张三', NULL, NULL, NULL, '2026-06-09 10:38:37', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (104, '20260607-0002', 1, 4, 8, '2026-06-07 10:38:48', '2026-06-07 10:38:48', 1, 0, 228.00, 'AI导购推荐方案', '张三', '14423243234', '213', '张三', NULL, NULL, NULL, '2026-06-09 10:38:48', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (105, '20260607-0003', 2, 4, 8, '2026-06-07 10:39:01', '2026-06-07 10:39:42', 1, 1, 228.00, 'AI导购推荐方案', '张三', '14423243234', '213', '张三', NULL, NULL, NULL, '2026-06-09 10:39:01', 1, NULL, 10, 0);
INSERT INTO `orders` VALUES (107, '20260613-0001', 1, 9, 5, '2026-06-13 12:02:58', '2026-06-13 12:02:58', 1, 0, 168.00, 'AI导购推荐方案', '9527', '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-15 12:02:58', 1, NULL, 10, 10);
INSERT INTO `orders` VALUES (108, '20260613-0002', 2, 9, 5, '2026-06-13 12:46:17', '2026-06-13 13:07:02', 1, 1, 168.00, 'AI导购推荐方案', '9527', '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-15 12:46:17', 1, NULL, 10, 10);
INSERT INTO `orders` VALUES (109, '20260613-0003', 4, 9, 5, '2026-06-13 13:05:38', '2026-06-13 13:05:39', 1, 1, 168.00, 'AI导购推荐方案', '9527', '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-06-15 13:05:38', 1, '2026-06-13 13:07:16', 0, 0);
INSERT INTO `orders` VALUES (110, '20260613-0004', 8, 9, 5, '2026-06-13 13:06:09', '2026-06-13 13:06:10', 1, 1, 36.00, '', '9527', '13132323532', '佛山大学', '李四', '4|||用户申请退货', NULL, NULL, '2026-06-15 13:06:09', 1, '2026-06-13 13:07:15', 0, 0);
INSERT INTO `orders` VALUES (111, '20260613-0005', 2, 7, 2, '2026-06-13 20:27:07', '2026-06-13 20:27:08', 1, 1, 108.00, 'AI导购推荐方案', '风吹月满楼', '13131313123', '123', '张三', NULL, NULL, NULL, '2026-06-15 20:27:07', 1, NULL, 10, 10);
INSERT INTO `orders` VALUES (112, '20260613-0006', 1, 7, 2, '2026-06-13 20:29:39', '2026-06-13 20:29:39', 1, 0, 218.00, 'AI导购推荐方案', '风吹月满楼', '13131313123', '123', '张三', NULL, NULL, NULL, '2026-06-15 20:29:39', 1, NULL, 10, 0);

-- ----------------------------
-- Table structure for participation
-- ----------------------------
DROP TABLE IF EXISTS `participation`;
CREATE TABLE `participation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与ID',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '活动ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `quantity` int NULL DEFAULT NULL COMMENT '数量',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动参与表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of participation
-- ----------------------------

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `flower_id` bigint NULL DEFAULT NULL COMMENT '鲜花ID',
  `number` int NULL DEFAULT NULL COMMENT '数量',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 357 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别 0女 1男',
  `id_number` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `create_time` datetime NULL DEFAULT NULL COMMENT '注册时间',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '123', '123', '1', '123', '132', '2026-05-13 19:59:44', '222', '123');
INSERT INTO `user` VALUES (2, '456', '456', '0', '456', '456', '2026-05-20 20:07:58', '456', '456');
INSERT INTO `user` VALUES (4, '张三', '132', '1', '123', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/ff3c2f87-d761-430d-9c19-51164bf3a762.jpg', '2026-05-23 18:33:16', '123', '12345');
INSERT INTO `user` VALUES (6, 'Jin', '13535121212', NULL, NULL, NULL, '2026-05-23 20:58:43', '281872', '123456');
INSERT INTO `user` VALUES (7, '风吹月满楼', '13131412151', '1', '10086', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/788d0616-9be4-47e4-af54-44c20427275b.jpg', '2026-05-24 11:44:22', '10086', '10086');
INSERT INTO `user` VALUES (9, '9527', '13131215181', '1', '123', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c7232a81-cee3-4d5f-938d-ae09bcf87722.gif', '2026-05-26 20:52:37', '9527', '10086');
INSERT INTO `user` VALUES (10, '纳西妲', '14514232123', NULL, NULL, NULL, '2026-06-01 20:37:55', '123456', '123456');
INSERT INTO `user` VALUES (11, '阿帽', '15234323432', NULL, NULL, NULL, '2026-06-01 20:39:31', '654321', '654321');
INSERT INTO `user` VALUES (12, '英雄形态的宵宫姐姐', '15142323432', NULL, NULL, NULL, '2026-06-01 20:40:28', '111111', '111111');
INSERT INTO `user` VALUES (13, '王五', '15115121213', NULL, NULL, NULL, '2026-06-13 13:08:12', '1234567', '1234567');

-- ----------------------------
-- Table structure for user_agent_session
-- ----------------------------
DROP TABLE IF EXISTS `user_agent_session`;
CREATE TABLE `user_agent_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否活跃：1=活跃 0=已删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_active`(`user_id` ASC, `is_active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI导购会话记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_agent_session
-- ----------------------------
INSERT INTO `user_agent_session` VALUES (26, 9, '新对话', 0, '2026-06-11 16:30:26', '2026-06-12 16:32:59');
INSERT INTO `user_agent_session` VALUES (27, 9, '我想买花送给女朋友，有什么推荐？', 0, '2026-06-11 16:30:46', '2026-06-12 16:32:58');
INSERT INTO `user_agent_session` VALUES (28, 9, '有没有促销活动？帮我搭配一个省钱方案', 0, '2026-06-11 21:58:05', '2026-06-12 16:32:56');
INSERT INTO `user_agent_session` VALUES (29, 9, '母亲节快到了，想给妈妈买花', 0, '2026-06-12 13:40:35', '2026-06-12 16:32:55');
INSERT INTO `user_agent_session` VALUES (30, 9, '你好', 0, '2026-06-12 15:28:12', '2026-06-12 16:32:53');
INSERT INTO `user_agent_session` VALUES (31, 9, '母亲节快到了，想给妈妈买花', 1, '2026-06-12 16:33:00', '2026-06-12 16:33:15');
INSERT INTO `user_agent_session` VALUES (32, 9, '我想买花送给女朋友，有什么推荐？', 0, '2026-06-12 19:26:17', '2026-06-12 20:19:11');
INSERT INTO `user_agent_session` VALUES (33, 9, '我想买花送给女朋友，有什么推荐？', 0, '2026-06-12 19:44:43', '2026-06-12 19:48:38');
INSERT INTO `user_agent_session` VALUES (34, 9, '母亲节快到了，想给妈妈买花', 1, '2026-06-12 19:45:08', '2026-06-13 12:05:58');
INSERT INTO `user_agent_session` VALUES (35, 9, '新会话', 0, '2026-06-12 19:48:32', '2026-06-12 19:48:36');
INSERT INTO `user_agent_session` VALUES (36, 9, '恋人', 1, '2026-06-12 20:11:53', '2026-06-13 12:05:58');
INSERT INTO `user_agent_session` VALUES (37, 9, '母亲节快到了，想给妈妈买花', 1, '2026-06-12 20:20:49', '2026-06-13 11:45:15');
INSERT INTO `user_agent_session` VALUES (38, 9, '我想装饰家里', 1, '2026-06-12 20:22:37', '2026-06-13 12:05:57');
INSERT INTO `user_agent_session` VALUES (39, 9, '新会话', 1, '2026-06-12 20:58:40', '2026-06-13 11:45:16');
INSERT INTO `user_agent_session` VALUES (40, 7, '你好', 0, '2026-06-13 13:07:36', '2026-06-13 16:43:30');
INSERT INTO `user_agent_session` VALUES (41, 13, '新会话', 1, '2026-06-13 13:08:15', '2026-06-13 13:08:15');
INSERT INTO `user_agent_session` VALUES (42, 7, '母亲节快到了，想给妈妈买花', 0, '2026-06-13 16:43:39', '2026-06-13 16:47:28');
INSERT INTO `user_agent_session` VALUES (43, 7, '母亲节快到了，想给妈妈买花', 0, '2026-06-13 16:45:25', '2026-06-13 19:11:32');
INSERT INTO `user_agent_session` VALUES (44, 7, '母亲节快到了，想给妈妈买花', 0, '2026-06-13 18:42:18', '2026-06-13 19:11:31');
INSERT INTO `user_agent_session` VALUES (45, 7, '母亲节快到了，想给妈妈买花', 0, '2026-06-13 19:00:54', '2026-06-13 19:10:42');
INSERT INTO `user_agent_session` VALUES (46, 7, '母亲节快到了，想给妈妈买花', 1, '2026-06-13 19:11:33', '2026-06-13 20:17:18');
INSERT INTO `user_agent_session` VALUES (47, 7, '母亲节快到了，想给妈妈买花', 1, '2026-06-13 20:25:49', '2026-06-13 20:28:29');
INSERT INTO `user_agent_session` VALUES (48, 7, '我想买花送给女朋友，有什么推荐？', 1, '2026-06-13 20:29:43', '2026-06-13 20:47:39');
INSERT INTO `user_agent_session` VALUES (49, 7, '母亲节快到了，想给妈妈买花', 0, '2026-06-13 21:02:15', '2026-06-13 21:03:30');
INSERT INTO `user_agent_session` VALUES (50, 7, '我想买花送给女朋友，有什么推荐？', 1, '2026-06-13 21:03:31', '2026-06-13 21:20:31');

-- ----------------------------
-- Table structure for user_comment
-- ----------------------------
DROP TABLE IF EXISTS `user_comment`;
CREATE TABLE `user_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户点赞记录ID',
  `comment_id` bigint NULL DEFAULT NULL COMMENT '评论ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_comment`(`user_id` ASC, `comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户点赞评论记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_comment
-- ----------------------------
INSERT INTO `user_comment` VALUES (14, 20, 7);
INSERT INTO `user_comment` VALUES (2, 1, 9);
INSERT INTO `user_comment` VALUES (9, 10, 9);

SET FOREIGN_KEY_CHECKS = 1;
