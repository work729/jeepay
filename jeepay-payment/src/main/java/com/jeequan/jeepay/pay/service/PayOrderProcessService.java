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
package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/***
* 订单处理通用逻辑
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/22 16:50
*/
@Service
@Slf4j
public class PayOrderProcessService {


    @Autowired private PayOrderService payOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;
 

    /** 明确成功的处理逻辑（除更新订单其他业务） **/
    public void confirmSuccess(PayOrder payOrder){

        // 查询查询订单详情
        payOrder = payOrderService.getById(payOrder.getPayOrderId());

        //设置订单状态
        payOrder.setState(PayOrder.STATE_SUCCESS);

 

        //发送商户通知
        payMchNotifyService.payOrderNotify(payOrder);

    }



 


    /***
     *
     * 支付中 --》 支付成功或者失败
     * **/
    @Transactional
    public void updateIngAndSuccessOrFailByCreatebyOrder(PayOrder payOrder, ChannelRetMsg channelRetMsg){

        boolean isSuccess = payOrderService.updateInit2Ing(payOrder.getPayOrderId(), payOrder);
        if(!isSuccess){
            log.error("updateInit2Ing更新异常 payOrderId={}", payOrder.getPayOrderId());
            throw new BizException("更新订单异常!");
        }

        isSuccess = payOrderService.updateIng2SuccessOrFail(payOrder.getPayOrderId(), payOrder.getState(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
        if(!isSuccess){
            log.error("updateIng2SuccessOrFail更新异常 payOrderId={}", payOrder.getPayOrderId());
            throw new BizException("更新订单异常!");
        }
    }

}
