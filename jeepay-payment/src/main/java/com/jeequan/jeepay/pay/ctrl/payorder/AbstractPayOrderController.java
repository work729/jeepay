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
package com.jeequan.jeepay.pay.ctrl.payorder;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.PayOrderReissueMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.MchPayProduct;
import com.jeequan.jeepay.core.entity.PayChannel;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayProduct;
import com.jeequan.jeepay.core.entity.PayProductChannel;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JsonKit;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.pay.channel.IPaymentService;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import com.jeequan.jeepay.service.impl.MchPayProductService;
import com.jeequan.jeepay.service.impl.PayProductService;
import com.jeequan.jeepay.service.impl.PayChannelService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayProductChannelService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
* 创建支付订单抽象类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:26
*/
@Slf4j
public abstract class AbstractPayOrderController extends ApiController {

    @Autowired private MchPayProductService mchPayProductService;
    @Autowired private PayProductChannelService payProductChannelService;
    @Autowired private PayChannelService payChannelService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private PayProductService payProductService;
    @Autowired private IMQSender mqSender;


    /** 统一下单 (新建订单模式) **/
    protected ApiRes unifiedOrder(String wayCode, UnifiedOrderRQ bizRQ){
        return unifiedOrder(wayCode, bizRQ, null);
    }

