package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.enums.CommonStatus;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.ServerNode;
import com.company.notify.domain.entity.ServerProduct;
import com.company.notify.domain.mapper.ServerNodeMapper;
import com.company.notify.domain.mapper.ServerProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/** 服务器管理。一台服务器可部署多个产品（server_product 维护多对多）。 */
@Service
@RequiredArgsConstructor
public class ServerNodeService {

    private final ServerNodeMapper serverNodeMapper;
    private final ServerProductMapper serverProductMapper;

    public List<ServerNode> list() {
        return serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>()
                .orderByDesc(ServerNode::getId));
    }

    /** 查询某服务器部署的产品 id 列表 */
    public List<Long> productIdsOfServer(Long serverNodeId) {
        return serverProductMapper.selectList(new LambdaQueryWrapper<ServerProduct>()
                        .eq(ServerProduct::getServerNodeId, serverNodeId))
                .stream().map(ServerProduct::getProductId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(ServerNode node, List<Long> productIds) {
        ensureCodeUnique(node.getCode(), null);
        if (node.getStatus() == null) {
            node.setStatus(CommonStatus.ENABLED);
        }
        serverNodeMapper.insert(node);
        bindProducts(node.getId(), productIds);
        return node.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(ServerNode node, List<Long> productIds) {
        if (serverNodeMapper.selectById(node.getId()) == null) {
            throw BizException.of(ErrorCode.DATA_NOT_FOUND, "服务器不存在");
        }
        ensureCodeUnique(node.getCode(), node.getId());
        serverNodeMapper.updateById(node);
        // 全量覆盖绑定关系
        serverProductMapper.delete(new LambdaQueryWrapper<ServerProduct>()
                .eq(ServerProduct::getServerNodeId, node.getId()));
        bindProducts(node.getId(), productIds);
    }

    private void bindProducts(Long serverNodeId, List<Long> productIds) {
        if (productIds == null) {
            return;
        }
        for (Long pid : productIds) {
            ServerProduct sp = new ServerProduct();
            sp.setServerNodeId(serverNodeId);
            sp.setProductId(pid);
            serverProductMapper.insert(sp);
        }
    }

    private void ensureCodeUnique(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            return;
        }
        Long count = serverNodeMapper.selectCount(new LambdaQueryWrapper<ServerNode>()
                .eq(ServerNode::getCode, code)
                .ne(excludeId != null, ServerNode::getId, excludeId));
        if (count != null && count > 0) {
            throw BizException.of(ErrorCode.DATA_CONFLICT, "服务器编码已存在");
        }
    }

    public List<ServerProduct> bindingsOfServer(Long serverNodeId) {
        if (serverNodeId == null) {
            return Collections.emptyList();
        }
        return serverProductMapper.selectList(new LambdaQueryWrapper<ServerProduct>()
                .eq(ServerProduct::getServerNodeId, serverNodeId));
    }
}
