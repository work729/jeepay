package com.jeequan.jeepay.mgr.ctrl.analysis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "对账管理-商户对账")
@RestController
@RequestMapping("/api/reconcile/mchDaily")
public class MchReconcileController extends CommonCtrl {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private TransferOrderService transferOrderService;

    @Operation(summary = "商户日对账列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "date", description = "统计日期(yyyy-MM-dd)")
    })
    @PreAuthorize("hasAuthority('ENT_RECONCILE_MCH_LIST')")
    @GetMapping("")
    public ApiPageRes<Map> list() {
        String date = getValString("date");
        String createdStart = null;
        String createdEnd = null;
        if (date != null && !"".equals(date)) {
            createdStart = date + " 00:00:00";
            createdEnd = date + " 23:59:59";
        }
        IPage<Map> page = payOrderService.mchStatsPage(getIPage(), null, null, createdStart, createdEnd);
        List<Map> payoutList = transferOrderService.dailyPayoutByMch(createdStart, createdEnd);
        Map<String, Object> payoutMap = new HashMap<>();
        for (Map item : payoutList) {
            payoutMap.put(String.valueOf(item.get("mchNo")), item.get("payoutAmount"));
        }
        for (Map record : page.getRecords()) {
            Object creditedAmount = record.get("creditedAmount");
            Object rechargeCount = record.get("rechargeCount");
            Object payoutAmountObj = payoutMap.get(String.valueOf(record.get("mchNo")));
            double payoutAmount = payoutAmountObj == null ? 0D : Double.parseDouble(String.valueOf(payoutAmountObj));
            double credit = creditedAmount == null ? 0D : Double.parseDouble(String.valueOf(creditedAmount));
            record.put("payoutAmount", payoutAmount);
            record.put("creditAmount", credit);
            record.put("diffAmount", credit - payoutAmount);
            record.put("tradeCount", rechargeCount == null ? 0 : rechargeCount);
            record.put("pinTop", false);
        }
        return ApiPageRes.pages(page);
    }

    @Operation(summary = "商户当日产品对账详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "date", description = "统计日期(yyyy-MM-dd)")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_VIEW')")
    @GetMapping("/{mchNo}/products")
    public ApiRes<List<Map>> productStats(@PathVariable("mchNo") String mchNo) {
        String date = getValString("date");
        String createdStart = null;
        String createdEnd = null;
        if (date != null && !"".equals(date)) {
            createdStart = date + " 00:00:00";
            createdEnd = date + " 23:59:59";
        }
        List<Map> data = payOrderService.productStatsByMchForDay(mchNo, createdStart, createdEnd);
        return ApiRes.ok(data);
    }
}
