package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.CommonStatus;
import com.company.notify.common.enums.PackageLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 客户 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {
    private String name;
    private String industry;
    private PackageLevel packageLevel;
    private CommonStatus status;
    /** 企业微信接收人 userid 列表，逗号分隔 */
    private String wecomUserIds;
    /** 邮件收件人（邮件渠道用） */
    private String email;
    /** 手机号（短信渠道用） */
    private String phone;
}