    /** 统一下单 **/
    protected ApiRes unifiedOrder(String wayCode, UnifiedOrderRQ bizRQ, PayOrder payOrder){

        // 响应数据
        UnifiedOrderRS bizRS = null;

        //是否新订单模式 [  一般接口都为新订单模式，  由于QR_CASHIER支付方式，需要先 在DB插入一个新订单， 导致此处需要特殊判断下。 如果已存在则直接更新，否则为插入。  ]
        boolean isNewOrder = payOrder == null;

        try {

            if(payOrder != null){ //当订单存在时，封装公共参数。

                if(payOrder.getState() != PayOrder.STATE_INIT){
                    throw new BizException("订单状态异常");
                }

                payOrder.setChannelUser(bizRQ.getChannelUserId()); //更新渠道用户信息
                bizRQ.setMchId(payOrder.getMchNo());
                bizRQ.setAppId(payOrder.getAppId());
                bizRQ.setMchOrderNo(payOrder.getMchOrderNo());
                bizRQ.setAmount(payOrder.getAmount());
                bizRQ.setProductId(payOrder.getProductId());
                bizRQ.setClientIp(payOrder.getClientIp());
                bizRQ.setNotifyUrl(payOrder.getNotifyUrl());
                bizRQ.setReturnUrl(payOrder.getReturnUrl());
            }

            String mchNo = bizRQ.getMchId();
            String appId = bizRQ.getAppId();

            // 只有新订单模式，进行校验
            if(isNewOrder && payOrderService.count(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getMchOrderNo, bizRQ.getMchOrderNo())) > 0){
                throw new BizException("商户订单["+bizRQ.getMchOrderNo()+"]已存在");
            }

            if(StringUtils.isNotEmpty(bizRQ.getNotifyUrl()) && !StringKit.isAvailableUrl(bizRQ.getNotifyUrl())){
                throw new BizException("异步通知地址协议仅支持http:// 或 https:// !");
            }
            if(StringUtils.isNotEmpty(bizRQ.getReturnUrl()) && !StringKit.isAvailableUrl(bizRQ.getReturnUrl())){
                throw new BizException("同步通知地址协议仅支持http:// 或 https:// !");
            }

            MchAppConfigContext mchAppConfigContext = configContextQueryService.getMchInfoContext(mchNo);
            if(mchAppConfigContext == null){
                throw new BizException("获取商户信息失败");
            }

            MchInfo mchInfo = mchAppConfigContext.getMchInfo();
            MchApp mchApp = mchAppConfigContext.getMchApp();

            RouteConfig routeConfig = findRouteConfig(mchAppConfigContext, bizRQ);

            IPaymentService paymentService = checkMchWayCodeAndGetService(mchAppConfigContext, routeConfig.getIfCode(), wayCode);
            String ifCode = paymentService.getIfCode();

            Long routeProductId = routeConfig.getProductId();
            BigDecimal routeChannelRate = routeConfig.getChannelRate();
            BigDecimal routeMchRate = routeConfig.getMchRate();
            Long routeChannelId = routeConfig.getChannelId();

            if(isNewOrder){
                payOrder = genPayOrder(bizRQ, mchInfo, mchApp, ifCode, routeConfig.getMchRate());
                // 设置产品信息与通道费率
                payOrder.setProductId(routeProductId);
                payOrder.setChannelId(routeChannelId);
                payOrder.setChannelProviderId(routeConfig.getIfCode());
                payOrder.setChannelName(routeConfig.getChannelName());
                payOrder.setChannelFeeRate(routeChannelRate);
                payOrder.setChannelIfCode(routeConfig.getIfCode());
                payOrder.setChannelSign(routeConfig.getChannelSign());
            }else{
                payOrder.setIfCode(ifCode);
                payOrder.setMchFeeRate(routeConfig.getMchRate());
                payOrder.setMchFeeAmount(AmountUtil.calPercentageFee(payOrder.getAmount(), payOrder.getMchFeeRate()));
                // 更新产品信息与通道费率
                payOrder.setProductId(routeProductId);
                payOrder.setChannelId(routeChannelId);
                payOrder.setChannelProviderId(routeConfig.getIfCode());
                payOrder.setChannelName(routeConfig.getChannelName());
                payOrder.setChannelFeeRate(routeChannelRate);
                payOrder.setChannelIfCode(routeConfig.getIfCode());
                payOrder.setChannelSign(routeConfig.getChannelSign());
            }

            //预先校验
            String errMsg = paymentService.preCheck(bizRQ, payOrder);
            if(StringUtils.isNotEmpty(errMsg)){
                throw new BizException(errMsg);
            }

            String newPayOrderId = paymentService.customPayOrderId(bizRQ, payOrder, mchAppConfigContext);


            if(isNewOrder){
                if(StringUtils.isNotBlank(newPayOrderId)){ // 自定义订单号
                    payOrder.setPayOrderId(newPayOrderId);
                }
                //订单入库 订单状态： 生成状态  此时没有和任何上游渠道产生交互。
                payOrderService.save(payOrder);
            }

            //调起上游支付接口
            bizRS = (UnifiedOrderRS) paymentService.pay(bizRQ, payOrder, mchAppConfigContext);

            //处理上游返回数据
            this.processChannelMsg(bizRS.getChannelRetMsg(), payOrder);

            return packageApiResByPayOrder(bizRQ, bizRS, payOrder);

        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());

        } catch (ChannelException e) {

            //处理上游返回数据
            this.processChannelMsg(e.getChannelRetMsg(), payOrder);

            if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR ){
                return ApiRes.customFail(e.getMessage());
            }

            return this.packageApiResByPayOrder(bizRQ, bizRS, payOrder);


        } catch (Exception e) {
            log.error("系统异常：{}", e);
            String msg = e.getMessage();
            if(StringUtils.isBlank(msg)){
                msg = "系统异常";
            }
            return ApiRes.customFail(msg);
        }
    }

    private PayOrder genPayOrder(UnifiedOrderRQ rq, MchInfo mchInfo, MchApp mchApp, String ifCode, BigDecimal mchFeeRate){

        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(SeqKit.genPayOrderId()); //生成订单ID
        payOrder.setMchNo(mchInfo.getMchNo()); //商户号
        payOrder.setIsvNo(mchInfo.getIsvNo()); //服务商号
        payOrder.setAgentId(mchInfo.getAgentId()); //代理ID
        payOrder.setMchName(mchInfo.getMchShortName()); //商户名称（简称）
        payOrder.setMchType(mchInfo.getType()); //商户类型
        payOrder.setMchOrderNo(rq.getMchOrderNo()); //商户订单号
        payOrder.setAppId(mchApp == null ? null : mchApp.getAppId()); //商户应用appId
        payOrder.setIfCode(ifCode); //接口代码
        payOrder.setAmount(rq.getAmount()); //订单金额

        if(mchFeeRate != null){
            payOrder.setMchFeeRate(mchFeeRate); //商户手续费费率快照
        }else{
            payOrder.setMchFeeRate(BigDecimal.ZERO); //预下单模式， 按照0计算入库， 后续进行更新
        }

        payOrder.setMchFeeAmount(AmountUtil.calPercentageFee(payOrder.getAmount(), payOrder.getMchFeeRate())); //商户手续费,单位分

        payOrder.setState(PayOrder.STATE_INIT); //订单状态, 默认订单生成状态
        payOrder.setClientIp(StringUtils.defaultIfEmpty(rq.getClientIp(), getClientIp())); //客户端IP
//        payOrder.setChannelExtra(rq.getChannelExtra()); //特殊渠道发起的附件额外参数,  是否应该删除该字段了？？ 比如authCode不应该记录， 只是在传输阶段存在的吧？  之前的为了在payOrder对象需要传参。
        payOrder.setChannelUser(rq.getChannelUserId()); //渠道用户标志
        payOrder.setNotifyUrl(rq.getNotifyUrl()); //异步通知地址
        payOrder.setReturnUrl(rq.getReturnUrl()); //页面跳转地址

 

        Date nowDate = new Date();

        //订单过期时间 单位： 秒
        payOrder.setExpiredTime(DateUtil.offsetHour(nowDate, 2)); //订单过期时间 默认两个小时

        payOrder.setCreatedAt(nowDate); //订单创建时间
        return payOrder;
    }


    /**
     * 校验： 商户的支付方式是否可用
     * 返回： 支付接口
     * **/
    private IPaymentService checkMchWayCodeAndGetService(MchAppConfigContext mchAppConfigContext, String ifCode, String wayCode){

        IPaymentService paymentService = SpringBeansUtil.getBean(ifCode + "PaymentService", IPaymentService.class);
        if(paymentService == null){
            throw new BizException("无此支付通道接口");
        }

        if(!paymentService.isSupport(wayCode)){
            throw new BizException("接口不支持该支付方式");
        }

        return paymentService;

    }


    /** 处理返回的渠道信息，并更新订单状态
     *  payOrder将对部分信息进行 赋值操作。
     * **/
    private void processChannelMsg(ChannelRetMsg channelRetMsg, PayOrder payOrder){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }

        String payOrderId = payOrder.getPayOrderId();

        //明确成功
        if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(PayOrder.STATE_SUCCESS, payOrder, channelRetMsg);

            //订单支付成功，其他业务逻辑
            payOrderProcessService.confirmSuccess(payOrder);

        //明确失败
        }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(PayOrder.STATE_FAIL, payOrder, channelRetMsg);

        // 上游处理中 || 未知 || 上游接口返回异常  订单为支付中状态
        }else if( ChannelRetMsg.ChannelState.WAITING == channelRetMsg.getChannelState() ||
                  ChannelRetMsg.ChannelState.UNKNOWN == channelRetMsg.getChannelState() ||
                  ChannelRetMsg.ChannelState.API_RET_ERROR == channelRetMsg.getChannelState()

        ){
            this.updateInitOrderStateThrowException(PayOrder.STATE_ING, payOrder, channelRetMsg);

        // 系统异常：  订单不再处理。  为： 生成状态
        }else if( ChannelRetMsg.ChannelState.SYS_ERROR == channelRetMsg.getChannelState()){

        }else{

            throw new BizException("ChannelState 返回异常！");
        }

        //判断是否需要轮询查单
        if(channelRetMsg.isNeedQuery()){
            mqSender.send(PayOrderReissueMQ.build(payOrderId, 1), 5);
        }

    }


    /** 更新订单状态 --》 订单生成--》 其他状态  (向外抛出异常) **/
    private void updateInitOrderStateThrowException(byte orderState, PayOrder payOrder, ChannelRetMsg channelRetMsg){

        payOrder.setState(orderState);
        payOrder.setChannelOrderNo(channelRetMsg.getChannelOrderId());
        payOrder.setErrCode(channelRetMsg.getChannelErrCode());
        payOrder.setErrMsg(channelRetMsg.getChannelErrMsg());

        // 聚合码场景 订单对象存在会员信息， 不可全部以上游为准。
        if(StringUtils.isNotEmpty(channelRetMsg.getChannelUserId())){
            payOrder.setChannelUser(channelRetMsg.getChannelUserId());
        }

        payOrderProcessService.updateIngAndSuccessOrFailByCreatebyOrder(payOrder, channelRetMsg);

    }


    /** 统一封装订单数据  **/
    private ApiRes packageApiResByPayOrder(UnifiedOrderRQ bizRQ, UnifiedOrderRS bizRS, PayOrder payOrder){

        // 返回接口数据
        bizRS.setPayOrderId(payOrder.getPayOrderId());
        bizRS.setOrderState(payOrder.getState());
        bizRS.setMchOrderNo(payOrder.getMchOrderNo());

        if(payOrder.getState() == PayOrder.STATE_FAIL){
            bizRS.setErrCode(bizRS.getChannelRetMsg() != null ? bizRS.getChannelRetMsg().getChannelErrCode() : null);
            bizRS.setErrMsg(bizRS.getChannelRetMsg() != null ? bizRS.getChannelRetMsg().getChannelErrMsg() : null);
        }

        MchApp app = StringUtils.isBlank(bizRQ.getAppId()) ? null : configContextQueryService.queryMchApp(bizRQ.getAppId(), bizRQ.getAppId());
        if(app == null){
            return ApiRes.ok(bizRS);
        }
        String mchSecret = null;
        MchAppConfigContext ctx = configContextQueryService.queryMchInfoAndAppInfo(bizRQ.getAppId(), bizRQ.getAppId());
        if(ctx != null && ctx.getMchInfo() != null && StringUtils.isNotBlank(ctx.getMchInfo().getMchSecret())){
            mchSecret = ctx.getMchInfo().getMchSecret();
        }
        return ApiRes.okWithSign(bizRS, mchSecret);
    }


    private RouteConfig findRouteConfig(MchAppConfigContext mchAppConfigContext, UnifiedOrderRQ bizRQ){

        MchInfo mchInfo = mchAppConfigContext.getMchInfo();
        if(mchInfo == null || mchInfo.getId() == null){
            throw new BizException("商户信息不存在");
        }

        Long mchId = mchInfo.getId();
        Long reqProductId = bizRQ.getProductId();
        if(reqProductId == null){
            throw new BizException("产品ID不能为空");
        }

        List<MchPayProduct> mchPayProducts = mchPayProductService.list(
                MchPayProduct.gw()
                        .eq(MchPayProduct::getMchId, mchId)
                        .eq(MchPayProduct::getProductId, reqProductId)
                        .eq(MchPayProduct::getState, CS.YES)
        );
        if(mchPayProducts == null || mchPayProducts.isEmpty()){
            throw new BizException("商户未配置该支付产品");
        }

        List<Long> productIds = mchPayProducts.stream()
                .map(MchPayProduct::getProductId)
                .collect(Collectors.toList());

        List<PayProductChannel> productChannels = payProductChannelService.list(
                PayProductChannel.gw()
                        .in(PayProductChannel::getProductId, productIds)
        );
        if(productChannels == null || productChannels.isEmpty()){
            throw new BizException("商户支付产品未配置通道");
        }

        Map<Long, BigDecimal> productRateMap = new HashMap<>();
        for (MchPayProduct item : mchPayProducts) {
            productRateMap.put(item.getProductId(), item.getMchRate());
        }

        List<Long> channelIds = productChannels.stream()
                .map(PayProductChannel::getChannelId)
                .collect(Collectors.toList());

        List<PayChannel> channelList = payChannelService.list(
                PayChannel.gw()
                        .in(PayChannel::getId, channelIds)
                        .eq(PayChannel::getState, CS.PUB_USABLE)
        );
        if(channelList == null || channelList.isEmpty()){
            throw new BizException("商户支付产品通道不可用");
        }

        Map<Long, List<Long>> channelProductMap = new HashMap<>();
        for (PayProductChannel relation : productChannels) {
            List<Long> list = channelProductMap.get(relation.getChannelId());
            if(list == null){
                list = new ArrayList<>();
                channelProductMap.put(relation.getChannelId(), list);
            }
            list.add(relation.getProductId());
        }

        // 若指定了强制通道（按 channelSign 或 channelId），则仅在该通道上进行校验与返回
        String forceSign = FORCE_CHANNEL_SIGN.get();
        Long forceId = FORCE_CHANNEL_ID.get();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(forceSign) || forceId != null){
            PayChannel target = null;
            for (PayChannel c : channelList){
                if((forceId != null && forceId.equals(c.getId())) ||
                        (org.apache.commons.lang3.StringUtils.isNotBlank(forceSign) && forceSign.equals(c.getChannelSign()))){
                    target = c; break;
                }
            }
            if(target == null){
                throw new BizException("指定通道不可用");
            }
            List<Long> bindProductIds0 = channelProductMap.get(target.getId());
            if(bindProductIds0 == null || !bindProductIds0.contains(reqProductId)){
                throw new BizException("指定通道未绑定该产品");
            }
            BigDecimal rate0 = productRateMap.get(reqProductId);
            if(rate0 == null){ rate0 = BigDecimal.ZERO; }
            // 校验该通道是否有实现的 PaymentService
            try{
                IPaymentService ps = SpringBeansUtil.getBean(target.getChannelSign() + "PaymentService", IPaymentService.class);
                if(ps == null){
                    throw new BizException("指定通道未实现支付能力");
                }
            }catch (Exception ignore){
                throw new BizException("指定通道未实现支付能力");
            }
            return new RouteConfig(target.getChannelSign(), rate0, reqProductId, target.getChannelRate(), target.getChannelName(), target.getChannelSign(), target.getId());
        }

        // 风控过滤：剔除命中风控条件的通道
        List<PayChannel> filtered = new ArrayList<>();
        for (PayChannel channel : channelList) {
            if (passRisk(bizRQ, channel)) {
                filtered.add(channel);
            }
        }
        if(filtered.isEmpty()){
            throw new BizException("商户支付产品通道不可用");
        }
        for (PayChannel channel : filtered) {
            String channelSign = channel.getChannelSign();
            IPaymentService paymentService;
            try {
                paymentService = SpringBeansUtil.getBean(channelSign + "PaymentService", IPaymentService.class);
            } catch (Exception e) {
                continue;
            }
            if(paymentService == null){
                continue;
            }

            List<Long> bindProductIds = channelProductMap.get(channel.getId());
            if(bindProductIds == null || bindProductIds.isEmpty()){
                continue;
            }
            if(bindProductIds.contains(reqProductId)){
                BigDecimal rate = productRateMap.get(reqProductId);
                if(rate == null){
                    rate = BigDecimal.ZERO;
                }
                return new RouteConfig(channelSign, rate, reqProductId, channel.getChannelRate(), channel.getChannelName(), channel.getChannelSign(), channel.getId());
            }
        }

        throw new BizException("商户不支持该支付方式");
    }


    /** 强制通道 ThreadLocal（由上层控制器在收到请求时注入，仅当前请求生效） **/
    private static final ThreadLocal<String> FORCE_CHANNEL_SIGN = new ThreadLocal<>();
    private static final ThreadLocal<Long> FORCE_CHANNEL_ID = new ThreadLocal<>();

    public static void setForceChannelSign(String channelSign){
        FORCE_CHANNEL_SIGN.set(channelSign);
    }
    public static void setForceChannelId(Long channelId){
        FORCE_CHANNEL_ID.set(channelId);
    }
    public static void clearForceChannel(){
        FORCE_CHANNEL_SIGN.remove();
        FORCE_CHANNEL_ID.remove();
    }

    /** 判断是否通过风控（true=可用；false=命中风控需剔除） */
    private boolean passRisk(UnifiedOrderRQ bizRQ, PayChannel channel){
        try{
            String cfgStr = channel.getChannelSignConfig();
            if(org.apache.commons.lang3.StringUtils.isBlank(cfgStr)){
                return true;
            }
            JSONObject cfg = JSONObject.parseObject(cfgStr);
            if(cfg == null){
                return true;
            }
            JSONObject risk = cfg.getJSONObject("risk");
            if(risk == null){
                return true;
            }
            int enable = risk.getIntValue("enable");
            if(enable != 1){
                return true;
            }
            long amountFen = bizRQ.getAmount() == null ? 0L : bizRQ.getAmount();
            // 排除金额（无条件生效）
            String exclude = risk.getString("excludeAmountsYuan");
            if(org.apache.commons.lang3.StringUtils.isNotBlank(exclude)){
                String[] arr = exclude.split(",");
                for(String s : arr){
                    try{
                        BigDecimal v = new BigDecimal(s.trim());
                        long fen = v.multiply(new BigDecimal(100)).longValue();
                        if(fen == amountFen){ return false; }
                    }catch (Exception ignore){}
                }
            }
            // 时间窗（仅在时间窗内才应用其它风控；时间之外不风控）
            String start = risk.getString("startTime");
            String end = risk.getString("endTime");
            if(org.apache.commons.lang3.StringUtils.isNotBlank(start) && org.apache.commons.lang3.StringUtils.isNotBlank(end)){
                int nowSec = timeToSec(DateUtil.format(new Date(), "HH:mm:ss"));
                int sSec = timeToSec(start);
                int eSec = timeToSec(end);
                boolean inRange;
                if(sSec <= eSec){
                    inRange = nowSec >= sSec && nowSec <= eSec;
                }else{
                    inRange = nowSec >= sSec || nowSec <= eSec;
                }
                if(!inRange){
                    return true;
                }
            }
            // 金额范围（仅在范围内才应用其它风控；范围之外不风控）
            Long singleMinFen = yuanToFen(risk.getBigDecimal("singleMinYuan"));
            Long singleMaxFen = yuanToFen(risk.getBigDecimal("singleMaxYuan"));
            boolean inAmountRange = true;
            if(singleMinFen != null && amountFen < singleMinFen){ inAmountRange = false; }
            if(singleMaxFen != null && amountFen > singleMaxFen){ inAmountRange = false; }
            if(!inAmountRange){
                return true;
            }
            // 金额类型
            String amountType = risk.getString("amountType");
            if(org.apache.commons.lang3.StringUtils.isNotBlank(amountType)){
                long amountYuan = amountFen / 100;
                if("NOT_MULTIPLE_OF_10".equals(amountType)){
                    if(amountYuan % 10 != 0){ return false; }
                }else if("MUL_5".equals(amountType)){
                    if(amountYuan % 5 == 0){ return false; }
                }else if("MUL_10".equals(amountType)){
                    if(amountYuan % 10 == 0){ return false; }
                }else if("MUL_50".equals(amountType)){
                    if(amountYuan % 50 == 0){ return false; }
                }else if("MUL_100".equals(amountType)){
                    if(amountYuan % 100 == 0){ return false; }
                }else if("FIXED_MULTIPLE".equals(amountType)){
                    BigDecimal baseMul = risk.getBigDecimal("baseMultipleYuan");
                    if(baseMul != null){
                        long base = baseMul.multiply(new BigDecimal(1)).longValue();
                        if(base > 0 && amountYuan % base == 0){ return false; }
                    }
                }else if("FIXED_AMOUNTS".equals(amountType)){
                    BigDecimal fixed = risk.getBigDecimal("fixedAmountsYuan");
                    if(fixed != null){
                        long fen = fixed.multiply(new BigDecimal(100)).longValue();
                        if(fen == amountFen){ return false; }
                    }
                }
            }
            // 最短进单间隔
            Long minInterval = risk.getLong("minIntervalMs");
            if(minInterval != null && minInterval > 0){
                PayOrder last = payOrderService.getOne(PayOrder.gw()
                        .eq(PayOrder::getChannelId, channel.getId())
                        .orderByDesc(PayOrder::getCreatedAt)
                        .last("limit 1"));
                if(last != null && last.getCreatedAt() != null){
                    long diff = new Date().getTime() - last.getCreatedAt().getTime();
                    if(diff < minInterval){
                        return false;
                    }
                }
            }
            // 当天交易总金额上限（按支付成功累计）
            Long dailyMaxFen = yuanToFen(risk.getBigDecimal("dailyTotalYuan"));
            if(dailyMaxFen != null && dailyMaxFen > 0){
                Date startOfDay = DateUtil.beginOfDay(new Date());
                Date endOfDay = DateUtil.endOfDay(new Date());
                List<PayOrder> orders = payOrderService.list(PayOrder.gw()
                        .eq(PayOrder::getChannelId, channel.getId())
                        .eq(PayOrder::getState, PayOrder.STATE_SUCCESS)
                        .between(PayOrder::getCreatedAt, startOfDay, endOfDay));
                long sum = 0L;
                if(orders != null){
                    for(PayOrder o : orders){
                        if(o.getAmount() != null){ sum += o.getAmount(); }
                    }
                }
                if(sum + amountFen > dailyMaxFen){
                    return false;
                }
            }
            return true;
        }catch (Exception e){
            return true;
        }
    }

    private Long yuanToFen(BigDecimal yuan){
        if(yuan == null){ return null; }
        return yuan.multiply(new BigDecimal(100)).longValue();
    }

    private int timeToSec(String hhmmss){
        if(org.apache.commons.lang3.StringUtils.isBlank(hhmmss)){ return 0; }
        String[] arr = hhmmss.split(":");
        int h = arr.length > 0 ? Integer.parseInt(arr[0]) : 0;
        int m = arr.length > 1 ? Integer.parseInt(arr[1]) : 0;
        int s = arr.length > 2 ? Integer.parseInt(arr[2]) : 0;
        return h * 3600 + m * 60 + s;
    }

    private static class RouteConfig {

        private final String ifCode;
        private final BigDecimal mchRate;
        private final Long productId;
        private final BigDecimal channelRate;
        private final String channelName;
        private final String channelSign;
        private final Long channelId;

        RouteConfig(String ifCode, BigDecimal mchRate, Long productId, BigDecimal channelRate, String channelName, String channelSign, Long channelId) {
            this.ifCode = ifCode;
            this.mchRate = mchRate;
            this.productId = productId;
            this.channelRate = channelRate;
            this.channelName = channelName;
            this.channelSign = channelSign;
            this.channelId = channelId;
        }

        public String getIfCode() {
            return ifCode;
        }

        public BigDecimal getMchRate() {
            return mchRate;
        }

        public Long getProductId() {
            return productId;
        }

        public BigDecimal getChannelRate() {
            return channelRate;
        }
        
        public String getChannelName() {
            return channelName;
        }
        
        public String getChannelSign() {
            return channelSign;
        }
        
        public Long getChannelId() {
            return channelId;
        }
    }

}
