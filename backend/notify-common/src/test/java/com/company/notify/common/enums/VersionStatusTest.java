package com.company.notify.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 版本状态机合法/非法流转测试。 */
class VersionStatusTest {

    @Test
    void legal_transitions() {
        assertTrue(VersionStatus.DRAFT.canTransferTo(VersionStatus.REVIEW));
        assertTrue(VersionStatus.REVIEW.canTransferTo(VersionStatus.APPROVED));
        assertTrue(VersionStatus.REVIEW.canTransferTo(VersionStatus.DRAFT)); // 驳回退回
        assertTrue(VersionStatus.SCHEDULED.canTransferTo(VersionStatus.PUBLISHED));
        assertTrue(VersionStatus.PUBLISHED.canTransferTo(VersionStatus.REVOKED));
    }

    @Test
    void illegal_transitions() {
        assertFalse(VersionStatus.DRAFT.canTransferTo(VersionStatus.PUBLISHED));
        assertFalse(VersionStatus.PUBLISHED.canTransferTo(VersionStatus.DRAFT));
        assertFalse(VersionStatus.REVOKED.canTransferTo(VersionStatus.PUBLISHED));
    }
}
