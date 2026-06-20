package com.company.notify.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.admin.security.JwtUtil;
import com.company.notify.admin.security.PasswordHelper;
import com.company.notify.common.api.Result;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.SysUser;
import com.company.notify.domain.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "登录鉴权")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordHelper passwordHelper;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginReq req) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()).last("limit 1"));
        // BCrypt 校验（兼容历史明文，见 PasswordHelper）
        if (user == null || !passwordHelper.matches(req.getPassword(), user.getPassword())) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("roles", user.getRoles());
        return Result.success(data);
    }

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }
}
