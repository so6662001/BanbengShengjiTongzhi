package com.company.notify.admin.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具：BCrypt 加密/校验。
 * 兼容历史明文密码（非 $2 前缀按明文比较），便于平滑迁移；新用户一律存 BCrypt。
 */
@Component
public class PasswordHelper {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String raw) {
        return encoder.encode(raw);
    }

    public boolean matches(String raw, String stored) {
        if (stored == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return encoder.matches(raw, stored);
        }
        // 历史明文（迁移期兼容）
        return stored.equals(raw);
    }
}
