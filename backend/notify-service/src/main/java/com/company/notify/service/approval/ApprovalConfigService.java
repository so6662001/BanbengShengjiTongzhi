package com.company.notify.service.approval;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.domain.entity.ApprovalFlow;
import com.company.notify.domain.entity.ApprovalFlowNode;
import com.company.notify.domain.mapper.ApprovalFlowMapper;
import com.company.notify.domain.mapper.ApprovalFlowNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 审批流配置：按产品线维护审批流与多级节点。 */
@Service
@RequiredArgsConstructor
public class ApprovalConfigService {

    private final ApprovalFlowMapper flowMapper;
    private final ApprovalFlowNodeMapper nodeMapper;

    public List<ApprovalFlow> listFlows() {
        return flowMapper.selectList(new LambdaQueryWrapper<ApprovalFlow>()
                .orderByDesc(ApprovalFlow::getId));
    }

    public List<ApprovalFlowNode> nodesOf(Long flowId) {
        return nodeMapper.selectList(new LambdaQueryWrapper<ApprovalFlowNode>()
                .eq(ApprovalFlowNode::getFlowId, flowId)
                .orderByAsc(ApprovalFlowNode::getLevel));
    }

    /** 保存审批流及其节点（全量覆盖节点）。 */
    @Transactional(rollbackFor = Exception.class)
    public Long saveFlow(ApprovalFlow flow, List<ApprovalFlowNode> nodes) {
        if (flow.getEnabled() == null) {
            flow.setEnabled(true);
        }
        if (flow.getId() == null) {
            flowMapper.insert(flow);
        } else {
            flowMapper.updateById(flow);
            nodeMapper.delete(new LambdaQueryWrapper<ApprovalFlowNode>()
                    .eq(ApprovalFlowNode::getFlowId, flow.getId()));
        }
        if (nodes != null) {
            int level = 1;
            for (ApprovalFlowNode node : nodes) {
                node.setId(null);
                node.setFlowId(flow.getId());
                if (node.getLevel() == null) {
                    node.setLevel(level);
                }
                level++;
                nodeMapper.insert(node);
            }
        }
        return flow.getId();
    }
}
