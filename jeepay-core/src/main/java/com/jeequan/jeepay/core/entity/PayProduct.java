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
import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "支付产品表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_product")
public class PayProduct extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<PayProduct> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    @Schema(title = "id", description = "主键ID")
    @TableId
    private Long id;

    @Schema(title = "productName", description = "产品名称")
    private String productName;

    @Schema(title = "productType", description = "产品类型")
    private Integer productType;

    @Schema(title = "state", description = "状态: 0-停用, 1-启用")
    private Integer state;

    @Schema(title = "agentRate", description = "代理点位")
    private BigDecimal agentRate;

    @Schema(title = "mchRate", description = "商户费率")
    private BigDecimal mchRate;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;

    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;
}

