-- 演示种子数据。密码为明文 admin123（生产请改为 BCrypt）。
USE release_notify;

INSERT INTO sys_user (id, username, password, nickname, roles, status, deleted) VALUES
 (1, 'admin', 'admin123', '研发总裁', 'ADMIN', 'ENABLED', 0),
 (2, 'pm',    'admin123', '产品经理', 'PM', 'ENABLED', 0),
 (3, 'owner', 'admin123', '产品线负责人', 'PRODUCT_OWNER', 'ENABLED', 0);

INSERT INTO product (id, name, code, identity_mode, status, deleted) VALUES
 (101, '智控 ERP', 'erp', 'LICENSE', 'ENABLED', 0),
 (102, '云店 SaaS', 'cloudshop', 'TENANT', 'ENABLED', 0),
 (103, '财务通', 'finance', 'DEVICE', 'ENABLED', 0);

INSERT INTO server_node (id, name, code, env, region, status, deleted) VALUES
 (201, '华东生产 SH-01', 'srv-sh-01', 'PROD', '上海', 'ENABLED', 0),
 (202, '华北生产 BJ-02', 'srv-bj-02', 'PROD', '北京', 'ENABLED', 0);

-- 一台服务器部署多个产品
INSERT INTO server_product (id, server_node_id, product_id, deleted) VALUES
 (301, 201, 101, 0), (302, 201, 103, 0), (303, 201, 102, 0),
 (304, 202, 101, 0), (305, 202, 103, 0);

INSERT INTO customer (id, name, industry, package_level, status, wecom_user_ids, deleted) VALUES
 (401, '优品商贸', '商贸零售', 'FLAGSHIP', 'ENABLED', 'zhangsan', 0),
 (402, '恒丰制造', '制造业', 'PRO', 'ENABLED', 'lisi', 0),
 (403, '康程物流', '物流仓储', 'PRO', 'ENABLED', 'wangwu', 0);

INSERT INTO customer_product (id, customer_id, product_id, server_node_id, current_version, deleted) VALUES
 (501, 401, 101, 201, 'v3.1.9', 0),
 (502, 402, 101, 202, 'v3.2.0', 0),
 (503, 403, 101, 201, 'v3.1.8', 0);

INSERT INTO customer_identity (id, customer_id, product_id, identity_type, identity_value, deleted) VALUES
 (601, 401, 101, 'LICENSE', 'LIC-YOUPIN-001', 0),
 (602, 402, 101, 'LICENSE', 'LIC-HENGFENG-002', 0),
 (603, 403, 101, 'LICENSE', 'LIC-KANGCHENG-003', 0);

-- 审批流：智控 ERP 两级审批
INSERT INTO approval_flow (id, product_id, name, enabled, deleted) VALUES
 (701, 101, '智控ERP两级审批', 1, 0);
INSERT INTO approval_flow_node (id, flow_id, level, approver_type, approver_ids, approve_mode, deleted) VALUES
 (801, 701, 1, 'USER', '2', 'SINGLE', 0),
 (802, 701, 2, 'USER', '3', 'SINGLE', 0);
