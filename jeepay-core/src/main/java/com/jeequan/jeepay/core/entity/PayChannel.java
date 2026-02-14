/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0
 */
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

@Schema(description = "支付通道表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_channel")
public class PayChannel extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<PayChannel> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    @Schema(title = "channelSign", description = "通道标识")
    @TableId
    private String channelSign;

    @Schema(title = "channelName", description = "通道名称")
    private String channelName;

    @Schema(title = "ifCode", description = "接口代码")
    private String ifCode;

    @Schema(title = "state", description = "通道状态: 0-停用, 1-启用")
    private Integer state;

    @Schema(title = "isFloat", description = "是否浮动: 0-否, 1-是")
    private Integer isFloat;

    @Schema(title = "remark", description = "备注信息")
    private String remark;

    @Schema(title = "weight", description = "轮询权重")
    private Integer weight;

    @Schema(title = "accountName", description = "账户名称")
    private String accountName;

    @Schema(title = "channelMchId", description = "渠道商户ID")
    private String channelMchId;

    @Schema(title = "channelSignConfig", description = "通道标识配置JSON")
    private String channelSignConfig;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;

    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;
}
