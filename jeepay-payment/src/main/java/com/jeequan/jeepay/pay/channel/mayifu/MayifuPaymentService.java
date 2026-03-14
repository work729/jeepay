package com.jeequan.jeepay.pay.channel.mayifu;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayChannel;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.CommonPayDataRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.util.ApiResBuilder;
import com.jeequan.jeepay.service.impl.PayChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MayifuPaymentService extends AbstractPaymentService {

    @org.springframework.beans.factory.annotation.Autowired
    private PayChannelService payChannelService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MAYIFU;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        String channelMchId = null;
        String channelKey = null;
        String payUrlBase = null;

        PayChannel payChannel = payChannelService.getOne(
                PayChannel.gw()
                        .eq(PayChannel::getIfCode, getIfCode())
                        .eq(PayChannel::getState, CS.PUB_USABLE)
                        .last("limit 1")
        );

        if (payChannel != null && StringUtils.isNotBlank(payChannel.getChannelSignConfig())) {
            JSONObject cfg = JSONObject.parseObject(payChannel.getChannelSignConfig());
            channelMchId = cfg.getString("mchId");
            channelKey = cfg.getString("APP_KEY");
            payUrlBase = cfg.getString("Gateway");
            // 兼容 gateway + reqApi
            String reqApi = cfg.getString("reqApi");
            if (StringUtils.isNotBlank(payUrlBase) && StringUtils.isNotBlank(reqApi)) {
                String base = payUrlBase;
                String path = reqApi;
                if (!base.endsWith("/")) base = base + "/";
                if (path.startsWith("/")) path = path.substring(1);
                payUrlBase = base + path;
            }
        }

        if (StringUtils.isBlank(channelMchId) || StringUtils.isBlank(channelKey) || StringUtils.isBlank(payUrlBase)) {
            throw new BizException("Mayifu商户参数未配置或不完整");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("mchId", channelMchId);
        String productId = payChannel == null ? null : payChannel.getChannelCode();
        if (StringUtils.isNotBlank(productId)) {
            data.put("productId", productId);
        }
        data.put("mchOrderNo", payOrder.getMchOrderNo());
        data.put("amount", payOrder.getAmount());
        data.put("notifyUrl", getNotifyUrl(payOrder.getPayOrderId()));
        data.put("returnUrl", getReturnUrl(payOrder.getPayOrderId()));

        // 清理空值字段，避免下游表单构造对 null 处理不兼容
        data.entrySet().removeIf(e -> e.getKey() == null || e.getValue() == null || StringUtils.isBlank(String.valueOf(e.getValue())));

        String sign = MayifupayKit.getSign(data, channelKey);
        data.put("sign", sign);

        String url = MayifupayKit.getPaymentUrl(payUrlBase);
        String resStr = "";
        try {
            log.info("Mayifu request url:{}, form:{}", url, data.toString());
            resStr = HttpUtil.createPost(url)
                .timeout(60 * 1000)
                .form(data)
                .execute()
                .body();
            log.info("Mayifu response:{}", resStr);
        } catch (Exception e) {
            log.error("Mayifu http error", e);
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        String payUrl = "";
        if (StringUtils.isEmpty(resStr)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("");
            channelRetMsg.setChannelErrMsg("请求Mayifu接口异常");
        } else {
            JSONObject resObj = null;
            try {
                resObj = JSONObject.parseObject(resStr);
            } catch (Exception parseEx) {
                log.error("Mayifu non-json response: {}", resStr);
            }
            if (resObj == null) {
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode("");
                channelRetMsg.setChannelErrMsg("Mayifu返回非JSON响应");
            } else {
                String retCode = resObj.getString("retCode");
                String retMsg = resObj.getString("retMsg");
                if (!"SUCCESS".equalsIgnoreCase(retCode)) {
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(retCode);
                    channelRetMsg.setChannelErrMsg(retMsg);
                } else {
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    String channelOrderId = resObj.getString("payOrderId");
                    if (StringUtils.isNotBlank(channelOrderId)) {
                        channelRetMsg.setChannelOrderId(channelOrderId);
                    }
                    channelRetMsg.setChannelAttach(resStr);
                    JSONObject payParams = resObj.getJSONObject("payParams");
                    if (payParams != null) {
                        payUrl = StringUtils.defaultIfBlank(payParams.getString("payUrl"), payUrl);
                        payUrl = StringUtils.defaultIfBlank(payParams.getString("pay_url"), payUrl);
                        payUrl = StringUtils.defaultIfBlank(payParams.getString("url"), payUrl);
                    }
                }
            }
        }

        CommonPayDataRS res = ApiResBuilder.buildSuccess(CommonPayDataRS.class);
        res.setPayOrderId(payOrder.getPayOrderId());
        res.setMchOrderNo(payOrder.getMchOrderNo());
        if (StringUtils.isNotBlank(payUrl)) {
            res.setPayUrl(payUrl);
        }
        res.setChannelRetMsg(channelRetMsg);
        return res;
    }
}
