package com.jeequan.jeepay.core.model.params.mayifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class MayifuNormalMchParams extends NormalMchParams {

    private String merchantNo;

    private String key;

    private String payUrl;

    private String paymentMethodFlow;

    @Override
    public String deSenData() {
        MayifuNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.key)) {
            mchParams.setKey(StringKit.str2Star(this.key, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }
}
