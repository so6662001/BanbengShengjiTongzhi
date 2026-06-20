# 版本更新通知工具 · Cursor AI 开发提示词

> 用法：先把【主提示词】作为项目级规则（可放入 `.cursor/rules` 或每次对话开头粘贴），再按【分阶段提示词】逐个模块开发。每完成一个阶段，用【验收清单】自检，确保功能完整、逻辑不缺失。

---

## 一、主提示词（项目总纲 · 每个对话都带上）

```
你是一名资深全栈工程师，负责开发「版本更新通知工具」。请严格遵循以下规范产出可运行、可维护的生产级代码，不要遗漏边界与异常处理。

【产品目标】
面向一家多产品线软件公司，构建「软件升级的事前预告 + 事后告知」一体化平台：
- 后台：多服务器、多产品、多版本管理；更新内容结构化（新增/优化/修复）+ 版本说明；按产品线多级审批；定时发布与事前预告；客户分层圈选；数据看板。
- 客户端接口：启动拉取未读公告/弹窗、历史更新日志查询、已读状态上报。
- 多渠道推送：一期支持「站内信 + 企业微信」，预留邮件/短信/钉钉扩展。
- 前端通用组件：软件内更新弹窗、独立更新日志页（可嵌入系统设置）。

【技术栈】
- 后端：Java 17、Spring Boot 3.x、MyBatis-Plus、MySQL 8、Redis、RocketMQ、XXL-JOB（分布式定时）、Flowable（审批流）、Spring Security + JWT、Knife4j/OpenAPI3、Lombok、MapStruct。
- 前端：Vue 3 + Vite + TypeScript + Pinia + Vue Router + Element Plus + Axios。客户端通用组件单独打包为可复用 npm 包（库模式）。
- 构建：后端 Maven 多模块；前端 pnpm。

【工程结构（后端多模块）】
release-notify/
├── notify-common        // 通用：返回体、异常、枚举、工具、常量
├── notify-admin-api     // 后台管理端接口（内部用户，低并发）
├── notify-client-api    // 客户端接口（终端客户，高并发，独立部署/限流）
├── notify-service       // 领域服务：版本/发布/审批/分层/投递/统计
├── notify-push          // 多渠道推送：渠道适配 + MQ 消费者 + 模板
├── notify-job           // XXL-JOB 定时任务（事前预告生成、状态流转）
└── notify-domain        // 实体、Mapper、DTO/VO、Converter

【全局约定】
1. 分层：Controller → Service(接口+impl) → Mapper；DTO/VO 与实体分离，用 MapStruct 转换；禁止实体直接出参。
2. 统一返回体 Result<T>{code,message,data,traceId}；统一异常处理 @RestControllerAdvice；业务异常用 BizException(错误码枚举)。
3. 所有写接口加参数校验（JSR-303），关键操作打操作日志（操作人、对象、前后值）。
4. 并发与稳定：客户端接口必须限流（如 Sentinel/令牌桶）+ 鉴权；高频读走 Redis 缓存；批量推送一律异步（MQ），禁止同步循环发送。
5. 幂等：定时预告生成、推送发送、已读上报均需幂等（唯一键/状态机/去重表），避免重复打扰客户或重复计数。
6. 时间统一 UTC 存储、按需转时区；金额/计数等统计口径在注释中写明。
7. 软删除 + 审计字段：所有业务表含 id、create_by、create_time、update_by、update_time、deleted。
8. 安全：客户端接口用 License/Token + 签名校验；公告内容做权限隔离，防止越权拉取他人公告；SQL 全部参数化。
9. 代码注释只解释“为什么/约束/取舍”，不写废话注释；命名清晰；禁止魔法值（用枚举/常量）。
10. 每个模块产出对应单元测试（Service 层为主）与关键集成测试；提供 README 与本地启动说明。

【关键领域概念（务必理解，避免逻辑缺失）】
- 一台服务器可部署多个产品（server ↔ product 多对多）。
- 客户归属精确到「客户-产品-服务器」三元关系：customer_product(customer_id, product_id, server_node_id, current_version)。
- “内容(版本/公告)”与“投递(任务/记录)”解耦：一份内容可推给多个人群、多个渠道，独立统计。
- 客户身份按产品可配置多种模式（License / 租户账号 / 设备实例），用可插拔 CustomerIdentityResolver 解析为内部 customer_id。
- 升级完成率 = 目标人群中 current_version ≥ 目标版本 的客户占比。
- 阅读率 = 已读数 / 成功送达数（按渠道、人群、版本可下钻）。

每次开发前先复述你将实现的接口/类清单与边界条件，再写代码。
```

