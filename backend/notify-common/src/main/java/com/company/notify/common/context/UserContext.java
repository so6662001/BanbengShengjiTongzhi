package com.company.notify.common.context;

/**
 * 当前登录用户上下文（后台）。由鉴权拦截器设置，用于审计字段填充与权限判断。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(CurrentUser user) { HOLDER.set(user); }

    public static CurrentUser get() { return HOLDER.get(); }

    public static Long getUserId() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.userId();
    }

    public static void clear() { HOLDER.remove(); }

    public record CurrentUser(Long userId, String username, java.util.Set<String> roles) {}
}
