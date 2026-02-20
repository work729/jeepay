/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0
 */
package com.jeequan.jeepay.mgr.ctrl.payconfig;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.PayProductInterfaceModeEnum;
import com.jeequan.jeepay.core.constants.PayProductTypeEnum;
import com.jeequan.jeepay.core.entity.PayProduct;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "支付产品管理")
@RestController
@RequestMapping("api/payProducts")
public class PayProductController extends CommonCtrl {

    @Autowired
    private PayProductService payProductService;

    @Operation(summary = "支付产品--列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_LIST')")
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

        for (PayProduct item : page.getRecords()) {
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

    @Operation(summary = "支付产品--详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "主键ID", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_IF_DEFINE_VIEW', 'ENT_PC_IF_DEFINE_EDIT')")
    @GetMapping("/{id}")
    public ApiRes<PayProduct> detail(@PathVariable("id") Long id) {
        return ApiRes.ok(payProductService.getById(id));
    }

    @Operation(summary = "支付产品--新增")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_ADD')")
    @PostMapping
    @MethodLog(remark = "新增支付产品")
    public ApiRes add() {
        PayProduct payProduct = getObject(PayProduct.class);
        boolean result = payProductService.save(payProduct);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    @Operation(summary = "支付产品--更新")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "主键ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_EDIT')")
    @PutMapping("/{id}")
    @MethodLog(remark = "更新支付产品")
    public ApiRes update(@PathVariable("id") Long id) {
        PayProduct payProduct = getObject(PayProduct.class);
        payProduct.setId(id);
        boolean result = payProductService.updateById(payProduct);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @Operation(summary = "支付产品--删除")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "主键ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_DEL')")
    @DeleteMapping("/{id}")
    @MethodLog(remark = "删除支付产品")
    public ApiRes delete(@PathVariable("id") Long id) {
        boolean result = payProductService.removeById(id);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }
}
