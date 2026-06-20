-- ============================================================
-- 版本更新通知工具 - 数据库 DDL (MySQL 8)
-- 约定：枚举以 VARCHAR 存储枚举名(name())；统一审计字段 + 逻辑删除 deleted(0/1)
-- ============================================================
CREATE DATABASE IF NOT EXISTS release_notify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE release_notify;

-- 通用审计字段片段（每张表都包含）：
--   create_by BIGINT, create_time DATETIME, update_by BIGINT, update_time DATETIME, deleted TINYINT DEFAULT 0

CREATE TABLE product (
  id            BIGINT       NOT NULL PRIMARY KEY,
  name          VARCHAR(64)  NOT NULL,
  code          VARCHAR(64)  NOT NULL,
  icon          VARCHAR(255) NULL,
  identity_mode VARCHAR(16)  NOT NULL DEFAULT 'LICENSE' COMMENT 'LICENSE/TENANT/DEVICE',
  status        VARCHAR(16)  NOT NULL DEFAULT 'ENABLED',
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_code (code)  -- 唯一性在应用层按 deleted=0 校验（避免逻辑删除与唯一键冲突）
) COMMENT='产品';

CREATE TABLE server_node (
  id        BIGINT       NOT NULL PRIMARY KEY,
  name      VARCHAR(64)  NOT NULL,
  code      VARCHAR(64)  NOT NULL,
  env       VARCHAR(16)  NOT NULL DEFAULT 'PROD' COMMENT 'PROD/TEST',
  region    VARCHAR(64)  NULL,
  base_url  VARCHAR(255) NULL,
  status    VARCHAR(16)  NOT NULL DEFAULT 'ENABLED',
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_server_code (code)
) COMMENT='服务器节点';

CREATE TABLE server_product (
  id             BIGINT NOT NULL PRIMARY KEY,
  server_node_id BIGINT NOT NULL,
  product_id     BIGINT NOT NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_server_product (server_node_id, product_id),
  KEY idx_sp_product (product_id)
) COMMENT='服务器-产品 多对多';

CREATE TABLE customer (
  id             BIGINT       NOT NULL PRIMARY KEY,
  name           VARCHAR(128) NOT NULL,
  industry       VARCHAR(64)  NULL,
  package_level  VARCHAR(16)  NULL COMMENT 'FLAGSHIP/PRO/STANDARD/BASIC',
  status         VARCHAR(16)  NOT NULL DEFAULT 'ENABLED',
  wecom_user_ids VARCHAR(512) NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_customer_industry (industry),
  KEY idx_customer_package (package_level)
) COMMENT='客户';

CREATE TABLE customer_product (
  id              BIGINT      NOT NULL PRIMARY KEY,
  customer_id     BIGINT      NOT NULL,
  product_id      BIGINT      NOT NULL,
  server_node_id  BIGINT      NOT NULL,
  current_version VARCHAR(32) NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_cust_product (customer_id, product_id),
  KEY idx_cp_product_server (product_id, server_node_id)
) COMMENT='客户-产品-服务器 归属';

CREATE TABLE customer_identity (
  id             BIGINT       NOT NULL PRIMARY KEY,
  customer_id    BIGINT       NOT NULL,
  product_id     BIGINT       NOT NULL,
  identity_type  VARCHAR(16)  NOT NULL,
  identity_value VARCHAR(255) NOT NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_identity (product_id, identity_type, identity_value)
) COMMENT='客户身份映射';

CREATE TABLE app_version (
  id                BIGINT       NOT NULL PRIMARY KEY,
  product_id        BIGINT       NOT NULL,
  version_no        VARCHAR(32)  NOT NULL,
  description       VARCHAR(1024) NULL COMMENT '版本说明',
  release_type      VARCHAR(16)  NOT NULL DEFAULT 'RELEASE',
  plan_release_time DATETIME     NULL,
  status            VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_version (product_id, version_no),
  KEY idx_version_status (status)
) COMMENT='版本';

CREATE TABLE version_change_item (
  id         BIGINT       NOT NULL PRIMARY KEY,
  version_id BIGINT       NOT NULL,
  category   VARCHAR(16)  NOT NULL COMMENT 'ADD/OPTIMIZE/FIX',
  title      VARCHAR(255) NOT NULL,
  content    TEXT         NULL,
  sort       INT          NOT NULL DEFAULT 0,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_item_version (version_id)
) COMMENT='更新条目';

