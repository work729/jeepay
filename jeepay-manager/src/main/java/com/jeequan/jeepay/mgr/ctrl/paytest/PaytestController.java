package com.jeequan.jeepay.mgr.ctrl.paytest;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/paytest")
public class PaytestController extends CommonCtrl {

    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private MchInfoService mchInfoService;

    @PostMapping("/payOrders")
    public ApiRes doPay() {
        DBApplicationConfig cfg = sysConfigService.getDBApplicationConfig();
        String testMchNo = cfg.getTestMchNo();
        String testProductIdStr = cfg.getTestProductId();
        if (StringUtils.isBlank(testMchNo) || StringUtils.isBlank(testProductIdStr)) {
            throw new BizException("请先在系统设置配置测试商户号与测试产品ID");
        }
        Long testProductId;
        try {
            testProductId = Long.parseLong(testProductIdStr);
        } catch (Exception e) {
            throw new BizException("测试产品ID格式不正确");
        }
        MchInfo mchInfo = mchInfoService.getById(testMchNo);
        if (mchInfo == null || mchInfo.getId() == null || StringUtils.isBlank(mchInfo.getMchSecret())) {
            throw new BizException("测试商户不存在或密钥未配置");
        }

        JSONObject body = getReqParamJSON();
        BigDecimal amountYuan = body.getBigDecimal("amount");
        if (amountYuan == null || amountYuan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("请输入正确的金额");
        }
        long amountCent = amountYuan.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

        Map<String, Object> req = new HashMap<>();
        req.put("mchId", mchInfo.getMchNo());
        req.put("mchNo", mchInfo.getMchNo());
        req.put("productId", testProductId);
        req.put("mchOrderNo", "TEST" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6));
        req.put("amount", amountCent);
        req.put("channelSign", body.getString("channelSign"));
        req.put("channelId", body.getLong("channelId"));

        String sign = JeepayKit.getSign(req, mchInfo.getMchSecret());
        req.put("sign", sign);

        String payApi = cfg.getPaySiteUrl() + "/api/internal/pay/unifiedOrder";
        org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
        try {
            String respStr = rt.postForObject(payApi, new JSONObject(req), String.class);
            JSONObject resp = JSONObject.parseObject(respStr);
            Integer code = resp.getInteger("code");
            String msg = resp.getString("msg");
            if (code == null || code != 0) {
                throw new BizException(StringUtils.defaultIfBlank(msg, "请求失败"));
            }
            JSONObject data = resp.getJSONObject("data");
            if (data != null) {
                String payDataType = data.getString("payDataType");
                String payData = data.getString("payData");
                if ("payurl".equalsIgnoreCase(payDataType) && StringUtils.isNotBlank(payData)) {
                    data.put("payUrl", payData);
                }
            }
            return ApiRes.ok(data);
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }
}
