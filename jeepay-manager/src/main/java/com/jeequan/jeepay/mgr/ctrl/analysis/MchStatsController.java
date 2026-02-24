package com.jeequan.jeepay.mgr.ctrl.analysis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据分析-商户统计")
@RestController
@RequestMapping("/api/analysis/mchStats")
public class MchStatsController extends CommonCtrl {

    @Autowired
    private PayOrderService payOrderService;

    @Operation(summary = "商户统计列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "开始时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "createdEnd", description = "结束时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "mchNo", description = "商户号"),
            @Parameter(name = "mchName", description = "商户名称关键词")
    })
    @PreAuthorize("hasAuthority('ENT_ANALYSIS_MCH_STATS_LIST')")
    @GetMapping("")
    public ApiPageRes<Map> list() {
        String mchNo = getValString("mchNo");
        String mchName = getValString("mchName");
        String createdStart = getValString("createdStart");
        String createdEnd = getValString("createdEnd");
        IPage<Map> page = payOrderService.mchStatsPage(getIPage(), mchNo, mchName, createdStart, createdEnd);
        return ApiPageRes.pages(page);
    }

    @Operation(summary = "商户通道成功率统计")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "createdStart", description = "开始时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "createdEnd", description = "结束时间(yyyy-MM-dd HH:mm:ss)")
    })
    @PreAuthorize("hasAuthority('ENT_ANALYSIS_MCH_STATS_VIEW')")
    @GetMapping("/{mchNo}/channels")
    public ApiRes<List<Map>> channelStats(@PathVariable("mchNo") String mchNo) {
        String createdStart = getValString("createdStart");
        String createdEnd = getValString("createdEnd");
        List<Map> data = payOrderService.mchChannelSuccessStats(mchNo, createdStart, createdEnd);
        return ApiRes.ok(data);
    }
}
