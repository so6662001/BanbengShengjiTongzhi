package com.company.notify.service.approval;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.ApprovalBizType;
import com.company.notify.common.enums.ApproveMode;
import com.company.notify.common.enums.ApproveResult;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.ApprovalFlow;
import com.company.notify.domain.entity.ApprovalFlowNode;
import com.company.notify.domain.entity.ApprovalRecord;
import com.company.notify.domain.mapper.ApprovalFlowMapper;
import com.company.notify.domain.mapper.ApprovalFlowNodeMapper;
import com.company.notify.domain.mapper.ApprovalRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 按产品线多级审批引擎（自研状态机）。
 * 节点模式：SINGLE/OR 一人通过即过；AND 会签需全部通过；任一驳回则整单驳回退回。
 */
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalFlowMapper flowMapper;
    private final ApprovalFlowNodeMapper nodeMapper;
    private final ApprovalRecordMapper recordMapper;
    private final List<ApprovalListener> listeners;

    /** 提交审批：按产品线加载审批流，生成第一级待审记录。 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(ApprovalBizType bizType, Long bizId, Long productId) {
        ApprovalFlow flow = flowMapper.selectOne(new LambdaQueryWrapper<ApprovalFlow>()
                .eq(ApprovalFlow::getProductId, productId)
                .eq(ApprovalFlow::getEnabled, true)
                .last("limit 1"));
        if (flow == null) {
            throw BizException.of(ErrorCode.APPROVAL_FLOW_NOT_CONFIG);
        }
        List<ApprovalFlowNode> nodes = nodesOf(flow.getId());
        if (nodes.isEmpty()) {
            throw BizException.of(ErrorCode.APPROVAL_FLOW_NOT_CONFIG, "审批流未配置节点");
        }
        // 清理历史记录（重新提交场景）
        recordMapper.delete(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getBizType, bizType)
                .eq(ApprovalRecord::getBizId, bizId));
        createLevelRecords(bizType, bizId, flow.getId(), nodes.get(0));
    }

    /** 审批处理：通过/驳回。 */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long recordId, Long approverId, boolean pass, String comment) {
        ApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null || record.getResult() != ApproveResult.PENDING) {
            throw BizException.of(ErrorCode.APPROVAL_NODE_NOT_CURRENT);
        }
        if (!record.getApproverId().equals(approverId)) {
            throw BizException.of(ErrorCode.APPROVAL_NO_PERMISSION);
        }
        int currentLevel = currentLevel(record.getBizType(), record.getBizId());
        if (record.getNodeLevel() != currentLevel) {
            throw BizException.of(ErrorCode.APPROVAL_NODE_NOT_CURRENT);
        }

        record.setResult(pass ? ApproveResult.PASS : ApproveResult.REJECT);
        record.setComment(comment);
        record.setOpTime(LocalDateTime.now());
        recordMapper.updateById(record);

        if (!pass) {
            // 整单驳回
            notifyRejected(record.getBizType(), record.getBizId(), comment);
            return;
        }
        advanceIfNodeComplete(record);
    }

    private void advanceIfNodeComplete(ApprovalRecord record) {
        ApprovalFlowNode node = nodeMapper.selectOne(new LambdaQueryWrapper<ApprovalFlowNode>()
                .eq(ApprovalFlowNode::getFlowId, record.getFlowId())
                .eq(ApprovalFlowNode::getLevel, record.getNodeLevel())
                .last("limit 1"));
        List<ApprovalRecord> levelRecords = recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getBizType, record.getBizType())
                .eq(ApprovalRecord::getBizId, record.getBizId())
                .eq(ApprovalRecord::getNodeLevel, record.getNodeLevel()));

        boolean nodePassed;
        if (node != null && node.getApproveMode() == ApproveMode.AND) {
            // 会签：所有审批人均已通过
            nodePassed = levelRecords.stream().allMatch(r -> r.getResult() == ApproveResult.PASS);
        } else {
            // SINGLE / OR：存在一个通过即过
            nodePassed = levelRecords.stream().anyMatch(r -> r.getResult() == ApproveResult.PASS);
        }
        if (!nodePassed) {
            return;
        }
        // 进入下一级，或全流程结束
        List<ApprovalFlowNode> nodes = nodesOf(record.getFlowId());
        Optional<ApprovalFlowNode> next = nodes.stream()
                .filter(n -> n.getLevel() > record.getNodeLevel())
                .min((a, b) -> Integer.compare(a.getLevel(), b.getLevel()));
        if (next.isPresent()) {
            createLevelRecords(record.getBizType(), record.getBizId(), record.getFlowId(), next.get());
        } else {
            notifyApproved(record.getBizType(), record.getBizId());
        }
    }

    private void createLevelRecords(ApprovalBizType bizType, Long bizId, Long flowId, ApprovalFlowNode node) {
        for (String approver : splitIds(node.getApproverIds())) {
            ApprovalRecord r = new ApprovalRecord();
            r.setBizType(bizType);
            r.setBizId(bizId);
            r.setFlowId(flowId);
            r.setNodeLevel(node.getLevel());
            r.setApproverId(Long.valueOf(approver));
            r.setResult(ApproveResult.PENDING);
            recordMapper.insert(r);
        }
    }

    /** 当前审批层级 = 存在待审记录的最小层级；无待审则取最大已处理层级。 */
    public int currentLevel(ApprovalBizType bizType, Long bizId) {
        List<ApprovalRecord> pending = recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getBizType, bizType)
                .eq(ApprovalRecord::getBizId, bizId)
                .eq(ApprovalRecord::getResult, ApproveResult.PENDING));
        return pending.stream().mapToInt(ApprovalRecord::getNodeLevel).min().orElse(-1);
    }

    public List<ApprovalRecord> records(ApprovalBizType bizType, Long bizId) {
        return recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getBizType, bizType)
                .eq(ApprovalRecord::getBizId, bizId)
                .orderByAsc(ApprovalRecord::getNodeLevel));
    }

    /** 待我审批：当前节点且分配给我的待审记录。 */
    public List<ApprovalRecord> todo(Long approverId) {
        return recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getApproverId, approverId)
                .eq(ApprovalRecord::getResult, ApproveResult.PENDING)
                .orderByAsc(ApprovalRecord::getCreateTime));
    }

    private List<ApprovalFlowNode> nodesOf(Long flowId) {
        return nodeMapper.selectList(new LambdaQueryWrapper<ApprovalFlowNode>()
                .eq(ApprovalFlowNode::getFlowId, flowId)
                .orderByAsc(ApprovalFlowNode::getLevel));
    }

    private List<String> splitIds(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private void notifyApproved(ApprovalBizType bizType, Long bizId) {
        listeners.stream().filter(l -> l.support(bizType)).forEach(l -> l.onApproved(bizId));
    }

    private void notifyRejected(ApprovalBizType bizType, Long bizId, String comment) {
        listeners.stream().filter(l -> l.support(bizType)).forEach(l -> l.onRejected(bizId, comment));
    }
}
