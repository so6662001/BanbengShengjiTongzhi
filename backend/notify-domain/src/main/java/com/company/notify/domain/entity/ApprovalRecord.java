package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.ApprovalBizType;
import com.company.notify.common.enums.ApproveResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 审批记录（逐节点逐审批人留痕） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_record")
public class ApprovalRecord extends BaseEntity {
    private ApprovalBizType bizType;
    private Long bizId;
    private Long flowId;
    private Integer nodeLevel;
    private Long approverId;
    private ApproveResult result;
    private String comment;
    private LocalDateTime opTime;
}
