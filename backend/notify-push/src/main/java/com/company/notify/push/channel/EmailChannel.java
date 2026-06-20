package com.company.notify.push.channel;

import com.company.notify.common.enums.Channel;
import com.company.notify.domain.entity.Customer;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮件渠道。notify.email.enabled=true 且配置 spring.mail.* 时通过 JavaMailSender 发送。
 * 收件人取 customer.email。
 */
@Slf4j
@Component
public class EmailChannel implements MessageChannel {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${notify.email.enabled:false}")
    private boolean enabled;

    @Value("${notify.email.from:noreply@example.com}")
    private String from;

    public EmailChannel(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public Channel channel() {
        return Channel.EMAIL;
    }

    @Override
    public boolean send(Customer customer, MessageContent content) {
        if (!enabled) {
            log.info("[邮件-未启用] 跳过客户 {}", customer.getId());
            return false;
        }
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            log.warn("客户[{}]无邮箱，跳过邮件推送", customer.getId());
            return false;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("未配置 JavaMailSender，邮件渠道不可用");
            return false;
        }
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(customer.getEmail());
        mail.setSubject(content.getTitle());
        mail.setText((content.getBody() == null ? "" : content.getBody())
                + (content.getJumpUrl() == null ? "" : "\n\n查看详情：" + content.getJumpUrl()));
        sender.send(mail);
        return true;
    }
}
