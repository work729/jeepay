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
package com.jeequan.jeepay.mch.ctrl.paytest;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayChannel;
import com.jeequan.jeepay.core.entity.PayProductChannel;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.MchPayProductService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.PayChannelService;
import com.jeequan.jeepay.service.impl.PayProductChannelService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
* 支付测试类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/22 9:43
*/
@Tag(name = "支付测试")
@RestController
@RequestMapping("/api/paytest")
public class PaytestController extends CommonCtrl {

    @Autowired private MchAppService mchAppService;
    @Autowired private MchPayPassageService mchPayPassageService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchPayProductService mchPayProductService;
    @Autowired private PayProductChannelService payProductChannelService;
    @Autowired private PayChannelService payChannelService;

    /** 查询商户对应应用下支持的支付方式 **/
    @Operation(summary = "查询商户对应应用下支持的支付方式")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_TEST_PAYWAY_LIST')")
    @GetMapping("/payways/{appId}")
    public ApiRes<Set<String>> payWayList(@PathVariable("appId") String appId) {

        Set<String> payWaySet = new HashSet<>();
        mchPayPassageService.list(
                MchPayPassage.gw().select(MchPayPassage::getWayCode)
                        .eq(MchPayPassage::getMchNo, getCurrentMchNo())
                        .eq(MchPayPassage::getAppId, appId)
                        .eq(MchPayPassage::getState, CS.PUB_USABLE)
        ).stream().forEach(r -> payWaySet.add(r.getWayCode()));

        return ApiRes.ok(payWaySet);
    }

    /** 查询商户默认应用下支持的支付方式（按支付通道测试） **/
    @Operation(summary = "查询商户默认应用下支持的支付方式")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_TEST_PAYWAY_LIST')")
    @GetMapping("/payways")
    public ApiRes<Set<String>> defaultPayWayList() {

        MchApp mchApp = mchAppService.getOne(
                MchApp.gw()
                        .eq(MchApp::getMchNo, getCurrentMchNo())
                        .eq(MchApp::getState, CS.PUB_USABLE)
                        .last("limit 1")
        );

        if (mchApp == null) {
            return ApiRes.ok(new HashSet<>());
        }

        return payWayList(mchApp.getAppId());
    }


    /** 调起下单接口 **/
    @Operation(summary = "调起下单接口")
    @Parameters({
            @Parameter(name = "mchId", description = "商户ID", required = true),
            @Parameter(name = "productId", description = "产品ID", required = true),
            @Parameter(name = "mchOrderNo", description = "商户订单号", required = true),
            @Parameter(name = "amount", description = "转账金额,单位元", required = true),
            @Parameter(name = "notifyUrl", description = "通知地址", required = true),
            @Parameter(name = "returnUrl", description = "页面跳转地址"),
            @Parameter(name = "param2", description = "如透传参数有值,回调会一起返回"),
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_TEST_DO')")
    @PostMapping("/payOrders")
    public ApiRes doPay() {

        //获取请求参数
        Long productId = getValLong("productId");
        Long amount = getRequiredAmountL("amount");
        String mchOrderNo = getValStringRequired("mchOrderNo");
        String wayCode = getValStringRequired("wayCode");

        String orderTitle = getValStringRequired("orderTitle");

        if(StringUtils.isEmpty(orderTitle)){
            throw new BizException("订单标题不能为空");
        }

        // 前端明确了支付参数的类型 payDataType
        String payDataType = getValString("payDataType");
        String authCode = getValString("authCode");


        if (productId != null) {
            String mchNo = getCurrentMchNo();
            MchInfo mchInfo = mchInfoService.getById(mchNo);
            if (mchInfo == null || mchInfo.getId() == null) {
                throw new BizException("商户信息不存在");
            }
            Long mchId = mchInfo.getId();
            boolean exists = mchPayProductService.count(
                    com.jeequan.jeepay.core.entity.MchPayProduct.gw()
                            .eq(com.jeequan.jeepay.core.entity.MchPayProduct::getMchId, mchId)
                            .eq(com.jeequan.jeepay.core.entity.MchPayProduct::getProductId, productId)
            ) > 0;
            if (!exists) {
                throw new BizException("支付产品不存在或不可用");
            }
        }

        if (productId != null) {
            List<PayProductChannel> relations = payProductChannelService.list(
                    PayProductChannel.gw().eq(PayProductChannel::getProductId, productId)
            );
            if (CollectionUtils.isEmpty(relations)) {
                throw new BizException("支付产品未配置任何通道");
            }

            List<Long> channelIds = relations.stream()
                    .map(PayProductChannel::getChannelId)
                    .collect(Collectors.toList());

            List<PayChannel> channelList = payChannelService.list(
                    PayChannel.gw()
                            .in(PayChannel::getId, channelIds)
                            .eq(PayChannel::getState, CS.PUB_USABLE)
            );
            if (CollectionUtils.isEmpty(channelList)) {
                throw new BizException("支付产品通道不可用");
            }
        }

        // 改为直连支付网关统一下单接口（不再使用应用ID/SDK）
        DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();
        String payApi = dbApplicationConfig.getPaySiteUrl() + "/api/pay/unifiedOrder";

        JSONObject body = new JSONObject();
        body.put("mchNo", getCurrentMchNo());
        body.put("mchOrderNo", mchOrderNo);
        body.put("wayCode", wayCode);
        body.put("amount", amount);
        body.put("currency", wayCode.equalsIgnoreCase("pp_pc") ? "USD" : "CNY");
        body.put("clientIp", getClientIp());
        body.put("subject", orderTitle + "[" + getCurrentMchNo() + "商户联调]");
        body.put("body", orderTitle + "[" + getCurrentMchNo() + "商户联调]");
        body.put("notifyUrl", dbApplicationConfig.getMchSiteUrl() + "/api/anon/paytestNotify/payOrder");
        // 可选返回地址
        String returnUrl = getValString("returnUrl");
        if(StringUtils.isNotBlank(returnUrl)){
            body.put("returnUrl", returnUrl);
        }
        // 扩展参数
        JSONObject extParams = new JSONObject();
        if(StringUtils.isNotEmpty(payDataType)) { extParams.put("payDataType", payDataType.trim()); }
        if(StringUtils.isNotEmpty(authCode)) { extParams.put("authCode", authCode.trim()); }
        if(!extParams.isEmpty()){ body.put("channelExtra", extParams.toString()); }
        // 验签占位（服务端在无 appId 情况下不做验签，但要求存在 signType 与 sign 字段）
        body.put("signType", "MD5");
        body.put("sign", "IGNORE");

        org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
        try{
            String respStr = rt.postForObject(payApi, body, String.class);
            JSONObject resp = JSONObject.parseObject(respStr);
            Integer code = resp.getInteger("code");
            String msg = resp.getString("msg");
            if(code == null || code != 0){
                throw new BizException(StringUtils.defaultIfBlank(msg, "请求失败"));
            }
            return ApiRes.ok(resp.getJSONObject("data"));
        }catch (Exception e){
            throw new BizException(e.getMessage());
        }
    }

}
