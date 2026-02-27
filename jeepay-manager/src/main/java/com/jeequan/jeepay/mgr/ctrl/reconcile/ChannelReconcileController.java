/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 */
package com.jeequan.jeepay.mgr.ctrl.reconcile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "渠道对账")
@RestController
@RequestMapping("/api/reconcile/channelDaily")
public class ChannelReconcileController extends CommonCtrl {

    @Autowired private PayOrderService payOrderService;
    @Autowired private TransferOrderService transferOrderService;

    @Operation(summary = "渠道维度日对账分页")
    @Parameters({})
    @RequestMapping(method = RequestMethod.GET)
    public ApiRes pageChannelDaily() {
        String date = getValStringRequired("date");
        String ifName = getValString("ifName");
        String createdStart = date + " 00:00:00";
        String createdEnd = date + " 23:59:59";

        IPage<Map> page = payOrderService.channelStatsPage(getIPage(), null, ifName, createdStart, createdEnd);

        List<Map> payoutList = transferOrderService.dailyPayoutByIfCode(createdStart, createdEnd);
        Map<String, Object> payoutMap = new HashMap<>();
        if (payoutList != null) {
            for (Map m : payoutList) {
                payoutMap.put(String.valueOf(m.get("ifCode")), m.get("payoutAmount"));
            }
        }

        for (Map record : page.getRecords()) {
            Object credited = record.get("creditedAmount");
            record.put("creditAmount", credited);
            Object tradeCount = record.get("rechargeCount");
            record.put("tradeCount", tradeCount);
            String ifCode = String.valueOf(record.get("ifCode"));
            double payoutAmount = payoutMap.get(ifCode) == null ? 0D : Double.parseDouble(String.valueOf(payoutMap.get(ifCode)));
            double creditAmount = credited == null ? 0D : Double.parseDouble(String.valueOf(credited));
            record.put("payoutAmount", payoutAmount);
            record.put("diffAmount", creditAmount - payoutAmount);
        }

        return ApiRes.page(page);
    }

    @Operation(summary = "某渠道下通道维度对账详情")
    @Parameters({})
    @RequestMapping(value = "/{ifCode}/channels", method = RequestMethod.GET)
    public ApiRes<List<Map>> channelDetail(@PathVariable("ifCode") String ifCode) {
        String date = getValStringRequired("date");
        String createdStart = date + " 00:00:00";
        String createdEnd = date + " 23:59:59";
        List<Map> list = payOrderService.channelReconcileDetailsByIfCode(ifCode, createdStart, createdEnd);
        return ApiRes.ok(list);
    }
}
