package com.company.notify.service.support;

/**
 * 语义化版本比较工具。支持 "v3.2.0" / "3.2.0" 形式，逐段数字比较。
 * 升级完成率、待升级筛选均依赖此比较口径。
 */
public final class VersionComparator {

    private VersionComparator() {}

    /** a 大于等于 b 返回 true（即 a 已达到或超过 b） */
    public static boolean gte(String a, String b) {
        return compare(a, b) >= 0;
    }

    /** a 小于 b 返回 true（即 a 尚未升级到 b） */
    public static boolean lt(String a, String b) {
        return compare(a, b) < 0;
    }

    public static int compare(String a, String b) {
        int[] pa = parse(a);
        int[] pb = parse(b);
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int va = i < pa.length ? pa[i] : 0;
            int vb = i < pb.length ? pb[i] : 0;
            if (va != vb) {
                return Integer.compare(va, vb);
            }
        }
        return 0;
    }

    private static int[] parse(String v) {
        if (v == null || v.isBlank()) {
            return new int[]{0};
        }
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1);
        }
        String[] parts = s.split("[.\\-_]");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                nums[i] = Integer.parseInt(parts[i].replaceAll("\\D", ""));
            } catch (NumberFormatException e) {
                nums[i] = 0;
            }
        }
        return nums;
    }
}
