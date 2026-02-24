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

@Tag(name = "数据分析-渠道统计")
@RestController
@RequestMapping("/api/analysis/channelStats")
public class ChannelStatsController extends CommonCtrl {

    @Autowired
    private PayOrderService payOrderService;

    @Operation(summary = "渠道统计列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "开始时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "createdEnd", description = "结束时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "ifCode", description = "接口代码"),
            @Parameter(name = "ifName", description = "接口名称关键词")
    })
    @PreAuthorize("hasAuthority('ENT_ANALYSIS_CHANNEL_STATS_LIST')")
    @GetMapping("")
    public ApiPageRes<Map> list() {
        String ifCode = getValString("ifCode");
        String ifName = getValString("ifName");
        String createdStart = getValString("createdStart");
        String createdEnd = getValString("createdEnd");
        IPage<Map> page = payOrderService.channelStatsPage(getIPage(), ifCode, ifName, createdStart, createdEnd);
        return ApiPageRes.pages(page);
    }

    @Operation(summary = "渠道详情（按方式）")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "createdStart", description = "开始时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "createdEnd", description = "结束时间(yyyy-MM-dd HH:mm:ss)")
    })
    @PreAuthorize("hasAuthority('ENT_ANALYSIS_CHANNEL_STATS_VIEW')")
    @GetMapping("/{ifCode}/details")
    public ApiRes<List<Map>> details(@PathVariable("ifCode") String ifCode) {
        String createdStart = getValString("createdStart");
        String createdEnd = getValString("createdEnd");
        List<Map> data = payOrderService.channelDetailsByIfCode(ifCode, createdStart, createdEnd);
        return ApiRes.ok(data);
    }
}
