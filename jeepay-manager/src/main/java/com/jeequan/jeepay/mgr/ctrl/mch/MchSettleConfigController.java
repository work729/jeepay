package com.jeequan.jeepay.mgr.ctrl.mch;

import com.jeequan.jeepay.core.entity.MchSettleConfig;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchSettleConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商户结算设置")
@RestController
@RequestMapping("api/mch/settle")
public class MchSettleConfigController extends CommonCtrl {

    @Autowired
    private MchSettleConfigService mchSettleConfigService;

    @Operation(summary = "查询商户结算设置")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchNo", description = "商户号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @RequestMapping(value = "/{mchNo}", method = RequestMethod.GET)
    public ApiRes<MchSettleConfig> getConfig(@PathVariable("mchNo") String mchNo) {
        MchSettleConfig config = mchSettleConfigService.getById(mchNo);
        if (config == null) {
            config = new MchSettleConfig();
            config.setMchNo(mchNo);
            config.setInheritSystem((byte) 1);
        }
        return ApiRes.ok(config);
    }

    @Operation(summary = "保存商户结算设置")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes saveConfig(@RequestBody MchSettleConfig reqModel) {
        if (reqModel == null || reqModel.getMchNo() == null) {
            return ApiRes.customFail("商户号不能为空");
        }
        if (reqModel.getInheritSystem() == null) {
            reqModel.setInheritSystem((byte) 1);
        }
        mchSettleConfigService.saveOrUpdate(reqModel);
        return ApiRes.ok();
    }
}
