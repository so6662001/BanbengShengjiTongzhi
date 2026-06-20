# 版本更新通知工具 · 后端

Spring Boot 3 + MyBatis-Plus 多模块工程。

## 模块
| 模块 | 说明 |
|---|---|
| notify-common | 返回体 `Result`、`PageResult`、异常与错误码、业务枚举、用户上下文 |
| notify-domain | 实体、Mapper、BaseEntity（审计+逻辑删除） |
| notify-service | 领域服务：产品/服务器/版本/客户/分层/审批/发布/投递/看板/客户端/身份解析 |
| notify-push | 渠道适配（站内信 / 企业微信）+ 异步推送编排（进程内，可换 RocketMQ） |
| notify-job | 事前预告定时任务（Spring @Scheduled，可换 XXL-JOB） |
| notify-admin-api | 后台管理端接口（端口 8081，JWT 鉴权） |
| notify-client-api | 客户端接口（端口 8082，身份解析） |

## 关键设计落点
- 服务器 ↔ 产品 多对多；客户归属精确到「客户-产品-服务器」三元（升级完成率口径）。
- 版本状态机：DRAFT→REVIEW→APPROVED→SCHEDULED→PUBLISHED/REVOKED，非法流转抛异常。
- 按产品线多级审批引擎（SINGLE/会签 AND/或签 OR），通过回调驱动版本/计划状态。
- 内容与投递解耦；投递任务幂等键防止预告/发布重复；撤回可停推未发送任务。
- 客户身份可插拔（LICENSE/TENANT/DEVICE），客户端接口做产品归属隔离防越权。
- 推送一期渠道：站内信 + 企业微信；邮件/短信/钉钉预留 `MessageChannel` 扩展点。

## 本地运行
1. 准备 MySQL8、Redis；执行 `sql/schema.sql`、`sql/seed.sql`。
2. 配置环境变量 `DB_HOST/DB_USER/DB_PASSWORD/REDIS_HOST`（或改 application.yml）。
3. 启动后台：`mvn -pl notify-admin-api spring-boot:run`（接口文档 http://localhost:8081/doc.html）
4. 启动客户端：`mvn -pl notify-client-api spring-boot:run`（http://localhost:8082/doc.html）
5. 默认账号：admin / admin123。

## 编译与测试
```
mvn compile           # 全量编译
mvn -pl notify-service test   # 服务层单测
```

## 可切换基础设施（配置开关，默认走可测试的轻量实现）
| 能力 | 默认 | 切换为生产实现 |
|---|---|---|
| 推送队列 | `notify.mq.mode=inprocess`（@Async） | `=rocketmq` + 配 `rocketmq.name-server`（消费者重试/死信由 MQ 承接） |
| 定时任务 | `notify.job.mode=spring`（@Scheduled） | `=xxl` + 配 `xxl.job.admin.addresses`（JobHandler=`preNoticeJobHandler`） |
| 企业微信 | MOCK 日志 | `notify.wecom.enabled=true` + corp-id/secret/agent-id（真实 textcard） |
| 邮件 | 关闭 | `notify.email.enabled=true` + `spring.mail.*`（JavaMailSender） |
| 短信 | 关闭 | `notify.sms.enabled=true` + gateway-url/api-key（HTTP 网关） |
| 钉钉 | 关闭 | `notify.dingtalk.enabled=true` + webhook/secret（自定义机器人加签） |
| 登录密码 | BCrypt（兼容历史明文） | 生产用 `PasswordHelper.encode` 存 BCrypt |
| 客户端鉴权 | 身份+限流 | `notify.client.sign-enabled=true` 开启签名校验 |

## 测试
- `mvn test`：单测（状态机/版本比较/审批引擎 Mockito）+ H2 上下文加载 + **端到端全链路** + **真实 HTTP 冒烟(随机端口 Tomcat)**。
