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

@Schema(description = "商户账户每日快照表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_account_daily_snapshot")
public class MchAccountDailySnapshot extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchAccountDailySnapshot> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    @Schema(title = "id", description = "主键ID")
    @TableId
    private Long id;

    @Schema(title = "mchNo", description = "商户号")
    private String mchNo;

    @Schema(title = "snapshotAmount", description = "当日账户余额快照,单位分")
    private Long snapshotAmount;

    @Schema(title = "snapshotDate", description = "快照日期(每天0:00更新)")
    private Date snapshotDate;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;
}
