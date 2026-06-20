package com.company.notify.common.exception;

import lombok.Getter;

/**
 * 业务错误码。区间约定：
 * 1xxx 通用 / 2xxx 鉴权 / 3xxx 版本与发布 / 4xxx 审批 / 5xxx 客户与分层 / 6xxx 推送与客户端
 */
@Getter
public enum ErrorCode {

    SYSTEM_ERROR(1000, "系统繁忙，请稍后重试"),
    PARAM_INVALID(1001, "参数校验失败"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_CONFLICT(1003, "数据冲突或已存在"),
    OPERATION_NOT_ALLOWED(1004, "当前状态不允许该操作"),

    UNAUTHORIZED(2001, "未登录或登录已失效"),
    FORBIDDEN(2002, "无权限访问"),
    CLIENT_AUTH_FAIL(2003, "客户端鉴权失败"),
    SIGN_INVALID(2004, "签名校验失败"),

    VERSION_STATUS_ILLEGAL(3001, "版本状态流转非法"),
    VERSION_NO_DUPLICATED(3002, "同产品下版本号已存在"),
    RELEASE_PLAN_NOT_APPROVED(3003, "发布计划未通过审批，不能发布"),
    RELEASE_PLAN_PUBLISHED(3004, "发布计划已发布，不能重复操作"),

    APPROVAL_FLOW_NOT_CONFIG(4001, "该产品线未配置审批流"),
    APPROVAL_NODE_NOT_CURRENT(4002, "当前审批节点不可处理"),
    APPROVAL_NO_PERMISSION(4003, "您不是当前节点审批人"),

    CUSTOMER_NOT_FOUND(5001, "客户不存在"),
    AUDIENCE_CONDITION_EMPTY(5002, "圈选条件不能为空"),

    IDENTITY_RESOLVE_FAIL(6001, "客户身份解析失败"),
    ANNOUNCEMENT_FORBIDDEN(6002, "无权访问该公告"),
    CHANNEL_NOT_SUPPORT(6003, "不支持的推送渠道");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
