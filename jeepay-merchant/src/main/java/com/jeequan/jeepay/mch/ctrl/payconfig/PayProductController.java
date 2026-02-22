/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0
 */
package com.jeequan.jeepay.mch.ctrl.payconfig;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.PayProductInterfaceModeEnum;
import com.jeequan.jeepay.core.constants.PayProductTypeEnum;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayProduct;
import com.jeequan.jeepay.core.entity.PayProduct;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchPayProductService;
import com.jeequan.jeepay.service.impl.PayProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付产品查看（商户端）
 *
 * 提供当前系统中的支付产品列表数据，供商户中心展示。
 */
@Tag(name = "支付产品查看（商户端）")
@RestController
@RequestMapping("api/payProducts")
public class PayProductController extends CommonCtrl {

    @Autowired
    private PayProductService payProductService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private MchPayProductService mchPayProductService;

    @Operation(summary = "支付产品--列表（商户端）")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_PRODUCT_LIST')")
    @GetMapping
    public ApiRes<ApiPageRes.PageBean<PayProduct>> list() {
        IPage<PayProduct> page = getIPage(true);
        String productName = getValString("productName");
        Integer productType = getValInteger("productType");
        Integer state = getValInteger("state");

        payProductService.page(
                page,
                PayProduct.gw()
                        .like(StringUtils.isNotBlank(productName), PayProduct::getProductName, productName)
                        .eq(productType != null, PayProduct::getProductType, productType)
                        .eq(state != null, PayProduct::getState, state)
                        .orderByAsc(PayProduct::getCreatedAt)
        );

        List<PayProduct> records = page.getRecords();

        if (!records.isEmpty()) {
            String mchNo = getCurrentMchNo();
            MchInfo mchInfo = mchInfoService.getById(mchNo);
            if (mchInfo != null && mchInfo.getId() != null) {
                Long mchId = mchInfo.getId();
                List<Long> productIdList = records.stream()
                        .map(PayProduct::getId)
                        .collect(Collectors.toList());
                List<MchPayProduct> relaList = mchPayProductService.list(
                        MchPayProduct.gw()
                                .eq(MchPayProduct::getMchId, mchId)
                                .in(MchPayProduct::getProductId, productIdList)
                );
                Map<Long, MchPayProduct> relaMap = relaList.stream()
                        .collect(Collectors.toMap(MchPayProduct::getProductId, v -> v, (a, b) -> b));

                for (PayProduct item : records) {
                    MchPayProduct rela = relaMap.get(item.getId());
                    if (rela != null) {
                        if (rela.getMchRate() != null) {
                            item.setMchRate(rela.getMchRate());
                        }
                        if (rela.getState() != null) {
                            item.setState(rela.getState().intValue());
                        }
                    } else {
                        item.setState(0);
                    }
                }
            } else {
                for (PayProduct item : records) {
                    if (item.getState() == null) {
                        item.setState(0);
                    }
                }
            }
        }

        for (PayProduct item : records) {
            PayProductTypeEnum typeEnum = PayProductTypeEnum.fromCode(item.getProductType());
            if (typeEnum != null) {
                item.setProductTypeLabel(typeEnum.getLabel());
            }
            PayProductInterfaceModeEnum modeEnum = PayProductInterfaceModeEnum.fromCode(item.getInterfaceMode());
            if (modeEnum != null) {
                item.setInterfaceModeLabel(modeEnum.getLabel());
            }
        }
        return ApiRes.page(page);
    }
}