---

## 二、数据模型（建库提示词）

```
请基于以下实体生成 MySQL 8 DDL（含索引、外键或逻辑外键、注释、审计字段、deleted 软删除），并生成对应 MyBatis-Plus 实体与 Mapper。所有表加 create_by/create_time/update_by/update_time/deleted。

1) product 产品：id, name, code(唯一), icon, identity_mode(枚举:LICENSE/TENANT/DEVICE), status
2) server_node 服务器：id, name, code(唯一), env(枚举:PROD/TEST), region, base_url, status
3) server_product 服务器-产品关系(多对多)：server_node_id, product_id  (唯一索引)
4) customer 客户：id, name, industry(行业), package(套餐枚举), status, wecom_user_ids(企微接收人,逗号或JSON)
5) customer_product 客户-产品-服务器：id, customer_id, product_id, server_node_id, current_version (唯一索引 customer_id+product_id)
6) customer_identity 客户身份映射：id, customer_id, product_id, identity_type, identity_value (唯一索引 product_id+identity_type+identity_value)
7) app_version 版本：id, product_id, version_no, description(版本说明), release_type(枚举:RELEASE/GRAY), plan_release_time, status(枚举:DRAFT/REVIEW/APPROVED/SCHEDULED/PUBLISHED/REVOKED)
8) version_change_item 更新条目：id, version_id, category(枚举:ADD/OPTIMIZE/FIX), title, content(富文本), sort
9) audience 人群模板：id, name, condition_json(套餐/行业/产品/服务器/版本条件), hit_count_cache
10) release_plan 发布计划：id, version_id, audience_id, channels(JSON:站内信/企微...), pre_notify_days(JSON 数组如[3,1]), release_time, schedule_enabled, status
11) approval_flow 审批流：id, product_id, name, enabled
12) approval_flow_node 审批节点：id, flow_id, level, approver_type(USER/ROLE), approver_ids, approve_mode(SINGLE/AND/OR 会签/或签)
13) approval_record 审批记录：id, biz_type(版本/发布计划), biz_id, flow_id, node_level, approver_id, result(枚举:PASS/REJECT/PENDING), comment, op_time
14) announcement 公告：id, version_id, type(枚举:PRE_NOTICE 预告/UPDATE 更新), title, popup_content, jump_url
15) delivery_task 投递任务：id, announcement_id, audience_id, channel, scheduled_time, status(枚举:PENDING/RUNNING/DONE/CANCELED), idempotent_key(唯一)
16) delivery_record 逐客户触达记录：id, task_id, customer_id, channel, send_status(枚举:PENDING/SENT/FAIL), read_status(枚举:UNREAD/READ), read_time, fail_reason (唯一索引 task_id+customer_id+channel)
17) feedback 反馈：id, announcement_id, customer_id, rating, content

请同时输出枚举类、表关系说明、以及为高频查询设计的索引（如 delivery_record 按 customer_id+read_status、按 task_id 聚合统计）。
```

---

## 三、分阶段开发提示词（按顺序逐个粘贴）

### 阶段 1：基础框架
```
搭建后端 Maven 多模块骨架与前端 Vite 工程；实现统一返回体、全局异常、错误码枚举、JWT 鉴权脚手架、Knife4j 文档、MyBatis-Plus 配置、Redis/RocketMQ/XXL-JOB/Flowable 的基础配置类与本地 docker-compose（MySQL/Redis/RocketMQ）。产出可一键启动的最小可运行工程与 README。
```

