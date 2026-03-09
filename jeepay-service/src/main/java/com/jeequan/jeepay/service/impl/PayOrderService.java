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
package com.jeequan.jeepay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.IsvInfo;
import com.jeequan.jeepay.core.entity.MchFundFlow;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.service.mapper.*;
import com.jeequan.jeepay.service.impl.MchFundFlowService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.*;

/**
 * <p>
 * 支付订单表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Service
public class PayOrderService extends ServiceImpl<PayOrderMapper, PayOrder> {

    @Autowired private PayOrderMapper payOrderMapper;
    @Autowired private MchInfoMapper mchInfoMapper;
    @Autowired private IsvInfoMapper isvInfoMapper;
    @Autowired private PayWayMapper payWayMapper;
    @Autowired private MchFundFlowService mchFundFlowService;

    /** 更新订单状态  【订单生成】 --》 【支付中】 **/
    public boolean updateInit2Ing(String payOrderId, PayOrder payOrder){

        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_ING);

        //同时更新， 未确定 --》 已确定的其他信息。  如支付接口的确认、 费率的计算。
        updateRecord.setIfCode(payOrder.getIfCode());
        updateRecord.setWayCode(payOrder.getWayCode());
        updateRecord.setMchFeeRate(payOrder.getMchFeeRate());
        updateRecord.setMchFeeAmount(payOrder.getMchFeeAmount());
        updateRecord.setChannelUser(payOrder.getChannelUser());
        updateRecord.setChannelOrderNo(payOrder.getChannelOrderNo());

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_INIT));
    }

    /** 更新订单状态  【支付中】 --》 【支付成功】 **/
    public boolean updateIng2Success(String payOrderId, String channelOrderNo, String channelUserId){

        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_SUCCESS);
        updateRecord.setChannelOrderNo(channelOrderNo);
        updateRecord.setChannelUser(channelUserId);
        updateRecord.setSuccessTime(new Date());

        boolean ok = update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_ING));
        if(!ok){
            return false;
        }
        PayOrder db = getById(payOrderId);
        long changeAmount = db.getAmount() - (db.getMchFeeAmount() == null ? 0L : db.getMchFeeAmount());
        MchInfo mchInfo = mchInfoMapper.selectById(db.getMchNo());
        long before = mchInfo == null || mchInfo.getAccountBalance() == null ? 0L : mchInfo.getAccountBalance();
        long after = before + changeAmount;
        MchFundFlow flow = new MchFundFlow()
                .setMchNo(db.getMchNo())
                .setBeforeAmount(before)
                .setChangeAmount(changeAmount)
                .setAfterAmount(after)
                .setBizType((byte)1)
                .setBizOrderId(db.getPayOrderId())
                .setBizOrderAmount(db.getAmount())
                .setRemark("支付入账");
        mchFundFlowService.save(flow);
        return true;
    }

    /** 更新订单状态  【支付中】 --》 【订单关闭】 **/
    public boolean updateIng2Close(String payOrderId){

        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_CLOSED);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_ING));
    }

    /** 更新订单状态  【订单生成】 --》 【订单关闭】 **/
    public boolean updateInit2Close(String payOrderId){

        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_CLOSED);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_INIT));
    }


    /** 更新订单状态  【支付中】 --》 【支付失败】 **/
    public boolean updateIng2Fail(String payOrderId, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg){

        PayOrder updateRecord = new PayOrder();
        updateRecord.setState(PayOrder.STATE_FAIL);
        updateRecord.setErrCode(channelErrCode);
        updateRecord.setErrMsg(channelErrMsg);
        updateRecord.setChannelOrderNo(channelOrderNo);
        updateRecord.setChannelUser(channelUserId);

        return update(updateRecord, new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getPayOrderId, payOrderId).eq(PayOrder::getState, PayOrder.STATE_ING));
    }


    /** 更新订单状态  【支付中】 --》 【支付成功/支付失败】 **/
    public boolean updateIng2SuccessOrFail(String payOrderId, Byte updateState, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg){

        if(updateState == PayOrder.STATE_ING){
            return true;
        }else if(updateState == PayOrder.STATE_SUCCESS){
            return updateIng2Success(payOrderId, channelOrderNo, channelUserId);
        }else if(updateState == PayOrder.STATE_FAIL){
            return updateIng2Fail(payOrderId, channelOrderNo, channelUserId, channelErrCode, channelErrMsg);
        }
        return false;
    }

    /** 查询商户订单 **/
    public PayOrder queryMchOrder(String mchNo, String payOrderId, String mchOrderNo){

        if(StringUtils.isNotEmpty(payOrderId)){
            return getOne(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getPayOrderId, payOrderId));
        }else if(StringUtils.isNotEmpty(mchOrderNo)){
            return getOne(PayOrder.gw().eq(PayOrder::getMchNo, mchNo).eq(PayOrder::getMchOrderNo, mchOrderNo));
        }else{
            return null;
        }
    }


    public Map payCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd) {
        Map param = new HashMap<>();
        if (state != null) {
            param.put("state", state);
        }
        if (refundState != null) {
            param.put("refundState", refundState);
        }
        if (StrUtil.isNotBlank(mchNo)) {
            param.put("mchNo", mchNo);
        }
        if (StrUtil.isNotBlank(dayStart)) {
            param.put("createTimeStart", dayStart);
        }
        if (StrUtil.isNotBlank(dayEnd)) {
            param.put("createTimeEnd", dayEnd);
        }
        return payOrderMapper.payCount(param);
    }

    public List<Map> payTypeCount(String mchNo, Byte state, Byte refundState, String dayStart, String dayEnd) {
        Map param = new HashMap<>();
        if (state != null) {
            param.put("state", state);
        }
        if (refundState != null) {
            param.put("refundState", refundState);
        }
        if (StrUtil.isNotBlank(mchNo)) {
            param.put("mchNo", mchNo);
        }
        if (StrUtil.isNotBlank(dayStart)) {
            param.put("createTimeStart", dayStart);
        }
        if (StrUtil.isNotBlank(dayEnd)) {
            param.put("createTimeEnd", dayEnd);
        }
        return payOrderMapper.payTypeCount(param);
    }

    /** 更新订单为 超时状态 **/
    public Integer updateOrderExpired(){

        PayOrder payOrder = new PayOrder();
        payOrder.setState(PayOrder.STATE_CLOSED);

        return baseMapper.update(payOrder,
                PayOrder.gw()
                        .in(PayOrder::getState, Arrays.asList(PayOrder.STATE_INIT, PayOrder.STATE_ING))
                        .le(PayOrder::getExpiredTime, new Date())
        );
    }

    /** 更新订单 通知状态 --> 已发送 **/
    public int updateNotifySent(String payOrderId){
        PayOrder payOrder = new PayOrder();
        payOrder.setNotifyState(CS.YES);
        payOrder.setPayOrderId(payOrderId);
        return baseMapper.updateById(payOrder);
    }

    /** 更新订单回调成功时间 **/
    public int updateNotifySuccessTime(String payOrderId){
        PayOrder payOrder = new PayOrder();
        payOrder.setNotifySuccessTime(new Date());
        payOrder.setPayOrderId(payOrderId);
        return baseMapper.updateById(payOrder);
    }

    /** 首页支付周统计 **/
    public JSONObject mainPageWeekCount(String mchNo) {
        JSONObject json = new JSONObject();
        Map dayAmount = new LinkedHashMap();
        ArrayList array = new ArrayList<>();
        BigDecimal payAmount = new BigDecimal(0);    // 当日金额
        BigDecimal payWeek  = payAmount;   // 周总收益
        String todayAmount = "0.00";    // 今日金额
        String todayPayCount = "0";    // 今日交易笔数
        String yesterdayAmount = "0.00";    // 昨日金额
        Date today = new Date();
        for(int i = 0 ; i < 7 ; i++){
            Date date = DateUtil.offsetDay(today, -i).toJdkDate();
            String dayStart = DateUtil.beginOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
            String dayEnd = DateUtil.endOfDay(date).toString(DatePattern.NORM_DATETIME_MINUTE_PATTERN);
            // 每日交易金额查询
            dayAmount = payCount(mchNo, PayOrder.STATE_SUCCESS, null, dayStart, dayEnd);
            if (dayAmount != null) {
                payAmount = new BigDecimal(dayAmount.get("payAmount").toString());
            }
            if (i == 0) {
                todayAmount = dayAmount.get("payAmount").toString();
                todayPayCount = dayAmount.get("payCount").toString();
            }
            if (i == 1) {
                yesterdayAmount = dayAmount.get("payAmount").toString();
            }
            payWeek = payWeek.add(payAmount);
            array.add(payAmount);
        }

        // 倒序排列
        Collections.reverse(array);
        json.put("dataArray", array);
        json.put("todayAmount", todayAmount);
        json.put("todayPayCount", todayPayCount);
        json.put("payWeek", payWeek);
        json.put("yesterdayAmount", yesterdayAmount);
        return json;
    }

    /** 首页统计总数量 **/
    public JSONObject mainPageNumCount(String mchNo) {
        JSONObject json = new JSONObject();
        // 商户总数
        long mchCount = mchInfoMapper.selectCount(MchInfo.gw());
        // 服务商总数
        long isvCount = isvInfoMapper.selectCount(IsvInfo.gw());
        // 总交易金额
        Map<String, String> payCountMap = payCount(mchNo, PayOrder.STATE_SUCCESS, null, null, null);
        json.put("totalMch", mchCount);
        json.put("totalIsv", isvCount);
        json.put("totalAmount", payCountMap.get("payAmount"));
        json.put("totalCount", payCountMap.get("payCount"));
        return json;
    }

    /** 首页支付统计 **/
    public List<Map> mainPagePayCount(String mchNo, String createdStart, String createdEnd) {
        Map param = new HashMap<>(); // 条件参数
        int daySpace = 6; // 默认最近七天（含当天）
        if (StringUtils.isNotEmpty(createdStart) && StringUtils.isNotEmpty(createdEnd)) {
            createdStart = createdStart + " 00:00:00";
            createdEnd = createdEnd + " 23:59:59";
            // 计算两时间间隔天数
            daySpace = Math.toIntExact(DateUtil.betweenDay(DateUtil.parseDate(createdStart), DateUtil.parseDate(createdEnd), true));
        } else {
            Date today = new Date();
            createdStart = DateUtil.formatDate(DateUtil.offsetDay(today, -daySpace)) + " 00:00:00";
            createdEnd = DateUtil.formatDate(today) + " 23:59:59";
        }

        if (StrUtil.isNotBlank(mchNo)) {
            param.put("mchNo", mchNo);
        }
        param.put("createTimeStart", createdStart);
        param.put("createTimeEnd", createdEnd);
        // 查询收款的记录
        List<Map> payAndRefundOrderList = payOrderMapper.selectOrderCount(param);
        // 生成前端返回参数类型
        List<Map> returnList = getReturnList(daySpace, createdEnd, payAndRefundOrderList);
        return returnList;
    }

    /** 首页支付类型统计 **/
    public ArrayList mainPagePayTypeCount(String mchNo, String createdStart, String createdEnd) {
        // 返回数据列
        ArrayList array = new ArrayList<>();
        if (StringUtils.isNotEmpty(createdStart) && StringUtils.isNotEmpty(createdEnd)) {
            createdStart = createdStart + " 00:00:00";
            createdEnd = createdEnd + " 23:59:59";
        }else {
            Date endDay = new Date();    // 当前日期
            Date startDay = DateUtil.lastWeek().toJdkDate(); // 一周前日期
            String end = DateUtil.formatDate(endDay);
            String start = DateUtil.formatDate(startDay);
            createdStart = start + " 00:00:00";
            createdEnd = end + " 23:59:59";
        }
        // 统计列表
        List<Map> payCountMap = payTypeCount(mchNo, PayOrder.STATE_SUCCESS, null, createdStart, createdEnd);

        // 得到所有支付方式
        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayMapper.selectList(PayWay.gw());
        for (PayWay payWay:payWayList) {
            payWayNameMap.put(payWay.getWayCode(), payWay.getWayName());
        }
        // 支付方式名称标注
        for (Map payCount:payCountMap) {
            if (StringUtils.isNotEmpty(payWayNameMap.get(payCount.get("wayCode")))) {
                payCount.put("typeName", payWayNameMap.get(payCount.get("wayCode")));
            }else {
                payCount.put("typeName", payCount.get("wayCode"));
            }
        }
        array.add(payCountMap);
        return array;
    }

    /** 商户维度汇总分页 **/
    public com.baomidou.mybatisplus.core.metadata.IPage<Map> mchStatsPage(com.baomidou.mybatisplus.core.metadata.IPage<?> page, String mchNo, String mchName, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(mchNo)) { param.put("mchNo", mchNo); }
        if (StrUtil.isNotBlank(mchName)) { param.put("mchName", mchName); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectMchStatsPage(page, param);
    }

    /** 商户通道成功率统计 **/
    public List<Map> mchChannelSuccessStats(String mchNo, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(mchNo)) { param.put("mchNo", mchNo); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectChannelSuccessStats(param);
    }

    /** 产品维度汇总分页 **/
    public com.baomidou.mybatisplus.core.metadata.IPage<Map> productStatsPage(com.baomidou.mybatisplus.core.metadata.IPage<?> page, String productId, String productName, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(productId)) { param.put("productId", productId); }
        if (StrUtil.isNotBlank(productName)) { param.put("productName", productName); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectProductStatsPage(page, param);
    }

    /** 产品下商户统计 **/
    public List<Map> productMchStats(String productId, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(productId)) { param.put("productId", productId); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectProductMchStats(param);
    }

    /** 渠道类型汇总分页 **/
    public com.baomidou.mybatisplus.core.metadata.IPage<Map> channelStatsPage(com.baomidou.mybatisplus.core.metadata.IPage<?> page, String ifCode, String ifName, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(ifCode)) { param.put("ifCode", ifCode); }
        if (StrUtil.isNotBlank(ifName)) { param.put("ifName", ifName); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectChannelStatsPage(page, param);
    }

    /** 渠道详情（按 ifCode 下的 wayCode） **/
    public List<Map> channelDetailsByIfCode(String ifCode, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(ifCode)) { param.put("ifCode", ifCode); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectChannelDetailsByIfCode(param);
    }

    /** 渠道对账详情（按 ifCode 下的 channel_id，含跑量、费率、入账） **/
    public List<Map> channelReconcileDetailsByIfCode(String ifCode, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(ifCode)) { param.put("ifCode", ifCode); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectChannelReconcileDetailsByIfCode(param);
    }

    /** 商户当日产品维度统计 **/
    public List<Map> productStatsByMchForDay(String mchNo, String createdStart, String createdEnd) {
        Map param = new HashMap<>();
        if (StrUtil.isNotBlank(mchNo)) { param.put("mchNo", mchNo); }
        if (StrUtil.isNotBlank(createdStart)) { param.put("createTimeStart", createdStart); }
        if (StrUtil.isNotBlank(createdEnd)) { param.put("createTimeEnd", createdEnd); }
        return payOrderMapper.selectProductStatsByMchForDay(param);
    }

    /** 生成首页交易统计数据类型 **/
    public List<Map> getReturnList(int daySpace, String createdStart, List<Map> payAndRefundOrderList) {
        List<Map> dayList = new ArrayList<>();
        DateTime endDay = DateUtil.parseDateTime(createdStart);
        // 先判断间隔天数 根据天数设置空的list
        for (int i = 0; i <= daySpace ; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("date", DateUtil.format(DateUtil.offsetDay(endDay, -i), "MM-dd"));
            dayList.add(map);
        }
        // 日期倒序排列
        Collections.reverse(dayList);

        List<Map> payListMap = new ArrayList<>(); // 收款的列
        List<Map> refundListMap = new ArrayList<>(); // 退款的列
        for (Map dayMap:dayList) {
            // 为收款列和退款列赋值默认参数【payAmount字段切记不可为string，否则前端图表解析不出来】
            Map<String, Object> payMap = new HashMap<>();
            payMap.put("date", dayMap.get("date").toString());
            payMap.put("type", "收款");
            payMap.put("payAmount", 0);

            Map<String, Object> refundMap = new HashMap<>();
            refundMap.put("date", dayMap.get("date").toString());
            refundMap.put("type", "退款");
            refundMap.put("payAmount", 0);
            for (Map payOrderMap:payAndRefundOrderList) {
                if (dayMap.get("date").equals(payOrderMap.get("groupDate"))) {
                    payMap.put("payAmount", payOrderMap.get("payAmount"));
                }
            }
            payListMap.add(payMap);
            for (Map refundOrderMap:payAndRefundOrderList) {
                if (dayMap.get("date").equals(refundOrderMap.get("groupDate"))) {
                    refundMap.put("payAmount", refundOrderMap.get("refundAmount"));
                }
            }
            refundListMap.add(refundMap);
        }
        payListMap.addAll(refundListMap);
        return payListMap;
    }


    /**
    *  计算支付订单商家入账金额
    * 商家订单入账金额 （支付金额 - 手续费 - 退款金额 - 总分账金额）
    * @author terrfly
    * @site https://www.jeequan.com
    * @date 2021/8/26 16:39
    */
    public Long calMchIncomeAmount(PayOrder dbPayOrder){

        //商家订单入账金额 （支付金额 - 手续费 - 退款金额 - 总分账金额）
        Long mchIncomeAmount = dbPayOrder.getAmount() - dbPayOrder.getMchFeeAmount() - dbPayOrder.getRefundAmount();

        return mchIncomeAmount <= 0 ? 0 : mchIncomeAmount;

    }

    /**
     * 通用列表查询条件
     * @param iPage
     * @param payOrder
     * @param paramJSON
     * @param wrapper
     * @return
     */
    public IPage<PayOrder> listByPage(IPage iPage, PayOrder payOrder, JSONObject paramJSON, LambdaQueryWrapper<PayOrder> wrapper) {
        if (StringUtils.isNotEmpty(payOrder.getPayOrderId())) {
            wrapper.eq(PayOrder::getPayOrderId, payOrder.getPayOrderId());
        }
        if (StringUtils.isNotEmpty(payOrder.getMchNo())) {
            wrapper.eq(PayOrder::getMchNo, payOrder.getMchNo());
        }
        if (StringUtils.isNotEmpty(payOrder.getIsvNo())) {
            wrapper.eq(PayOrder::getIsvNo, payOrder.getIsvNo());
        }
        if (payOrder.getMchType() != null) {
            wrapper.eq(PayOrder::getMchType, payOrder.getMchType());
        }
        if (StringUtils.isNotEmpty(payOrder.getWayCode())) {
            wrapper.eq(PayOrder::getWayCode, payOrder.getWayCode());
        }
        if (StringUtils.isNotEmpty(payOrder.getMchOrderNo())) {
            wrapper.eq(PayOrder::getMchOrderNo, payOrder.getMchOrderNo());
        }
        if (payOrder.getState() != null) {
            wrapper.eq(PayOrder::getState, payOrder.getState());
        }
        if (payOrder.getNotifyState() != null) {
            wrapper.eq(PayOrder::getNotifyState, payOrder.getNotifyState());
        }
        if (StringUtils.isNotEmpty(payOrder.getAppId())) {
            wrapper.eq(PayOrder::getAppId, payOrder.getAppId());
        }
 
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(PayOrder::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(PayOrder::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        // 三合一订单
        if (paramJSON != null && StringUtils.isNotEmpty(paramJSON.getString("unionOrderId"))) {
            wrapper.and(wr -> {
                wr.eq(PayOrder::getPayOrderId, paramJSON.getString("unionOrderId"))
                        .or().eq(PayOrder::getMchOrderNo, paramJSON.getString("unionOrderId"))
                        .or().eq(PayOrder::getChannelOrderNo, paramJSON.getString("unionOrderId"));
            });
        }

        // 隔日回调：支付成功时间与回调成功时间不在同一天
        if (paramJSON != null && paramJSON.containsKey("nextDayCallback")) {
            Boolean nextDayCallback = paramJSON.getBoolean("nextDayCallback");
            if (nextDayCallback != null && nextDayCallback) {
                wrapper.eq(PayOrder::getState, PayOrder.STATE_SUCCESS)
                       .isNotNull(PayOrder::getSuccessTime)
                       .isNotNull(PayOrder::getNotifySuccessTime)
                       .apply("DATE_FORMAT(success_time, '%Y-%m-%d') != DATE_FORMAT(notify_success_time, '%Y-%m-%d')");
            } else if (nextDayCallback != null && !nextDayCallback) {
                // 选择"否"时，查询当日回调的订单（支付成功时间与回调成功时间在同一天）
                wrapper.eq(PayOrder::getState, PayOrder.STATE_SUCCESS)
                       .isNotNull(PayOrder::getSuccessTime)
                       .isNotNull(PayOrder::getNotifySuccessTime)
                       .apply("DATE_FORMAT(success_time, '%Y-%m-%d') = DATE_FORMAT(notify_success_time, '%Y-%m-%d')");
            }
        }

        wrapper.orderByDesc(PayOrder::getCreatedAt);

        return page(iPage, wrapper);
    }

    /**
     * 订单列表统计
     * @param paramJSON 查询参数
     * @return 统计数据
     */
    public JSONObject orderListStats(JSONObject paramJSON) {
        JSONObject result = new JSONObject();
        
        // 构建查询参数 Map
        Map<String, Object> param = new HashMap<>();
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("mchNo"))) {
                param.put("mchNo", paramJSON.getString("mchNo"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("isvNo"))) {
                param.put("isvNo", paramJSON.getString("isvNo"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("appId"))) {
                param.put("appId", paramJSON.getString("appId"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("wayCode"))) {
                param.put("wayCode", paramJSON.getString("wayCode"));
            }
            if (paramJSON.getInteger("state") != null) {
                param.put("state", paramJSON.getInteger("state"));
            }
            if (paramJSON.getInteger("notifyState") != null) {
                param.put("notifyState", paramJSON.getInteger("notifyState"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("unionOrderId"))) {
                // 三合一订单号查询
                param.put("payOrderId", paramJSON.getString("unionOrderId"));
                param.put("mchOrderNo", paramJSON.getString("unionOrderId"));
                param.put("channelOrderNo", paramJSON.getString("unionOrderId"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                param.put("createTimeStart", paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                param.put("createTimeEnd", paramJSON.getString("createdEnd"));
            }
        }
        
        // 调用 Mapper 查询统计
        Map<String, Object> statsMap = payOrderMapper.selectOrderListStats(param);
        
        // 计算成功率
        Long submitOrderCount = statsMap.get("submitOrderCount") != null ? 
            Long.valueOf(statsMap.get("submitOrderCount").toString()) : 0L;
        Long successOrderCount = statsMap.get("successOrderCount") != null ? 
            Long.valueOf(statsMap.get("successOrderCount").toString()) : 0L;
        
        BigDecimal successRate = BigDecimal.ZERO;
        if (submitOrderCount > 0) {
            successRate = new BigDecimal(successOrderCount).divide(new BigDecimal(submitOrderCount), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        
        result.put("submitOrderCount", submitOrderCount);
        result.put("allAmount", statsMap.get("allAmount") != null ? statsMap.get("allAmount") : "0.00");
        result.put("successOrderCount", successOrderCount);
        result.put("successAmount", statsMap.get("successAmount") != null ? statsMap.get("successAmount") : "0.00");
        result.put("mchIncome", statsMap.get("mchIncome") != null ? statsMap.get("mchIncome") : "0.00");
        result.put("agentIncome", statsMap.get("agentIncome") != null ? statsMap.get("agentIncome") : "0.00");
        result.put("platformIncome", statsMap.get("platformIncome") != null ? statsMap.get("platformIncome") : "0.00");
        result.put("unpaidOrderCount", statsMap.get("unpaidOrderCount") != null ? statsMap.get("unpaidOrderCount") : 0L);
        result.put("unpaidAmount", statsMap.get("unpaidAmount") != null ? statsMap.get("unpaidAmount") : "0.00");
        result.put("successRate", successRate);
        
        return result;
    }
}
