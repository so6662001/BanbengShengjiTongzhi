package com.company.notify.push;

import lombok.Builder;
import lombok.Data;

/** 推送消息内容（已渲染好的文案）。 */
@Data
@Builder
public class MessageContent {
    private String title;
    private String body;
    private String jumpUrl;
}