### 阶段 2：版本与产品/服务器管理（后台）
```
实现产品、服务器（含 server_product 多对多）、版本(app_version + version_change_item，含版本说明 description) 的增删改查与状态机。要求：
- 版本状态机：DRAFT→REVIEW→APPROVED→SCHEDULED→PUBLISHED→REVOKED，非法流转抛业务异常。
- 更新条目按 ADD/OPTIMIZE/FIX 分类，支持排序。
- 提供“版本弹窗预览”聚合接口（返回版本说明 + 分类条目）。
- 全部接口写 OpenAPI 注解与 Service 单测。
```

### 阶段 3：客户与分层圈选
```
实现客户、customer_product(客户-产品-服务器)、customer_identity 管理；实现人群圈选服务：按 套餐∩行业∩产品∩服务器∩当前版本 组合条件查询命中客户，返回命中数与分布；支持保存为 audience 人群模板并缓存命中数。注意大数据量下分页与统计性能（可用覆盖索引/预聚合）。
```

### 阶段 4：审批引擎（按产品线多级）
```
基于 Flowable 或自研状态机实现按 product_id 配置的多级审批：approval_flow + approval_flow_node（支持 SINGLE/会签 AND/或签 OR）。提交版本或发布计划时按产品线加载审批链，逐级流转，记录 approval_record，支持通过/驳回/加签/撤回；驳回退回草稿并带意见；全部通过后才允许进入 SCHEDULED/发布。提供“待我审批”“审批详情”接口。
```

### 阶段 5：发布计划 + 定时 + 事前预告
```
实现 release_plan：关联版本+人群+渠道(站内信/企微)+pre_notify_days(如[3,1])+release_time。
- 审批通过后，由 XXL-JOB 在 (release_time - N天) 自动生成“预告”投递任务；正式发布时生成“更新”投递任务。
- 生成投递任务必须幂等（idempotent_key=plan+type+预告天数），防重复。
- 支持“撤回/停止推送”：取消未执行的 delivery_task，已发送的不回收但停止后续。
- 发布动作把 app_version 置为 PUBLISHED。
```

### 阶段 6：多渠道推送服务
```
在 notify-push 实现：
- MessageChannel 接口（send(target, content)）；InAppChannel(站内信，写 delivery_record + 供客户端拉取)、WeComChannel(企业微信应用消息，图文卡片)。邮件/短信/钉钉留出 Adapter 空实现+TODO。
- 投递编排：release/job 触发 → 拆分为按渠道 delivery_task → 发到 RocketMQ → 各渠道消费者按各自 QPS 限速发送 → 回写 delivery_record(send_status)。
- 发送幂等（task_id+customer_id+channel 唯一）、失败重试与死信处理、统一模板(变量占位:客户名/产品/版本/时间)。
```

### 阶段 7：客户端接口服务（高并发）
```
在 notify-client-api 实现 3 个接口，均需 License/Token+签名鉴权与限流，按 product.identity_mode 用 CustomerIdentityResolver 解析 customer_id：
1) GET /client/announcements/unread?productCode&version  返回未读预告+更新弹窗（未读集合走 Redis 缓存）。
2) GET /client/changelog?productCode&page&category  历史更新日志分页查询。
3) POST /client/announcements/{id}/read  已读上报，异步写 delivery_record.read_status（先返回成功，幂等去重）。
注意防越权：只能拉取本客户、本产品、且属于其归属服务器的公告。
```

### 阶段 8：数据看板
```
实现统计接口：触达漏斗(目标→送达→曝光→已读→升级完成)、平均阅读率、按渠道阅读率、按服务器升级完成率(current_version≥目标版本)、反馈列表。统计口径在代码注释写明；大数据量用预聚合表或定时汇总，避免实时全表扫描。
```

### 阶段 9：前端后台（Vue3 + Element Plus）
```
按已确认的静态原型(prototype/ 目录)实现后台 6 个页面：数据看板、版本管理、服务器管理、客户分层、发布流程、审批中心。要求：路由+布局(侧边栏/顶栏)、Pinia 管理状态、Axios 封装(统一拦截/错误提示)、表单校验、与后端接口联调；视觉风格对齐原型。
```

