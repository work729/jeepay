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
package com.jeequan.jeepay.mch.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.PayWayService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付订单管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "订单管理（支付类）")
@RestController
@RequestMapping("/api/payOrder")
public class PayOrderController extends CommonCtrl {

    @Autowired private PayOrderService payOrderService;
    @Autowired private PayWayService payWayService;
    @Autowired private MchAppService mchAppService;
    @Autowired private SysConfigService sysConfigService;

    /**
     * @Author: ZhuXiao
     * @Description: 订单信息列表
     * @Date: 10:43 2021/5/13
    */
    @Operation(summary = "支付订单信息列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "unionOrderId", description = "支付/商户/渠道订单号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "wayCode", description = "支付方式代码"),
            @Parameter(name = "state", description = "支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭"),
            @Parameter(name = "notifyState", description = "向下游回调状态，0-未发送，1-已发送"),
            @Parameter(name = "nextDayCallback", description = "是否隔日回调：true-是，false-否（筛选支付成功时间与创建时间不在同一天的订单）"),
    })
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @GetMapping
    public ApiPageRes<PayOrder> list() {

        PayOrder payOrder = getObject(PayOrder.class);
        JSONObject paramJSON = getReqParamJSON();

        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
        wrapper.eq(PayOrder::getMchNo, getCurrentMchNo());

        IPage<PayOrder> pages = payOrderService.listByPage(getIPage(), payOrder, paramJSON, wrapper);

        // 得到所有支付方式
        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayService.list();
        if (!CollectionUtils.isEmpty(payWayList)) {
            for (PayWay payWay:payWayList) {
                payWayNameMap.put(payWay.getWayCode(), payWay.getWayName());
            }
            for (PayOrder order:pages.getRecords()) {
                // 存入支付方式名称
                if (StringUtils.isNotEmpty(payWayNameMap.get(order.getWayCode()))) {
                    order.addExt("wayName", payWayNameMap.get(order.getWayCode()));
                }else {
                    order.addExt("wayName", order.getWayCode());
                }
            }
        }

        return ApiPageRes.pages(pages);
    }

    /**
     * @describe: 订单列表统计（商户侧）
     */
    @Operation(summary = "订单列表统计")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间"),
            @Parameter(name = "unionOrderId", description = "支付/商户/渠道订单号"),
            @Parameter(name = "appId", description = "应用 ID"),
            @Parameter(name = "wayCode", description = "支付方式代码"),
            @Parameter(name = "state", description = "支付状态"),
            @Parameter(name = "notifyState", description = "向下游回调状态")
    })
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @GetMapping("/stats")
    public ApiRes stats() {
        JSONObject paramJSON = getReqParamJSON();
        if (paramJSON == null) {
            paramJSON = new JSONObject();
        }
        // 商户侧限定当前商户
        paramJSON.put("mchNo", getCurrentMchNo());
        return ApiRes.ok(payOrderService.orderListStats(paramJSON));
    }

    /**
     * @Author: ZhuXiao
     * @Description: 支付订单信息
     * @Date: 10:43 2021/5/13
    */
    @Operation(summary = "支付订单信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "payOrderId", description = "支付订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @GetMapping("/{payOrderId}")
    public ApiRes<PayOrder> detail(@PathVariable("payOrderId") String payOrderId) {
        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        if (!payOrder.getMchNo().equals(getCurrentMchNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_PERMISSION_ERROR);
        }
        return ApiRes.ok(payOrder);
    }

    /**
     * 支付订单列表导出（CSV）
     */
    @Operation(summary = "支付订单列表导出（CSV）")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "createdStart", description = "开始时间"),
            @Parameter(name = "createdEnd", description = "结束时间"),
            @Parameter(name = "unionOrderId", description = "支付/商户/渠道订单号"),
            @Parameter(name = "wayCode", description = "支付方式代码"),
            @Parameter(name = "state", description = "支付状态"),
            @Parameter(name = "notifyState", description = "回调状态"),
            @Parameter(name = "nextDayCallback", description = "是否隔日回调")
    })
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        PayOrder payOrder = getObject(PayOrder.class);
        JSONObject paramJSON = getReqParamJSON();
        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();
        wrapper.eq(PayOrder::getMchNo, getCurrentMchNo());

        if (payOrder != null) {
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(payOrder.getPayOrderId())) {
                wrapper.eq(PayOrder::getPayOrderId, payOrder.getPayOrderId());
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(payOrder.getIsvNo())) {
                wrapper.eq(PayOrder::getIsvNo, payOrder.getIsvNo());
            }
            if (payOrder.getMchType() != null) {
                wrapper.eq(PayOrder::getMchType, payOrder.getMchType());
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(payOrder.getWayCode())) {
                wrapper.eq(PayOrder::getWayCode, payOrder.getWayCode());
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(payOrder.getMchOrderNo())) {
                wrapper.eq(PayOrder::getMchOrderNo, payOrder.getMchOrderNo());
            }
            if (payOrder.getState() != null) {
                wrapper.eq(PayOrder::getState, payOrder.getState());
            }
            if (payOrder.getNotifyState() != null) {
                wrapper.eq(PayOrder::getNotifyState, payOrder.getNotifyState());
            }
        }

        if (paramJSON != null) {
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(PayOrder::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(PayOrder::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        if (paramJSON != null && org.apache.commons.lang3.StringUtils.isNotEmpty(paramJSON.getString("unionOrderId"))) {
            wrapper.and(wr -> {
                wr.eq(PayOrder::getPayOrderId, paramJSON.getString("unionOrderId"))
                        .or().eq(PayOrder::getMchOrderNo, paramJSON.getString("unionOrderId"))
                        .or().eq(PayOrder::getChannelOrderNo, paramJSON.getString("unionOrderId"));
            });
        }
        if (paramJSON != null && paramJSON.containsKey("nextDayCallback")) {
            Boolean nextDayCallback = paramJSON.getBoolean("nextDayCallback");
            if (nextDayCallback != null && nextDayCallback) {
                wrapper.eq(PayOrder::getState, PayOrder.STATE_SUCCESS)
                        .isNotNull(PayOrder::getSuccessTime)
                        .isNotNull(PayOrder::getNotifySuccessTime)
                        .apply("DATE_FORMAT(success_time, '%Y-%m-%d') != DATE_FORMAT(notify_success_time, '%Y-%m-%d')");
            } else if (nextDayCallback != null && !nextDayCallback) {
                wrapper.eq(PayOrder::getState, PayOrder.STATE_SUCCESS)
                        .isNotNull(PayOrder::getSuccessTime)
                        .isNotNull(PayOrder::getNotifySuccessTime)
                        .apply("DATE_FORMAT(success_time, '%Y-%m-%d') = DATE_FORMAT(notify_success_time, '%Y-%m-%d')");
            }
        }

        wrapper.orderByDesc(PayOrder::getCreatedAt);

        List<PayOrder> orders = payOrderService.list(wrapper);

        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayService.list();
        if (!org.springframework.util.CollectionUtils.isEmpty(payWayList)) {
            payWayNameMap = payWayList.stream().collect(Collectors.toMap(PayWay::getWayCode, PayWay::getWayName));
        }

        String filename = "pay-orders-" + System.currentTimeMillis() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));

        try (OutputStream os = response.getOutputStream()) {
            os.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            String header = String.join(",",
                    "支付订单号","商户订单号","渠道订单号",
                    "应用ID","支付方式","支付金额(元)","退款金额(元)","手续费(元)",
                    "订单状态","创建时间","支付成功时间","回调成功时间"
            ) + "\n";
            os.write(header.getBytes(StandardCharsets.UTF_8));
            for (PayOrder o : orders) {
                String wayName = org.apache.commons.lang3.StringUtils.defaultString(payWayNameMap.get(o.getWayCode()), o.getWayCode());
                String stateName =
                        o.getState() == 0 ? "订单生成" :
                        o.getState() == 1 ? "支付中" :
                        o.getState() == 2 ? "支付成功" :
                        o.getState() == 3 ? "支付失败" :
                        o.getState() == 4 ? "已撤销" :
                        o.getState() == 5 ? "已退款" :
                        o.getState() == 6 ? "订单关闭" : "未知";
                String line = String.join(",",
                        safe(o.getPayOrderId()),
                        safe(o.getMchOrderNo()),
                        safe(o.getChannelOrderNo()),
                        safe(wayName),
                        amountYuan(o.getAmount()),
                        amountYuan(o.getRefundAmount()),
                        amountYuan(o.getMchFeeAmount()),
                        stateName,
                        safeDate(o.getCreatedAt()),
                        safeDate(o.getSuccessTime()),
                        safeDate(o.getNotifySuccessTime())
                ) + "\n";
                os.write(line.getBytes(StandardCharsets.UTF_8));
            }
            os.flush();
        }
    }

    private String amountYuan(Long cents) {
        if (cents == null) return "0.00";
        java.math.BigDecimal bd = new java.math.BigDecimal(cents).divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
        return bd.toPlainString();
    }
    private String safe(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\r\\n]", " ");
    }
    private String safeDate(java.util.Date d) {
        if (d == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    }




    /**
     * 发起订单退款
     * @author terrfly
     * @site https://www.jeequan.com
     * @date 2021/6/17 16:38
     */
    @Operation(summary = "发起订单退款")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "payOrderId", description = "支付订单号", required = true),
            @Parameter(name = "refundAmount", description = "退款金额", required = true),
            @Parameter(name = "refundReason", description = "退款原因", required = true)
    })
    @MethodLog(remark = "发起订单退款")
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_REFUND')")
    @PostMapping("/refunds/{payOrderId}")
    public ApiRes refund(@PathVariable("payOrderId") String payOrderId) {

        Long refundAmount = getRequiredAmountL("refundAmount");
        String refundReason = getValStringRequired("refundReason");

        PayOrder payOrder = payOrderService.getById(payOrderId);
        if (payOrder == null || !payOrder.getMchNo().equals(getCurrentMchNo())) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        if(payOrder.getState() != PayOrder.STATE_SUCCESS){
            throw new BizException("订单状态不正确");
        }

        if(payOrder.getRefundAmount() + refundAmount > payOrder.getAmount()){
            throw new BizException("退款金额超过订单可退款金额！");
        }


        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        request.setBizModel(model);

        model.setMchNo(payOrder.getMchNo());     // 商户号
        model.setAppId(payOrder.getAppId());
        model.setPayOrderId(payOrderId);
        model.setMchRefundNo(SeqKit.genMhoOrderId());
        model.setRefundAmount(refundAmount);
        model.setRefundReason(refundReason);
        model.setCurrency("CNY");

        MchApp mchApp = mchAppService.getById(payOrder.getAppId());

        String mchSecret = null;
        com.jeequan.jeepay.core.entity.MchInfo mchInfo = com.jeequan.jeepay.core.utils.SpringBeansUtil.getBean(com.jeequan.jeepay.service.impl.MchInfoService.class).getById(payOrder.getMchNo());
        if(mchInfo != null && mchInfo.getMchSecret() != null && !mchInfo.getMchSecret().trim().isEmpty()){
            mchSecret = mchInfo.getMchSecret();
        }
        JeepayClient jeepayClient = new JeepayClient(sysConfigService.getDBApplicationConfig().getPaySiteUrl(), mchSecret);

        try {
            RefundOrderCreateResponse response = jeepayClient.execute(request);
            if(response.getCode() != 0){
                throw new BizException(response.getMsg());
            }
            return ApiRes.ok(response.get());
        } catch (JeepayException e) {
            throw new BizException(e.getMessage());
        }
    }

}
