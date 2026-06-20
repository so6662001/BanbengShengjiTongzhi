package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.CommonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 后台用户（内部员工：产品经理/产品线负责人/合规等审批角色） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    /** 角色编码，逗号分隔，如 PM,PRODUCT_OWNER,COMPLIANCE */
    private String roles;
    private CommonStatus status;
}
