-- H2(MySQL 模式) 测试用 DDL，与 sql/schema.sql 结构一致，去除 MySQL 专有表选项/注释。
-- 共享内存库可能被多个测试上下文重复初始化，故先 DROP 保证可重入。
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS server_node;
DROP TABLE IF EXISTS server_product;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS customer_product;
DROP TABLE IF EXISTS customer_identity;
DROP TABLE IF EXISTS app_version;
DROP TABLE IF EXISTS version_change_item;
DROP TABLE IF EXISTS audience;
DROP TABLE IF EXISTS release_plan;
DROP TABLE IF EXISTS approval_flow;
DROP TABLE IF EXISTS approval_flow_node;
DROP TABLE IF EXISTS approval_record;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS delivery_task;
DROP TABLE IF EXISTS delivery_record;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE product (
  id BIGINT PRIMARY KEY, name VARCHAR(64), code VARCHAR(64), icon VARCHAR(255),
  identity_mode VARCHAR(16), status VARCHAR(16),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE server_node (
  id BIGINT PRIMARY KEY, name VARCHAR(64), code VARCHAR(64), env VARCHAR(16), region VARCHAR(64), base_url VARCHAR(255), status VARCHAR(16),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE server_product (
  id BIGINT PRIMARY KEY, server_node_id BIGINT, product_id BIGINT,
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE customer (
  id BIGINT PRIMARY KEY, name VARCHAR(128), industry VARCHAR(64), package_level VARCHAR(16), status VARCHAR(16), wecom_user_ids VARCHAR(512),
  email VARCHAR(128), phone VARCHAR(32),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE customer_product (
  id BIGINT PRIMARY KEY, customer_id BIGINT, product_id BIGINT, server_node_id BIGINT, current_version VARCHAR(32),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE customer_identity (
  id BIGINT PRIMARY KEY, customer_id BIGINT, product_id BIGINT, identity_type VARCHAR(16), identity_value VARCHAR(255),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE app_version (
  id BIGINT PRIMARY KEY, product_id BIGINT, version_no VARCHAR(32), description VARCHAR(1024),
  release_type VARCHAR(16), plan_release_time TIMESTAMP, status VARCHAR(16),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE version_change_item (
  id BIGINT PRIMARY KEY, version_id BIGINT, category VARCHAR(16), title VARCHAR(255), content CLOB, sort INT,
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE audience (
  id BIGINT PRIMARY KEY, name VARCHAR(128), condition_json CLOB, hit_count_cache BIGINT,
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE release_plan (
  id BIGINT PRIMARY KEY, version_id BIGINT, audience_id BIGINT, channels VARCHAR(255), pre_notify_days VARCHAR(64),
  release_time TIMESTAMP, schedule_enabled TINYINT, status VARCHAR(16),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE approval_flow (
  id BIGINT PRIMARY KEY, product_id BIGINT, name VARCHAR(128), enabled TINYINT,
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE approval_flow_node (
  id BIGINT PRIMARY KEY, flow_id BIGINT, level INT, approver_type VARCHAR(16), approver_ids VARCHAR(255), approve_mode VARCHAR(16),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE approval_record (
  id BIGINT PRIMARY KEY, biz_type VARCHAR(16), biz_id BIGINT, flow_id BIGINT, node_level INT, approver_id BIGINT,
  result VARCHAR(16), comment VARCHAR(512), op_time TIMESTAMP,
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE announcement (
  id BIGINT PRIMARY KEY, version_id BIGINT, type VARCHAR(16), title VARCHAR(255), popup_content CLOB, jump_url VARCHAR(255),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE delivery_task (
  id BIGINT PRIMARY KEY, announcement_id BIGINT, audience_id BIGINT, channel VARCHAR(16), scheduled_time TIMESTAMP,
  status VARCHAR(16), idempotent_key VARCHAR(128),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE delivery_record (
  id BIGINT PRIMARY KEY, task_id BIGINT, announcement_id BIGINT, customer_id BIGINT, channel VARCHAR(16),
  send_status VARCHAR(16), read_status VARCHAR(16), read_time TIMESTAMP, fail_reason VARCHAR(255),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE feedback (
  id BIGINT PRIMARY KEY, announcement_id BIGINT, customer_id BIGINT, rating INT, content VARCHAR(1024),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY, username VARCHAR(64), password VARCHAR(128), nickname VARCHAR(64), roles VARCHAR(255), status VARCHAR(16),
  create_by BIGINT, create_time TIMESTAMP, update_by BIGINT, update_time TIMESTAMP, deleted TINYINT DEFAULT 0);
