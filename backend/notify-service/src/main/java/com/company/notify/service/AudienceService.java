package com.company.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.notify.common.exception.BizException;
import com.company.notify.common.exception.ErrorCode;
import com.company.notify.domain.entity.Audience;
import com.company.notify.domain.entity.Customer;
import com.company.notify.domain.entity.CustomerProduct;
import com.company.notify.domain.mapper.AudienceMapper;
import com.company.notify.domain.mapper.CustomerMapper;
import com.company.notify.domain.mapper.CustomerProductMapper;
import com.company.notify.service.dto.AudienceCondition;
import com.company.notify.service.support.JsonUtil;
import com.company.notify.service.support.VersionComparator;
import com.company.notify.service.vo.AudienceHitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户分层圈选：套餐 ∩ 行业 ∩ 产品 ∩ 服务器 ∩ 版本（交集）。
 * 两步过滤避免 join：先按 customer_product 过滤产品/服务器/版本，再按 customer 过滤套餐/行业。
 */
@Service
@RequiredArgsConstructor
public class AudienceService {

    private final CustomerProductMapper customerProductMapper;
    private final CustomerMapper customerMapper;
    private final AudienceMapper audienceMapper;

    public AudienceHitVO hit(AudienceCondition cond) {
        if (cond == null || cond.isEmpty() || cond.getProductId() == null) {
            throw BizException.of(ErrorCode.AUDIENCE_CONDITION_EMPTY);
        }
        // 第一步：按 产品 ∩ 服务器 过滤客户-产品归属
        List<CustomerProduct> cps = customerProductMapper.selectList(new LambdaQueryWrapper<CustomerProduct>()
                .eq(CustomerProduct::getProductId, cond.getProductId())
                .in(cond.getServerNodeIds() != null && !cond.getServerNodeIds().isEmpty(),
                        CustomerProduct::getServerNodeId, cond.getServerNodeIds()));

        // 版本过滤：仅保留当前版本低于目标版本的（待升级）
        if (cond.getVersionBelow() != null && !cond.getVersionBelow().isBlank()) {
            cps = cps.stream()
                    .filter(cp -> cp.getCurrentVersion() == null
                            || VersionComparator.lt(cp.getCurrentVersion(), cond.getVersionBelow()))
                    .toList();
        }
        List<Long> candidateCustomerIds = cps.stream()
                .map(CustomerProduct::getCustomerId).distinct().toList();

        AudienceHitVO vo = new AudienceHitVO();
        if (candidateCustomerIds.isEmpty()) {
            vo.setHitCount(0);
            vo.setCustomerIds(List.of());
            vo.setIndustryDistribution(Map.of());
            return vo;
        }

        // 第二步：按 套餐 ∩ 行业 过滤客户
        List<Customer> customers = customerMapper.selectList(new LambdaQueryWrapper<Customer>()
                .in(Customer::getId, candidateCustomerIds)
                .in(cond.getPackages() != null && !cond.getPackages().isEmpty(),
                        Customer::getPackageLevel, cond.getPackages())
                .in(cond.getIndustries() != null && !cond.getIndustries().isEmpty(),
                        Customer::getIndustry, cond.getIndustries()));

        vo.setHitCount(customers.size());
        vo.setCustomerIds(customers.stream().map(Customer::getId).toList());
        Map<String, Long> dist = customers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getIndustry() == null ? "未知" : c.getIndustry(),
                        LinkedHashMap::new, Collectors.counting()));
        vo.setIndustryDistribution(dist);
        return vo;
    }

    /** 保存人群模板并缓存命中数。 */
    public Long save(String name, AudienceCondition cond) {
        AudienceHitVO hit = hit(cond);
        Audience audience = new Audience();
        audience.setName(name);
        audience.setConditionJson(JsonUtil.toJson(cond));
        audience.setHitCountCache(hit.getHitCount());
        audienceMapper.insert(audience);
        return audience.getId();
    }

    public List<Audience> list() {
        return audienceMapper.selectList(new LambdaQueryWrapper<Audience>()
                .orderByDesc(Audience::getId));
    }

    public Audience getById(Long id) {
        Audience a = audienceMapper.selectById(id);
        if (a == null) {
            throw BizException.of(ErrorCode.DATA_NOT_FOUND, "人群模板不存在");
        }
        return a;
    }

    /** 解析人群条件并返回命中客户 id（投递时使用）。 */
    public List<Long> resolveCustomerIds(Long audienceId) {
        Audience a = getById(audienceId);
        AudienceCondition cond = JsonUtil.parse(a.getConditionJson(), AudienceCondition.class);
        return hit(cond).getCustomerIds();
    }
}
