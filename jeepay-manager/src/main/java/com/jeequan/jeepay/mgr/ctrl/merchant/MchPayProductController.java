package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.alibaba.fastjson.JSONArray;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayProduct;
import com.jeequan.jeepay.core.entity.PayProduct;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchPayProductService;
import com.jeequan.jeepay.service.impl.PayProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "商户支付产品关联管理")
@RestController
@RequestMapping("/api/mch/payProducts")
public class MchPayProductController extends CommonCtrl {

    @Autowired
    private MchPayProductService mchPayProductService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private PayProductService payProductService;

    @Operation(summary = "查询商户已配置支付产品列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchId", description = "商户ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @GetMapping("/{mchId}")
    public ApiRes<List<MchPayProduct>> list(@PathVariable("mchId") Long mchId) {
        List<MchPayProduct> relations = mchPayProductService.list(
                MchPayProduct.gw().eq(MchPayProduct::getMchId, mchId)
        );
        return ApiRes.ok(relations);
    }

    @Operation(summary = "根据支付产品查询已关联商户列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "productId", description = "支付产品ID", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_IF_DEFINE_VIEW', 'ENT_PC_IF_DEFINE_EDIT')")
    @GetMapping("/by-product/{productId}")
    public ApiRes<List<MchInfo>> listByProduct(@PathVariable("productId") Long productId) {
        List<MchPayProduct> relations = mchPayProductService.list(
                MchPayProduct.gw().eq(MchPayProduct::getProductId, productId)
        );
        if (relations.isEmpty()) {
            return ApiRes.ok(Collections.emptyList());
        }
        List<Long> mchIdList = relations.stream()
                .map(MchPayProduct::getMchId)
                .collect(Collectors.toList());
        List<MchInfo> mchList = mchInfoService.list(
                MchInfo.gw().in(MchInfo::getId, mchIdList)
        );
        PayProduct payProduct = payProductService.getById(productId);
        for (MchInfo mchInfo : mchList) {
            mchInfo.addExt("productId", productId);
            if (payProduct != null) {
                mchInfo.addExt("mchRate", payProduct.getMchRate());
            }
            if (mchInfo.getState() != null) {
                String stateLabel = mchInfo.getState() == 1 ? "启用" : "停用";
                mchInfo.addExt("stateLabel", stateLabel);
            }
        }
        return ApiRes.ok(mchList);
    }

    @Operation(summary = "更新商户支付产品关联关系")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchId", description = "商户ID", required = true),
            @Parameter(name = "relaListStr", description = "关联关系列表，eg：[{'productId':1,'mchRate':1.5,'state':1}]", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @PostMapping("/relas/{mchId}")
    @MethodLog(remark = "更新商户支付产品关联关系")
    public ApiRes relas(@PathVariable("mchId") Long mchId) {
        List<MchPayProduct> relaList = JSONArray.parseArray(getValStringRequired("relaListStr"), MchPayProduct.class);

        mchPayProductService.remove(
                MchPayProduct.gw().eq(MchPayProduct::getMchId, mchId)
        );

        if (!relaList.isEmpty()) {
            relaList.forEach(item -> item.setMchId(mchId));
            mchPayProductService.saveBatch(relaList);
        }

        return ApiRes.ok();
    }
}
