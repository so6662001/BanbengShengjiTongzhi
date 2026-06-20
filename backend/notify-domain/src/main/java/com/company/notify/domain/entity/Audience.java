package com.company.notify.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 人群模板。conditionJson 保存圈选条件（套餐/行业/产品/服务器/版本）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audience")
public class Audience extends BaseEntity {
    private String name;
    private String conditionJson;
    private Long hitCountCache;
}
