package com.jeequan.jeepay.pay.ctrl.payorder;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.service.impl.MchInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalUnifiedOrderController extends AbstractPayOrderController {

    @Autowired
    private MchInfoService mchInfoService;

    @PostMapping("/api/internal/pay/unifiedOrder")
    public ApiRes unifiedOrderForce(){

        JSONObject params = getReqParamJSON();
        if(params.containsKey("mchId") && !params.containsKey("mchNo")){
            Object mchIdVal = params.get("mchId");
            if(mchIdVal != null){
                try{
                    Long mchIdL = (mchIdVal instanceof Number) ? ((Number)mchIdVal).longValue() : Long.parseLong(String.valueOf(mchIdVal));
                    MchInfo info = mchInfoService.getOne(MchInfo.gw().eq(MchInfo::getId, mchIdL));
                    if(info != null && info.getMchNo() != null){
                        params.put("mchNo", info.getMchNo());
                    }
                }catch (Exception ignore){
                }
            }
        }

        String forceChannelSign = params.getString("channelSign");
        Long forceChannelId = null;
        try{
            Object v = params.get("channelId");
            if(v != null){
                forceChannelId = (v instanceof Number) ? ((Number)v).longValue() : Long.parseLong(String.valueOf(v));
            }
        }catch (Exception ignore){}
        if(org.apache.commons.lang3.StringUtils.isNotBlank(forceChannelSign)){
            AbstractPayOrderController.setForceChannelSign(forceChannelSign);
        }
        if(forceChannelId != null){
            AbstractPayOrderController.setForceChannelId(forceChannelId);
        }

        UnifiedOrderRQ rq = getRQByWithMchSign(UnifiedOrderRQ.class);
        UnifiedOrderRQ bizRQ = rq.buildBizRQ();
        try{
            return unifiedOrder("", bizRQ);
        } finally {
            AbstractPayOrderController.clearForceChannel();
        }
    }
}
