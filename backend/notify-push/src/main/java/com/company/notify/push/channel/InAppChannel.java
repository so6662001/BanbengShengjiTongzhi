package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import org.springframework.stereotype.Component;

/**
 * 站内信渠道：内容已通过 delivery_record 落库，客户端启动时拉取。
 * 故此处仅标记“已送达”，真正的展示由客户端接口完成。
 */
@Component
public class InAppChannel implements MessageChannel {

    @Override
    public Channel channel() {
        return Channel.IN_APP;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        // 站内信无外部调用，落库即送达成功。
        return true;
    }
}
