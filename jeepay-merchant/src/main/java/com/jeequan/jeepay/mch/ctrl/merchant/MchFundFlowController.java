/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.mch.ctrl.merchant;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.MchFundFlow;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchFundFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 商户资金流水
 *
 * @author jeepay
 */
@Tag(name = "商户资金流水")
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
            @Parameter(name = "createdStart", description = "开始时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "createdEnd", description = "结束时间(yyyy-MM-dd HH:mm:ss)"),
            @Parameter(name = "bizType", description = "业务类型: 1-支付入账,2-退款出账,3-人工增加,4-人工减少"),
    })
    @PreAuthorize("hasAuthority('ENT_MCH_FINANCE_LIST')")
    @GetMapping
    public ApiPageRes<MchFundFlow> list() {
        JSONObject paramJSON = getReqParamJSON();
        Byte bizType = paramJSON.getByte("bizType");
        String createdStart = paramJSON.getString("createdStart");
        String createdEnd = paramJSON.getString("createdEnd");

        LambdaQueryWrapper<MchFundFlow> wrapper = MchFundFlow.gw();
        wrapper.eq(MchFundFlow::getMchNo, getCurrentMchNo());
        if (bizType != null) {
            wrapper.eq(MchFundFlow::getBizType, bizType);
        }
        if (createdStart != null && createdEnd != null) {
            wrapper.ge(MchFundFlow::getCreatedAt, createdStart);
            wrapper.le(MchFundFlow::getCreatedAt, createdEnd);
        }
        wrapper.orderByDesc(MchFundFlow::getCreatedAt);
        IPage<MchFundFlow> pages = fundFlowService.page(getIPage(), wrapper);
        return ApiPageRes.pages(pages);
    }

    @Operation(summary = "资金流水详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "流水ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_FINANCE_VIEW')")
    @GetMapping("/{id}")
    public ApiRes<MchFundFlow> detail(@PathVariable("id") Long id) {
        MchFundFlow flow = fundFlowService.getById(id);
        if (flow == null || !getCurrentMchNo().equals(flow.getMchNo())) {
            return ApiRes.fail(com.jeequan.jeepay.core.constants.ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(flow);
    }
}