CREATE TABLE audience (
  id              BIGINT       NOT NULL PRIMARY KEY,
  name            VARCHAR(128) NOT NULL,
  condition_json  TEXT         NOT NULL,
  hit_count_cache BIGINT       NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='人群模板';

CREATE TABLE release_plan (
  id               BIGINT       NOT NULL PRIMARY KEY,
  version_id       BIGINT       NOT NULL,
  audience_id      BIGINT       NOT NULL,
  channels         VARCHAR(255) NOT NULL COMMENT 'JSON 数组',
  pre_notify_days  VARCHAR(64)  NULL COMMENT 'JSON 数组如[3,1]',
  release_time     DATETIME     NULL,
  schedule_enabled TINYINT      NOT NULL DEFAULT 1,
  status           VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_plan_version (version_id),
  KEY idx_plan_status (status)
) COMMENT='发布计划';

CREATE TABLE approval_flow (
  id         BIGINT       NOT NULL PRIMARY KEY,
  product_id BIGINT       NOT NULL,
  name       VARCHAR(128) NOT NULL,
  enabled    TINYINT      NOT NULL DEFAULT 1,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_flow_product (product_id)
) COMMENT='审批流(按产品线)';

CREATE TABLE approval_flow_node (
  id           BIGINT      NOT NULL PRIMARY KEY,
  flow_id      BIGINT      NOT NULL,
  level        INT         NOT NULL,
  approver_type VARCHAR(16) NOT NULL DEFAULT 'USER',
  approver_ids VARCHAR(255) NOT NULL COMMENT '逗号分隔',
  approve_mode VARCHAR(16) NOT NULL DEFAULT 'SINGLE' COMMENT 'SINGLE/AND/OR',
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_node_flow (flow_id, level)
) COMMENT='审批节点';

CREATE TABLE approval_record (
  id         BIGINT       NOT NULL PRIMARY KEY,
  biz_type   VARCHAR(16)  NOT NULL COMMENT 'VERSION/RELEASE_PLAN',
  biz_id     BIGINT       NOT NULL,
  flow_id    BIGINT       NULL,
  node_level INT          NULL,
  approver_id BIGINT      NULL,
  result     VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
  comment    VARCHAR(512) NULL,
  op_time    DATETIME     NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_record_biz (biz_type, biz_id),
  KEY idx_record_approver (approver_id, result)
) COMMENT='审批记录';

CREATE TABLE announcement (
  id            BIGINT       NOT NULL PRIMARY KEY,
  version_id    BIGINT       NOT NULL,
  type          VARCHAR(16)  NOT NULL COMMENT 'PRE_NOTICE/UPDATE',
  title         VARCHAR(255) NOT NULL,
  popup_content TEXT         NULL,
  jump_url      VARCHAR(255) NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_ann_version (version_id, type)
) COMMENT='公告';

CREATE TABLE delivery_task (
  id             BIGINT       NOT NULL PRIMARY KEY,
  announcement_id BIGINT      NOT NULL,
  audience_id    BIGINT       NOT NULL,
  channel        VARCHAR(16)  NOT NULL,
  scheduled_time DATETIME     NULL,
  status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
  idempotent_key VARCHAR(128) NOT NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_task_idem (idempotent_key),  -- 投递任务不做逻辑删除，强幂等
  KEY idx_task_sched (status, scheduled_time)
) COMMENT='投递任务';

CREATE TABLE delivery_record (
  id             BIGINT      NOT NULL PRIMARY KEY,
  task_id        BIGINT      NOT NULL,
  announcement_id BIGINT     NOT NULL,
  customer_id    BIGINT      NOT NULL,
  channel        VARCHAR(16) NOT NULL,
  send_status    VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  read_status    VARCHAR(16) NOT NULL DEFAULT 'UNREAD',
  read_time      DATETIME    NULL,
  fail_reason    VARCHAR(255) NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_record (task_id, customer_id, channel),  -- 发送幂等，不做逻辑删除
  KEY idx_record_customer_read (customer_id, read_status),
  KEY idx_record_ann (announcement_id)
) COMMENT='逐客户触达记录';

CREATE TABLE feedback (
  id             BIGINT      NOT NULL PRIMARY KEY,
  announcement_id BIGINT     NOT NULL,
  customer_id    BIGINT      NOT NULL,
  rating         INT         NULL,
  content        VARCHAR(1024) NULL,
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_feedback_ann (announcement_id)
) COMMENT='客户反馈';

CREATE TABLE sys_user (
  id       BIGINT       NOT NULL PRIMARY KEY,
  username VARCHAR(64)  NOT NULL,
  password VARCHAR(128) NOT NULL,
  nickname VARCHAR(64)  NULL,
  roles    VARCHAR(255) NULL,
  status   VARCHAR(16)  NOT NULL DEFAULT 'ENABLED',
  create_by BIGINT NULL, create_time DATETIME NULL, update_by BIGINT NULL, update_time DATETIME NULL, deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_username (username)
) COMMENT='后台用户';
