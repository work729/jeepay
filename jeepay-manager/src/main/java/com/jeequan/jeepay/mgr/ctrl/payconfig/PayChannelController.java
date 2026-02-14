/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0
 */
package com.jeequan.jeepay.mgr.ctrl.payconfig;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayChannel;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "支付通道管理")
@RestController
@RequestMapping("api/payChannels")
public class PayChannelController extends CommonCtrl {

    @Autowired
    private PayChannelService payChannelService;

    @Operation(summary = "支付通道--列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_LIST')")
    @GetMapping
    public ApiRes<ApiPageRes.PageBean<PayChannel>> list() {
        IPage<PayChannel> page = getIPage(true);
        String channelName = getValString("channelName");
        String ifCode = getValString("ifCode");
        String channelSign = getValString("channelSign");
        Integer state = getValInteger("state");

        payChannelService.page(
                page,
                PayChannel.gw()
                        .like(StringUtils.isNotBlank(channelName), PayChannel::getChannelName, channelName)
                        .like(StringUtils.isNotBlank(ifCode), PayChannel::getIfCode, ifCode)
                        .eq(StringUtils.isNotBlank(channelSign), PayChannel::getChannelSign, channelSign)
                        .eq(state != null, PayChannel::getState, state)
                        .orderByAsc(PayChannel::getCreatedAt)
        );
        return ApiRes.page(page);
    }

    @Operation(summary = "支付通道--详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "channelSign", description = "通道标识", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_IF_DEFINE_VIEW', 'ENT_PC_IF_DEFINE_EDIT')")
    @GetMapping("/{channelSign}")
    public ApiRes<PayChannel> detail(@PathVariable("channelSign") String channelSign) {
        return ApiRes.ok(payChannelService.getById(channelSign));
    }

    @Operation(summary = "支付通道--新增")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_ADD')")
    @PostMapping
    @MethodLog(remark = "新增支付通道")
    public ApiRes add() {
        PayChannel payChannel = getObject(PayChannel.class);
        boolean result = payChannelService.save(payChannel);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    @Operation(summary = "支付通道--更新")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "channelSign", description = "通道标识", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_EDIT')")
    @PutMapping("/{channelSign}")
    @MethodLog(remark = "更新支付通道")
    public ApiRes update(@PathVariable("channelSign") String channelSign) {
        PayChannel payChannel = getObject(PayChannel.class);
        payChannel.setChannelSign(channelSign);
        boolean result = payChannelService.updateById(payChannel);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @Operation(summary = "支付通道--删除")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "channelSign", description = "通道标识", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_DEL')")
    @DeleteMapping("/{channelSign}")
    @MethodLog(remark = "删除支付通道")
    public ApiRes delete(@PathVariable("channelSign") String channelSign) {
        boolean result = payChannelService.removeById(channelSign);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }
}
