package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jeequan.jeepay.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Schema(description = "商户结算配置表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_settle_config")
public class MchSettleConfig extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "mch_no", type = IdType.INPUT)
    private String mchNo;

    @Schema(description = "是否继承系统配置: 1-继承, 0-自定义")
    private Byte inheritSystem;

    @Schema(description = "提现开关: 0-关闭,1-开启")
    private Byte withdrawEnable;

    @Schema(description = "允许星期几提现, 逗号分隔1-7")
    private String withdrawDays;

    @Schema(description = "每日提现开始时间 HH:mm:ss")
    private String withdrawStartTime;

    @Schema(description = "每日提现结束时间 HH:mm:ss")
    private String withdrawEndTime;

    @Schema(description = "每日提现次数限制")
    private Integer dailyTimes;

    @Schema(description = "每日提现最大金额, 单位分")
    private Long dailyMaxAmount;

    @Schema(description = "单笔最大提现金额, 单位分")
    private Long singleMaxAmount;

    @Schema(description = "单笔最小提现金额, 单位分")
    private Long singleMinAmount;

    @Schema(description = "结算手续费类型: PERCENT/FIXED")
    private String feeType;

    @Schema(description = "每笔手续费, 百分比时存费率(万分比)或固定金额(分)")
    private Long feeAmount;

    @Schema(description = "单笔手续费上限, 单位分")
    private Long feeMaxAmount;

    @Schema(description = "结算类型: AUTO/MANUAL")
    private String settleType;

    @Schema(description = "结算方式: D0/D1")
    private String settleMode;
}

