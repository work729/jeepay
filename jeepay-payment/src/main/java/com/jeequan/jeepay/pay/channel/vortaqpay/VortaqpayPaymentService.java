package com.jeequan.jeepay.pay.channel.vortaqpay;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.vortaqpay.VortaqpayNormalMchParams;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VortaqpayPaymentService extends AbstractPaymentService {

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
        VortaqpayNormalMchParams params = (VortaqpayNormalMchParams) configContextQueryService
                .queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

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
        body.put("merchant_no", params.getMerchantNo());
        body.put("data", data);

        String url = VortaqpayKit.getPaymentUrl(params.getPayUrl());
        String resStr = "";
        try {
            log.info("vortaqpay request url:{}, body:{}", url, body.toJSONString());
            resStr = HttpUtil.createPost(url)
                    .timeout(60 * 1000)
                    .header("Content-Type", "application/json")
                    .body(body.toJSONString())
                    .execute()
                    .body();
            log.info("vortaqpay response:{}", resStr);
        } catch (Exception e) {
            log.error("vortaqpay http error", e);
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        if (StringUtils.isEmpty(resStr)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode("");
            channelRetMsg.setChannelErrMsg("请求vortaqpay接口异常");
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
            }
        }

        UnifiedOrderRS res = new UnifiedOrderRS();
        res.setPayOrderId(payOrder.getPayOrderId());
        res.setMchOrderNo(payOrder.getMchOrderNo());
        res.setChannelRetMsg(channelRetMsg);
        return res;
    }
}
