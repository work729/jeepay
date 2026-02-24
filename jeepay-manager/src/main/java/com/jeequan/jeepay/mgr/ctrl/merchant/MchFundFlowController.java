package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.MchFundFlow;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchFundFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商户资金流水（独立表）")
@RestController
@RequestMapping("/api/mch/fundFlows")
public class MchFundFlowController extends CommonCtrl {

    @Autowired
    private MchFundFlowService fundFlowService;

    @Operation(summary = "资金流水列表")
    @Parameters({
        @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
        @Parameter(name = "pageNumber", description = "分页页码"),
        @Parameter(name = "pageSize", description = "分页条数"),
        @Parameter(name = "mchNo", description = "商户号"),
        @Parameter(name = "bizType", description = "业务类型: 1-支付入账,2-退款出账,3-人工增加,4-人工减少"),
        @Parameter(name = "createdStart", description = "开始时间(yyyy-MM-dd HH:mm:ss)"),
        @Parameter(name = "createdEnd", description = "结束时间(yyyy-MM-dd HH:mm:ss)")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_FINANCE_LIST')")
    @GetMapping
    public ApiPageRes<MchFundFlow> list() {
        MchFundFlow q = getObject(MchFundFlow.class);
        JSONObject params = getReqParamJSON();

        LambdaQueryWrapper<MchFundFlow> w = MchFundFlow.gw();
        if (StringUtils.isNotEmpty(q.getMchNo())) {
            w.eq(MchFundFlow::getMchNo, q.getMchNo());
        }
        if (q.getBizType() != null) {
            w.eq(MchFundFlow::getBizType, q.getBizType());
        }
        if (params != null) {
            String start = params.getString("createdStart");
            String end = params.getString("createdEnd");
            if (StringUtils.isNotEmpty(start)) {
                w.ge(MchFundFlow::getCreatedAt, start);
            }
            if (StringUtils.isNotEmpty(end)) {
                w.le(MchFundFlow::getCreatedAt, end);
            }
        }
        w.orderByDesc(MchFundFlow::getCreatedAt);

        IPage<MchFundFlow> pages = fundFlowService.page(getIPage(), w);
        setMchName(pages.getRecords());
        return ApiPageRes.pages(pages);
    }

    @Operation(summary = "资金流水详情")
    @Parameters({
        @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
        @Parameter(name = "id", description = "记录ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_FINANCE_VIEW')")
    @GetMapping("/{id}")
    public ApiRes<MchFundFlow> detail(@PathVariable("id") Long id) {
        MchFundFlow log = fundFlowService.getById(id);
        return ApiRes.ok(log);
    }
}
