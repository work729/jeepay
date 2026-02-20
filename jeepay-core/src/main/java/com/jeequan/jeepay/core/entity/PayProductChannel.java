package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

@Schema(description = "支付产品通道关联表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_product_channel")
public class PayProductChannel extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<PayProductChannel> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    @Schema(title = "id", description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(title = "productId", description = "支付产品ID")
    private Long productId;

    @Schema(title = "channelId", description = "通道ID")
    private Long channelId;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;

    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;
}
