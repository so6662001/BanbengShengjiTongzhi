package com.company.notify.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 真实 HTTP 联调冒烟：启动内嵌 Tomcat（随机端口），经真实 socket 调用接口。
 * 验证 登录鉴权 + 鉴权拦截器 + 端到端 HTTP 链路真实可用（非 MockMvc）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LiveHttpSmokeTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    @Test
    void login_and_authorized_call_over_real_http() throws Exception {
        // 1. 未带 token 访问受保护接口 → 业务码 2001
        ResponseEntity<String> noAuth = rest.getForEntity(url("/admin/product"), String.class);
        assertEquals(2001, om.readTree(noAuth.getBody()).path("code").asInt());

        // 2. 真实 HTTP 登录
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> login = rest.postForEntity(url("/admin/auth/login"),
                new HttpEntity<>("{\"username\":\"admin\",\"password\":\"admin123\"}", h), String.class);
        JsonNode loginData = om.readTree(login.getBody()).path("data");
        String token = loginData.path("token").asText();
        assertFalse(token.isBlank(), "应返回 JWT");

        // 3. 带 token 访问受保护接口 → 成功并返回产品列表
        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        ResponseEntity<String> products = rest.exchange(url("/admin/product"),
                HttpMethod.GET, new HttpEntity<>(auth), String.class);
        JsonNode body = om.readTree(products.getBody());
        assertEquals(0, body.path("code").asInt());
        assertTrue(body.path("data").size() >= 1, "种子数据应至少含一个产品");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
