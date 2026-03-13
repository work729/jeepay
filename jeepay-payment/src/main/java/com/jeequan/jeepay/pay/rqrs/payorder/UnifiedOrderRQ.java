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
package com.jeequan.jeepay.pay.rqrs.payorder;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.AbstractMchAppRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AutoBarOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.QrCashierOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.UpAppOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.UpB2bOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.UpBarOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.UpJsapiOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.UpPcOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.UpQrOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.UpWapOrderRQ;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
* 创建订单请求参数对象
* 聚合支付接口（统一下单）
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:33
*/
@Data
public class UnifiedOrderRQ extends AbstractMchAppRQ {

    /** 产品 ID **/
    @NotNull(message="产品ID不能为空")
    @Min(value = 1, message = "产品ID必须大于0")
    private Long productId;

    /** 商户订单号 **/
    @NotBlank(message="商户订单号不能为空")
    private String mchOrderNo;

    /** 支付金额， 单位：分 **/
    @NotNull(message="支付金额不能为空")
    @Min(value = 1, message = "支付金额不能为空")
    private Long amount;

    /** 客户端IP地址 **/
    private String clientIp;

    /** 异步通知地址 **/
    private String notifyUrl;

    /** 跳转通知地址 **/
    private String returnUrl;

    /** 返回真实的bizRQ **/
    public UnifiedOrderRQ buildBizRQ(){
        return this;
    }

    /** 获取渠道用户ID **/
    @JSONField(serialize = false)
    public String getChannelUserId(){
        return null;
    }

}
