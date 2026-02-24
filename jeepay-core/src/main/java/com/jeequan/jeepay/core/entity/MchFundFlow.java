package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Schema(description = "商户资金流水表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_fund_flow")
public class MchFundFlow extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchFundFlow> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    @Schema(title = "id", description = "主键ID")
    @TableId
    private Long id;

    @Schema(title = "mchNo", description = "商户号")
    private String mchNo;

    @Schema(title = "beforeAmount", description = "变更前金额,单位分")
    private Long beforeAmount;

    @Schema(title = "changeAmount", description = "变更金额,单位分")
    private Long changeAmount;

    @Schema(title = "afterAmount", description = "变更后金额,单位分")
    private Long afterAmount;

    @Schema(title = "bizType", description = "业务类型: 1-支付入账,2-退款出账,3-人工增加,4-人工减少")
    private Byte bizType;

    @Schema(title = "bizOrderId", description = "业务订单ID")
    private String bizOrderId;

    @Schema(title = "bizOrderAmount", description = "业务订单金额,单位分")
    private Long bizOrderAmount;

    @Schema(title = "operatorId", description = "操作员ID")
    private Long operatorId;

    @Schema(title = "operatorName", description = "操作员姓名")
    private String operatorName;

    @Schema(title = "remark", description = "备注")
    private String remark;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;
}
