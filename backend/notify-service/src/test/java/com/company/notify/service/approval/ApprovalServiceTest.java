package com.company.notify.service.approval;

import com.company.notify.common.enums.ApprovalBizType;
import com.company.notify.common.enums.ApproveMode;
import com.company.notify.common.enums.ApproveResult;
import com.company.notify.common.exception.BizException;
import com.company.notify.domain.entity.ApprovalFlowNode;
import com.company.notify.domain.entity.ApprovalRecord;
import com.company.notify.domain.mapper.ApprovalFlowMapper;
import com.company.notify.domain.mapper.ApprovalFlowNodeMapper;
import com.company.notify.domain.mapper.ApprovalRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * 审批引擎核心流转测试（Mockito）：
 * - 单级通过 → 触发 onApproved
 * - 驳回 → 触发 onRejected
 * - 非当前节点审批人 → 拒绝
 */
class ApprovalServiceTest {

    private ApprovalFlowMapper flowMapper;
    private ApprovalFlowNodeMapper nodeMapper;
    private ApprovalRecordMapper recordMapper;
    private RecordingListener listener;
    private ApprovalService service;

    @BeforeEach
    void setup() {
        flowMapper = mock(ApprovalFlowMapper.class);
        nodeMapper = mock(ApprovalFlowNodeMapper.class);
        recordMapper = mock(ApprovalRecordMapper.class);
        listener = new RecordingListener();
        service = new ApprovalService(flowMapper, nodeMapper, recordMapper, List.of(listener));
    }

    @Test
    void single_node_pass_triggers_approved() {
        ApprovalRecord record = record(10L, ApprovalBizType.VERSION, 99L, 1, 2L, ApproveResult.PENDING);
        when(recordMapper.selectById(10L)).thenReturn(record);
        // 当前层级查询：返回该 PENDING 记录
        when(recordMapper.selectList(ArgumentMatchers.any()))
                .thenReturn(List.of(record))  // currentLevel()
                .thenReturn(List.of(record)); // advanceIfNodeComplete -> levelRecords
        ApprovalFlowNode node = new ApprovalFlowNode();
        node.setLevel(1);
        node.setApproveMode(ApproveMode.SINGLE);
        when(nodeMapper.selectOne(ArgumentMatchers.any())).thenReturn(node);
        when(nodeMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(node)); // 无下一级

        service.approve(10L, 2L, true, "ok");

        assertEquals(99L, listener.approvedId);
    }

    @Test
    void reject_triggers_rejected() {
        ApprovalRecord record = record(11L, ApprovalBizType.VERSION, 88L, 1, 2L, ApproveResult.PENDING);
        when(recordMapper.selectById(11L)).thenReturn(record);
        when(recordMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(record));

        service.approve(11L, 2L, false, "文案有问题");

        assertEquals(88L, listener.rejectedId);
        assertEquals("文案有问题", listener.rejectComment);
    }

    @Test
    void wrong_approver_is_denied() {
        ApprovalRecord record = record(12L, ApprovalBizType.VERSION, 77L, 1, 2L, ApproveResult.PENDING);
        when(recordMapper.selectById(12L)).thenReturn(record);
        assertThrows(BizException.class, () -> service.approve(12L, 999L, true, null));
    }

    private ApprovalRecord record(Long id, ApprovalBizType type, Long bizId, int level, Long approver, ApproveResult result) {
        ApprovalRecord r = new ApprovalRecord();
        r.setId(id);
        r.setBizType(type);
        r.setBizId(bizId);
        r.setNodeLevel(level);
        r.setApproverId(approver);
        r.setResult(result);
        r.setFlowId(1L);
        return r;
    }

    /** 记录回调结果的测试监听器。 */
    static class RecordingListener implements ApprovalListener {
        Long approvedId;
        Long rejectedId;
        String rejectComment;

        @Override
        public boolean support(ApprovalBizType bizType) {
            return true;
        }

        @Override
        public void onApproved(Long bizId) {
            this.approvedId = bizId;
        }

        @Override
        public void onRejected(Long bizId, String comment) {
            this.rejectedId = bizId;
            this.rejectComment = comment;
        }
    }
}
