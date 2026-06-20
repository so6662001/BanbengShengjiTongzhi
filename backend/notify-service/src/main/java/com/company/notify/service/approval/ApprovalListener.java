package com.company.notify.service.approval;

import com.company.notify.common.enums.ApprovalBizType;

/**
 * 审批结果回调。由各业务服务（版本、发布计划）实现，
 * 审批引擎在流程终态时回调，避免引擎与业务相互直接依赖。
 */
public interface ApprovalListener {

    boolean support(ApprovalBizType bizType);

    /** 全流程通过 */
    void onApproved(Long bizId);

    /** 任一节点驳回 */
    void onRejected(Long bizId, String comment);
}
