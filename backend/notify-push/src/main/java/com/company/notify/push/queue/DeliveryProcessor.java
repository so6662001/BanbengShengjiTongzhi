package com.company.notify.push.queue;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.Channel;
import com.company.notify.common.enums.ReadStatus;
import com.company.notify.common.enums.SendStatus;
import com.company.notify.domain.entity.Customer;
import com.company.notify.domain.entity.DeliveryRecord;
import com.company.notify.domain.mapper.CustomerMapper;
import com.company.notify.domain.mapper.DeliveryRecordMapper;
import com.company.notify.push.MessageChannel;
import com.company.notify.push.MessageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 投递处理器：消费投递消息，按渠道发送并回写 delivery_record。
 * 进程内队列与 RocketMQ 消费者都复用本处理器，保证逻辑一致。
 * 发送幂等由 delivery_record 唯一键(task+customer+channel)保证；单条失败重试 MAX_ATTEMPTS 次。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryProcessor {

    private static final int MAX_ATTEMPTS = 3;

    private final List<MessageChannel> channels;
    private final DeliveryRecordMapper recordMapper;
    private final CustomerMapper customerMapper;

    private Map<Channel, MessageChannel> channelMap;

    private Map<Channel, MessageChannel> channelMap() {
        if (channelMap == null) {
            channelMap = channels.stream()
                    .collect(Collectors.toMap(MessageChannel::channel, Function.identity()));
        }
        return channelMap;
    }

    public void process(DeliveryMessage msg) {
        MessageChannel channel = channelMap().get(msg.getChannel());
        if (channel == null) {
            log.error("未实现的渠道，丢弃消息: {}", msg.getChannel());
            return;
        }
        MessageContent content = MessageContent.builder()
                .title(msg.getTitle()).body(msg.getBody()).jumpUrl(msg.getJumpUrl()).build();
        for (Long customerId : msg.getCustomerIds()) {
            try {
                sendOne(msg, customerId, content, channel);
            } catch (Exception e) {
                log.error("推送失败 task={} customer={}", msg.getTaskId(), customerId, e);
            }
        }
    }

    private void sendOne(DeliveryMessage msg, Long customerId, MessageContent content, MessageChannel channel) {
        DeliveryRecord record = ensureRecord(msg, customerId);
        if (record.getSendStatus() == SendStatus.SENT) {
            return; // 幂等：已送达不重复发送
        }
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            record.setSendStatus(SendStatus.FAIL);
            record.setFailReason("客户不存在");
            recordMapper.updateById(record);
            return;
        }
        boolean ok = false;
        String lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS && !ok; attempt++) {
            try {
                ok = channel.send(customer, content);
                if (!ok) {
                    lastError = "渠道返回失败(第" + attempt + "次)";
                }
            } catch (Exception e) {
                lastError = "异常(第" + attempt + "次): " + e.getMessage();
                log.warn("推送重试 task={} customer={} attempt={}", msg.getTaskId(), customerId, attempt, e);
            }
        }
        record.setSendStatus(ok ? SendStatus.SENT : SendStatus.FAIL);
        record.setFailReason(ok ? null : lastError);
        recordMapper.updateById(record);
    }

    private DeliveryRecord ensureRecord(DeliveryMessage msg, Long customerId) {
        DeliveryRecord exist = recordMapper.selectOne(new LambdaQueryWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getTaskId, msg.getTaskId())
                .eq(DeliveryRecord::getCustomerId, customerId)
                .eq(DeliveryRecord::getChannel, msg.getChannel())
                .last("limit 1"));
        if (exist != null) {
            return exist;
        }
        DeliveryRecord record = new DeliveryRecord();
        record.setTaskId(msg.getTaskId());
        record.setAnnouncementId(msg.getAnnouncementId());
        record.setCustomerId(customerId);
        record.setChannel(msg.getChannel());
        record.setSendStatus(SendStatus.PENDING);
        record.setReadStatus(ReadStatus.UNREAD);
        recordMapper.insert(record);
        return record;
    }
}
