package com.company.notify.push;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.Channel;
import com.company.notify.common.enums.ReadStatus;
import com.company.notify.common.enums.SendStatus;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.Customer;
import com.company.notify.domain.entity.DeliveryRecord;
import com.company.notify.domain.entity.DeliveryTask;
import com.company.notify.domain.mapper.CustomerMapper;
import com.company.notify.domain.mapper.DeliveryRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 推送编排器。当前以 Spring @Async 作为「异步削峰」的进程内替代，
 * 生产可平滑替换为 RocketMQ：dispatch() 改为发送 MQ 消息，消费者侧执行 sendOne()。
 * 发送幂等由 delivery_record 唯一键(task+customer+channel)保证。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushDispatcher {

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

    /** 异步分发一个投递任务到目标客户。 */
    @Async("pushExecutor")
    public void dispatch(DeliveryTask task, List<Long> customerIds, MessageContent content) {
        MessageChannel channel = channelMap().get(task.getChannel());
        if (channel == null) {
            throw BizException.of(ErrorCode.CHANNEL_NOT_SUPPORT, "未实现的渠道: " + task.getChannel());
        }
        for (Long customerId : customerIds) {
            try {
                sendOne(task, customerId, content, channel);
            } catch (Exception e) {
                log.error("推送失败 task={} customer={}", task.getId(), customerId, e);
            }
        }
    }

    private void sendOne(DeliveryTask task, Long customerId, MessageContent content, MessageChannel channel) {
        DeliveryRecord record = ensureRecord(task, customerId);
        if (record.getSendStatus() == SendStatus.SENT) {
            return; // 幂等：已送达不重复发送
        }
        Customer customer = customerMapper.selectById(customerId);
        boolean ok = customer != null && channel.send(customer, content);
        record.setSendStatus(ok ? SendStatus.SENT : SendStatus.FAIL);
        if (!ok) {
            record.setFailReason("渠道发送失败或客户不存在");
        }
        recordMapper.updateById(record);
    }

    /** 幂等创建/获取投递记录。 */
    private DeliveryRecord ensureRecord(DeliveryTask task, Long customerId) {
        DeliveryRecord exist = recordMapper.selectOne(new LambdaQueryWrapper<DeliveryRecord>()
                .eq(DeliveryRecord::getTaskId, task.getId())
                .eq(DeliveryRecord::getCustomerId, customerId)
                .eq(DeliveryRecord::getChannel, task.getChannel())
                .last("limit 1"));
        if (exist != null) {
            return exist;
        }
        DeliveryRecord record = new DeliveryRecord();
        record.setTaskId(task.getId());
        record.setAnnouncementId(task.getAnnouncementId());
        record.setCustomerId(customerId);
        record.setChannel(task.getChannel());
        record.setSendStatus(SendStatus.PENDING);
        record.setReadStatus(ReadStatus.UNREAD);
        recordMapper.insert(record);
        return record;
    }
}
