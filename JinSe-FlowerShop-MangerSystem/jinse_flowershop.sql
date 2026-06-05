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

 Date: 30/05/2026 15:07:24
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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (5, '2026-05-13 00:00:00', '2026-05-28 00:00:00', 1, '520', 1, '2026-05-20 20:16:11', '2026-05-21 10:36:42', 1, 1);
INSERT INTO `activity` VALUES (7, '2026-05-31 00:00:00', '2026-06-30 00:00:00', 1, '毕业季', 5, '2026-05-21 10:38:35', '2026-05-29 19:26:51', 1, 1);
INSERT INTO `activity` VALUES (8, '2026-04-29 14:30:54', '2027-09-18 08:08:57', 1, 'sed dolore non', 40, '2026-05-21 20:51:12', '2026-05-23 15:35:42', 1, 1);
INSERT INTO `activity` VALUES (9, '2026-05-21 00:00:00', '2026-05-27 00:00:00', 1, '毕业大礼包', 5, '2026-05-21 21:29:02', '2026-05-22 21:58:54', 1, 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动销售表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_sale
-- ----------------------------
INSERT INTO `activity_sale` VALUES (37, 5, 32, 100.00, 80.00, 110, 0, '2026-05-24 14:22:12', '2026-05-24 14:22:12', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (38, 5, 33, 30.00, 20.00, 221, 1, '2026-05-24 14:23:38', '2026-05-24 14:23:38', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (39, 9, 34, 250.00, 244.00, 109, 1, '2026-05-24 14:45:00', '2026-05-24 14:45:00', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (40, 7, 35, 250.00, 233.00, 109, 1, '2026-05-24 14:50:16', '2026-05-24 14:50:16', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (41, 9, 36, 100.00, 80.00, 1105, 5, '2026-05-24 14:51:33', '2026-05-24 14:51:33', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (42, 7, 37, 30.00, 20.00, 110, 1, '2026-05-24 14:51:48', '2026-05-24 14:51:48', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (43, 8, 38, 88.00, 68.00, 1096, 14, '2026-05-24 14:52:10', '2026-05-24 14:52:10', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (44, 9, 39, 50.00, 48.00, 55, 0, '2026-05-24 16:19:06', '2026-05-24 16:19:06', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (45, 9, 40, 30.00, 28.00, 234, 10, '2026-05-24 16:19:47', '2026-05-24 16:19:47', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (46, 8, 41, 240.99, 48.29, 47, 69, '2026-05-24 16:25:14', '2026-05-24 16:44:14', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (48, 9, 43, 25.00, 20.00, 0, 1, '2026-05-28 11:42:40', '2026-05-28 11:42:40', 1, 1, 0);
INSERT INTO `activity_sale` VALUES (49, 9, 44, 50.00, 48.00, 4, 1, '2026-05-29 19:27:29', '2026-05-29 19:27:29', 1, 1, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地址簿表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of address_book
-- ----------------------------
INSERT INTO `address_book` VALUES (2, 7, '张三', '13131313123', '130000', '河北省', '130200', '唐山市', '130204', '古冶区', '123', '公司', 1);
INSERT INTO `address_book` VALUES (3, 7, '123', '13121212123', '440000', '广东省', '440300', '深圳市', '440305', '南山区', '123', '学校', 0);
INSERT INTO `address_book` VALUES (4, 7, '这些', '13132323234', '440000', '广东省', '440100', '广州市', '440103', '荔湾区', '12', '家', 0);
INSERT INTO `address_book` VALUES (5, 9, '李四', '13132323532', '440000', '广东省', '440600', '佛山市', '440605', '南海区', '佛山大学', '学校', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, NULL, 40, 5, '很好', '2026-05-27 21:18:52', 1, 0);
INSERT INTO `comment` VALUES (2, NULL, 39, 2, '一般般', '2026-05-27 21:19:38', 0, 0);
INSERT INTO `comment` VALUES (3, NULL, 39, 4, '还行\n', '2026-05-28 10:19:31', 0, 0);
INSERT INTO `comment` VALUES (4, NULL, 39, 5, 'ok', '2026-05-28 11:31:55', 0, 0);
INSERT INTO `comment` VALUES (5, 9, 40, 5, '好', '2026-05-28 15:12:31', 0, 0);
INSERT INTO `comment` VALUES (6, 9, 35, 5, '好', '2026-05-28 15:37:05', 0, 0);
INSERT INTO `comment` VALUES (7, 9, 33, 4, '一般般', '2026-05-28 21:54:09', 0, 0);
INSERT INTO `comment` VALUES (9, 9, 40, 1, '不行', '2026-05-29 12:53:40', 0, 0);
INSERT INTO `comment` VALUES (10, 9, 40, 1, '差评', '2026-05-29 12:53:45', 0, 0);
INSERT INTO `comment` VALUES (11, 9, 40, 1, '花谢了', '2026-05-29 12:53:59', 0, 0);
INSERT INTO `comment` VALUES (12, 9, 40, 5, '也就那样', '2026-05-29 12:54:04', 0, 0);
INSERT INTO `comment` VALUES (13, 9, 40, 3, '还行', '2026-05-29 12:54:14', 0, 0);
INSERT INTO `comment` VALUES (14, 9, 40, 5, '一般般', '2026-05-29 12:54:23', 0, 0);
INSERT INTO `comment` VALUES (15, 9, 40, 5, 'OK', '2026-05-29 12:54:29', 0, 0);
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
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鲜花表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flower
-- ----------------------------
INSERT INTO `flower` VALUES (1, '玫瑰', 85, 60.01, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/ea575cdf-ac9a-4105-9cfe-3cdc2ab0edf7.jpg', '玫瑰', 0, '2026-05-17 20:29:00', '2026-05-17 20:29:00', 1, 1);
INSERT INTO `flower` VALUES (3, '多肉', 14, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c69fda2f-cf44-4862-bd52-d3b665ca7e1b.jpg', '肉肉', 1, '2026-05-19 18:45:24', '2026-05-19 18:45:24', 1, 1);
INSERT INTO `flower` VALUES (9, '白掌', 11, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/4faede0d-cd5f-4aa8-81b5-be157046668a.jpg', '白毛浮绿水', 0, '2026-05-19 19:35:31', '2026-05-19 19:35:31', 1, 1);
INSERT INTO `flower` VALUES (10, '百合', 6, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/b889ea94-b2dc-4704-a4d3-9c1c99877ac7.jpg', '香水百合', 0, '2026-05-19 19:36:18', '2026-05-19 19:36:18', 1, 1);
INSERT INTO `flower` VALUES (11, '毕业花束', 6, 256.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c3187777-9081-4d6f-a7af-1fa41039c838.jpg', '毕业季', 1, '2026-05-19 19:41:41', '2026-05-19 19:41:41', 1, 1);
INSERT INTO `flower` VALUES (12, '红掌', 11, 30.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cce459a4-4d2c-4912-a8ac-2020b68ef83d.jpg', '红掌', 1, '2026-05-19 19:42:06', '2026-05-19 19:42:06', 1, 1);
INSERT INTO `flower` VALUES (13, '蝴蝶兰', 11, 188.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/cb34b0d2-e6b3-468d-ae12-47e0446fc07a.jpg', '蝴蝶兰', 0, '2026-05-19 19:42:52', '2026-05-19 19:42:52', 1, 1);
INSERT INTO `flower` VALUES (14, '彩菊', 11, 15.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/1c015028-e165-4dba-b5bd-261076f85a14.jpg', '菊花', 0, '2026-05-19 19:43:49', '2026-05-19 19:43:49', 1, 1);
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
INSERT INTO `flower` VALUES (38, '[促销]康乃馨', 6, 68.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/33d13b56-79cb-4fce-b3f2-f52dbda30751.jpg', '康乃馨', 1, '2026-05-24 14:52:10', '2026-05-24 14:52:10', 1, 1);
INSERT INTO `flower` VALUES (39, '[促销]蔷薇', 9, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg', '蔷薇', 1, '2026-05-24 16:19:05', '2026-05-24 16:19:05', 1, 1);
INSERT INTO `flower` VALUES (40, '[促销]满天星', 11, 28.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/d2ce22a3-3124-4c6c-970b-c13ed0d47adf.png', '满天星', 1, '2026-05-24 16:19:47', '2026-05-24 16:19:47', 1, 1);
INSERT INTO `flower` VALUES (41, '[促销]月季180', 9, 48.29, 'https://loremflickr.com/400/400?lock=728997696122934', '月季。', 1, '2026-05-24 16:25:14', '2026-05-24 16:25:14', 1, 1);
INSERT INTO `flower` VALUES (43, '[促销]君子兰', 11, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/a8fc64d0-a6f6-4d12-a71a-7531e94cc1eb.jpg', '君子兰', 1, '2026-05-28 11:42:40', '2026-05-28 11:42:40', 1, 1);
INSERT INTO `flower` VALUES (44, '[促销]蔷薇', 9, 48.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/18805b28-6b15-433d-ab9a-39010c628235.jpg', '蔷薇', 1, '2026-05-29 19:27:29', '2026-05-29 19:27:29', 1, 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 128 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (71, '1780053461996', 2, 9, 5, '2026-05-29 19:17:42', '2026-05-29 19:20:25', 1, 1, 85.00, '', NULL, '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-05-31 19:17:42', 1, NULL, 0, 0);
INSERT INTO `orders` VALUES (72, '1780053657033', 7, 9, 5, '2026-05-29 19:20:57', '2026-05-29 19:20:58', 1, 2, 150.00, '', NULL, '13132323532', '佛山大学', '李四', '商家同意退货', NULL, '2026-05-29 19:21:26', '2026-05-31 19:20:57', 1, NULL, 0, 0);
INSERT INTO `orders` VALUES (73, '1780053799676', 7, 9, 5, '2026-05-29 19:23:20', '2026-05-29 19:23:21', 1, 1, 150.00, '', NULL, '13132323532', '佛山大学', '李四', '库存不足', NULL, '2026-05-29 19:25:28', '2026-05-31 19:23:20', 1, NULL, 0, 0);
INSERT INTO `orders` VALUES (74, '1780053950736', 4, 9, 5, '2026-05-29 19:25:51', '2026-05-29 19:25:52', 1, 1, 150.00, '', NULL, '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-05-31 19:25:51', 1, '2026-05-29 19:26:12', 0, 0);
INSERT INTO `orders` VALUES (75, '1780054064021', 2, 9, 5, '2026-05-29 19:27:44', '2026-05-29 19:27:45', 1, 1, 58.00, '', NULL, '13132323532', '佛山大学', '李四', NULL, NULL, NULL, '2026-05-31 19:27:44', 1, NULL, 0, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 218 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------
INSERT INTO `shopping_cart` VALUES (217, '[促销]玫瑰海棠', 9, 33, 1, 20.00, 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/30cb3ace-245f-4d41-805f-a1ffa35c520f.jpg', '2026-05-30 14:37:39');

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '123', '123', '1', '123', '132', '2026-05-13 19:59:44', '222', '123');
INSERT INTO `user` VALUES (2, '456', '456', '0', '456', '456', '2026-05-20 20:07:58', '456', '456');
INSERT INTO `user` VALUES (4, '张三', '132', '1', '123', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/ff3c2f87-d761-430d-9c19-51164bf3a762.jpg', '2026-05-23 18:33:16', '123', '12345');
INSERT INTO `user` VALUES (6, 'Jin', '13535121212', NULL, NULL, NULL, '2026-05-23 20:58:43', '281872', '123456');
INSERT INTO `user` VALUES (7, '风吹月满楼', '13131412151', '1', '10086', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/83e953fd-d278-4adc-8c55-f5e264c55b13.gif', '2026-05-24 11:44:22', '10086', '10086');
INSERT INTO `user` VALUES (9, '9527', '13131215181', '1', '123', 'https://jinse-flowershop.oss-cn-beijing.aliyuncs.com/c7232a81-cee3-4d5f-938d-ae09bcf87722.gif', '2026-05-26 20:52:37', '9527', '952710086');

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户点赞评论记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_comment
-- ----------------------------
INSERT INTO `user_comment` VALUES (2, 1, 9);

SET FOREIGN_KEY_CHECKS = 1;
