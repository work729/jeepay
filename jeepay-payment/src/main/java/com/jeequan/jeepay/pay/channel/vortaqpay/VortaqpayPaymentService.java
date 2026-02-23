package com.jeequan.jeepay.pay.channel.vortaqpay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayChannel;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.vortaqpay.VortaqpayNormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
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
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class VortaqpayPaymentService extends AbstractPaymentService {

    @org.springframework.beans.factory.annotation.Autowired
    private PayChannelService payChannelService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.VORTAQPAY;
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
        VortaqpayNormalMchParams params = null;
        String headerAppId = null;
        String headerApiVersion = "1.0";

        PayChannel payChannel = payChannelService.getOne(
                PayChannel.gw()
                        .eq(PayChannel::getIfCode, getIfCode())
                        .eq(PayChannel::getState, CS.PUB_USABLE)
                        .last("limit 1")
        );

        if (payChannel != null && StringUtils.isNotBlank(payChannel.getChannelSignConfig())) {
            JSONObject cfg = JSONObject.parseObject(payChannel.getChannelSignConfig());
            params = cfg.toJavaObject(VortaqpayNormalMchParams.class);

            String gateway = StringUtils.defaultIfBlank(cfg.getString("Gateway"), cfg.getString("gateway"));
            String reqApi = StringUtils.defaultIfBlank(cfg.getString("reqApi"), cfg.getString("req_api"));
            if (StringUtils.isNotBlank(gateway) && StringUtils.isNotBlank(reqApi)) {
                String base = gateway;
                String path = reqApi;
                if (!base.endsWith("/")) {
                    base = base + "/";
                }
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                params.setPayUrl(base + path);
            }

            headerAppId = cfg.getString("APP_ID");
            headerApiVersion = StringUtils.defaultIfBlank(cfg.getString("ApiVersion"), headerApiVersion);
        }

        if (params == null) {
            params = (VortaqpayNormalMchParams) configContextQueryService
                    .queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());
        }

        if (params == null) {
            throw new BizException("Vortaqpay商户参数未配置");
        }

        if (StringUtils.isBlank(headerAppId)) {
            throw new BizException("Vortaqpay通道AppId未配置");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("country", params.getCountry());
        data.put("currency", params.getCurrency());
        data.put("payment_method_id", params.getPaymentMethodId());
        data.put("payment_method_flow", params.getPaymentMethodFlow());
        data.put("order_id", payOrder.getPayOrderId());
        String amountStr = AmountUtil.convertCent2Dollar(payOrder.getAmount().toString());
        data.put("amount", amountStr);
        data.put("notification_url", getNotifyUrl(payOrder.getPayOrderId()));
        data.put("success_redirect_url", getReturnUrl(payOrder.getPayOrderId()));
        if (StringUtils.isNotBlank(payOrder.getExtParam())) {
            data.put("extend", payOrder.getExtParam());
        }
        data.put("timestamp", System.currentTimeMillis());

        String sign = VortaqpayKit.getSign(data, params.getKey());
        data.put("signature", sign);

        JSONObject body = new JSONObject();
        JSONObject cfg = JSONObject.parseObject(payChannel.getChannelSignConfig());
        body.put("merchant_no", cfg.getString("MERCHANT_NO"));
        body.put("data", data);

        String url = VortaqpayKit.getPaymentUrl(params.getPayUrl());
        String resStr = "";
        String noncestr = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 100000000));
        String timestamp = String.valueOf(System.currentTimeMillis());
        try {
            log.info("vortaqpay request url:{}, body:{}", url, body.toJSONString());
            resStr = HttpUtil.createPost(url)
                    .timeout(60 * 1000)
                    .header("Content-Type", "application/json")
                    .header("ApiVersion", headerApiVersion)
                    .header("AppId", headerAppId)
                    .header("Noncestr", noncestr)
                    .header("Timestamp", timestamp)
                    .body(body.toJSONString())
                    .execute()
                    .body();
            log.info("vortaqpay response:{}", resStr);
        } catch (Exception e) {
            log.error("vortaqpay http error", e);
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        String payUrl = "";
        if (StringUtils.isEmpty(resStr)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("");
            channelRetMsg.setChannelErrMsg("请求vortaqpay接口异常");
        } else {
            String trimmed = resStr.trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                log.error("vortaqpay non-json response: {}", trimmed);
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode("");
                channelRetMsg.setChannelErrMsg("Vortaqpay返回非JSON响应");
            } else {
                JSONObject resObj = JSONObject.parseObject(resStr);
                String state = resObj.getString("state");
                if ("fail".equalsIgnoreCase(state)) {
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(resObj.getString("errorCode"));
                    channelRetMsg.setChannelErrMsg(resObj.getString("errorMsg"));
                } else {
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    String channelOrderId = resObj.getString("order_id");
                    if (StringUtils.isNotBlank(channelOrderId)) {
                        channelRetMsg.setChannelOrderId(channelOrderId);
                    }
                    channelRetMsg.setChannelAttach(resStr);

                    payUrl = StringUtils.defaultIfBlank(resObj.getString("payUrl"), payUrl);
                    payUrl = StringUtils.defaultIfBlank(resObj.getString("pay_url"), payUrl);
                    payUrl = StringUtils.defaultIfBlank(resObj.getString("payment_url"), payUrl);
                    payUrl = StringUtils.defaultIfBlank(resObj.getString("url"), payUrl);
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
