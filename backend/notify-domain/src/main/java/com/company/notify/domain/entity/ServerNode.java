package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.notify.common.enums.CommonStatus;
import com.company.notify.common.enums.Env;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 服务器节点。一台服务器可部署多个产品（见 server_product 关系表）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("server_node")
public class ServerNode extends BaseEntity {
    private String name;
    private String code;
    private Env env;
    private String region;
    private String baseUrl;
    private CommonStatus status;
}
