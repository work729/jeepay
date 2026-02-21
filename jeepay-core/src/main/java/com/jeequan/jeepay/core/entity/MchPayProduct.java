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

@Schema(description = "商户支付产品关联表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_pay_product")
public class MchPayProduct extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchPayProduct> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    @Schema(title = "id", description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(title = "mchNo", description = "商户号")
    private String mchNo;

    @Schema(title = "productId", description = "支付产品ID")
    private Long productId;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;

    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;
}