### 阶段 10：前端通用组件（可复用 SDK）
```
开发可复用 Vue3 组件库(库模式打包成 npm 包)：
- <UpdatePopup>：软件内更新弹窗，props 见下；支持“预告/更新”两种样式、强提醒(必读才可关)/弱提醒、按分类分组、阅读后自动调已读接口。
- <ChangelogPage>：独立更新日志页，时间线+分类筛选+搜索+分页，可嵌入系统设置。
- initUpdateNotifier({clientId, productCode, version, apiBase, token}) 一行初始化：自动拉未读→弹窗→上报。
B/S 直接引入；C/S 优先用 WebView 复用同一组件，纯原生端则仅调 REST。
```

---

## 四、关键接口契约（让 Cursor 据此生成 Controller + OpenAPI）

```
统一返回 Result<T>{code,message,data,traceId}；分页用 Page<T>{records,total,current,size}。

后台 Admin（需登录鉴权）：
- 产品/服务器/版本/条目：标准 CRUD + 状态流转
- POST /admin/audience/preview        圈选预览(入参条件JSON)→{hitCount,distribution}
- POST /admin/audience                保存人群模板
- POST /admin/release-plan            创建发布计划
- POST /admin/release-plan/{id}/submit 提交审批
- POST /admin/release-plan/{id}/revoke 撤回/停推
- GET  /admin/approval/todo           待我审批
- POST /admin/approval/{recordId}     审批(PASS/REJECT+comment)
- GET  /admin/dashboard/funnel?versionId
- GET  /admin/dashboard/channel?versionId
- GET  /admin/dashboard/server-upgrade?versionId

客户端 Client（License/Token+签名）：
- GET  /client/announcements/unread?productCode&version
- POST /client/announcements/{id}/read
- GET  /client/changelog?productCode&category&page&size
- POST /client/feedback   {announcementId, rating, content}

<UpdatePopup> props:
{ data: { type:'PRE_NOTICE'|'UPDATE', title, description(版本说明), versionNo, releaseTime, items:[{category, title, content}], jumpUrl },
  forceRead?:boolean, onRead?:fn, onClose?:fn }
```

---

## 五、验收清单（每阶段自检 · 防逻辑缺失）

- [ ] 服务器可部署多个产品；客户归属精确到“客户-产品-服务器”，统计无串档。
- [ ] 版本含“版本说明”字段，并在弹窗顶部与更新日志页正确展示。
- [ ] 更新内容严格区分 新增/优化/修复，前后端分类一致。
- [ ] 审批按产品线走不同层级；驳回可退回；未通过不可发布；审批全程留痕。
- [ ] 事前预告按 pre_notify_days 自动生成且幂等，不重复打扰；撤回能停止未发送任务。
- [ ] 推送全程异步(MQ)、限速、失败重试、发送幂等；阅读率/送达率口径正确。
- [ ] 客户端接口鉴权+限流+签名；防越权拉取他人公告；未读走缓存。
- [ ] 升级完成率 = current_version≥目标版本占比；看板漏斗各环节口径明确。
- [ ] 身份模式可按产品切换(License/租户/设备)，Resolver 可插拔。
- [ ] 关键 Service 有单测；接口有 OpenAPI 文档；提供本地一键启动。
- [ ] 时间时区、金额/计数口径、软删除、审计字段全部落实。
- [ ] 渠道一期=站内信+企微；邮件/短信/钉钉留有扩展点且不影响主流程。

---

## 六、给 Cursor 的协作建议

1. 先让它“复述计划+列接口/类清单+边界条件”，确认后再写代码，减少偏差。
2. 一次只做一个阶段，做完跑测试再进入下一个，避免大范围返工。
3. 涉及并发/幂等/状态机的代码，要求它显式写出“边界与异常分支”并补单测。
4. 前端严格对齐 `prototype/` 原型，要求“先搭路由与布局，再逐页面填充”。
5. 每个阶段结束让它更新 README 与接口文档，保持文档与代码同步。
```
