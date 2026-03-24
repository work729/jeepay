package com.jeequan.jeepay.pay.channel.mayifu;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.channel.IChannelNoticeService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.service.impl.PayProductService;
import com.jeequan.jeepay.core.entity.PayProduct;

@Slf4j
@Service("mayifuChannelNoticeService")
public class MayifuChannelNoticeService extends AbstractChannelNoticeService implements IChannelNoticeService {

    @Autowired
    private PayProductService payProductService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.MAYIFU;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {
        JSONObject params = getReqParamJSON();
        if (params == null) {
            params = new JSONObject();
        }
        String payOrderId = StringUtils.defaultIfBlank(urlOrderId, params.getString("payOrderId"));
        return MutablePair.of(payOrderId, params);
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, NoticeTypeEnum noticeTypeEnum) {
        JSONObject paramObj = (params instanceof JSONObject) ? (JSONObject) params : new JSONObject();

        String sign = paramObj.getString("sign");
        ChannelRetMsg retMsg = new ChannelRetMsg();

        // 使用 t_pay_product 校验产品
        long productId = paramObj.getLongValue("productId");
        if (productId > 0) {
            PayProduct product = payProductService.getById(productId);
            if (product == null || product.getState() == null || product.getState() != 1) {
                retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                retMsg.setResponseEntity(textResp("product invalid"));
                return retMsg;
            }
        }

        // 获取商户通道密钥
        String key = null;
        Object np = configContextQueryService.queryNormalMchParams(payOrder.getMchNo(), payOrder.getAppId(), getIfCode());
        if (np != null) {
            JSONObject j = JSONObject.parseObject(JSONObject.toJSONString(np));
            key = StringUtils.defaultIfBlank(j.getString("key"),
                    StringUtils.defaultIfBlank(j.getString("APP_KEY"), j.getString("Key")));
        }

        if (StringUtils.isBlank(key)) {
            retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            retMsg.setResponseEntity(textResp("key not configured"));
            return retMsg;
        }

        // 构建参与签名的参数
        Map<String, Object> signMap = new HashMap<>();
        for (Iterator<String> it = paramObj.keySet().iterator(); it.hasNext(); ) {
            String k = it.next();
            if (k == null || "sign".equals(k)) continue;
            Object v = paramObj.get(k);
            if (v == null) continue;
            String vs = String.valueOf(v);
            if (StringUtils.isBlank(vs)) continue;
            signMap.put(k, vs);
        }

        String calcSign = MayifupayKit.getSign(signMap, key);
        if (!StringUtils.equalsIgnoreCase(calcSign, sign)) {
            retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            retMsg.setResponseEntity(textResp("sign fail"));
            return retMsg;
        }

        int status = paramObj.getIntValue("status");
        if (status == 2 || status == 3) {
            retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        } else if (status == 0 || status == 1) {
            retMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        } else {
            retMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        }

        retMsg.setChannelOrderId(paramObj.getString("payOrderId"));
        retMsg.setResponseEntity(textResp("success"));
        return retMsg;
    }
}
