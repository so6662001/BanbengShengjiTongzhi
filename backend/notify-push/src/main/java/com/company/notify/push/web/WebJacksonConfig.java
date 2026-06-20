package com.company.notify.push.web;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;

/**
 * 雪花算法 Long 主键(19 位) 超出 JavaScript 安全整数(2^53)范围，前端会丢精度。
 * 故统一把 Long/long/BigInteger 序列化为字符串，前端按字符串透传，@PathVariable Long 可正常反序列化。
 */
@Configuration
public class WebJacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            module.addSerializer(BigInteger.class, ToStringSerializer.instance);
            builder.modulesToInstall(module); // 追加而非替换，保留 JavaTimeModule 等
        };
    }
}
