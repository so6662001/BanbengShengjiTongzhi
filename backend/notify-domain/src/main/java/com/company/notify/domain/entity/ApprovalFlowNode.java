package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.ApproveMode;
import com.company.notify.common.enums.ApproverType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 审批节点。level 从 1 递增表示审批层级。approverIds 逗号分隔。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_flow_node")
public class ApprovalFlowNode extends BaseEntity {
    private Long flowId;
    private Integer level;
    private ApproverType approverType;
    private String approverIds;
    private ApproveMode approveMode;
}
