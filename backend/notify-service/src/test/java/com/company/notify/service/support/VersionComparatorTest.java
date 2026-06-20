package com.company.notify.service.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 版本比较口径测试，保障升级完成率/待升级筛选正确。 */
class VersionComparatorTest {

    @Test
    void gte_should_handle_prefix_and_segments() {
        assertTrue(VersionComparator.gte("v3.2.0", "v3.2.0"));
        assertTrue(VersionComparator.gte("3.2.1", "v3.2.0"));
        assertTrue(VersionComparator.gte("v3.10.0", "v3.9.9"));
        assertFalse(VersionComparator.gte("v3.1.9", "v3.2.0"));
    }

    @Test
    void lt_should_detect_pending_upgrade() {
        assertTrue(VersionComparator.lt("v3.1.8", "v3.2.0"));
        assertFalse(VersionComparator.lt("v3.2.0", "v3.2.0"));
    }
}
