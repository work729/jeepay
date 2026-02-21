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

@Schema(description = "商户账户变动记录表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_account_change_log")
public class MchAccountChangeLog extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchAccountChangeLog> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    @Schema(title = "id", description = "主键ID")
    @TableId
    private Long id;

    @Schema(title = "mchNo", description = "商户号")
    private String mchNo;

    @Schema(title = "accountType", description = "账户类型: 1-账户余额,2-代付额度")
    private Byte accountType;

    @Schema(title = "changeDirection", description = "变动方向: 1-增加,2-减少")
    private Byte changeDirection;

    @Schema(title = "amount", description = "变动金额,单位分")
    private Long amount;

    @Schema(title = "beforeAmount", description = "变动前金额,单位分")
    private Long beforeAmount;

    @Schema(title = "afterAmount", description = "变动后金额,单位分")
    private Long afterAmount;

    @Schema(title = "operatorId", description = "操作员ID")
    private Long operatorId;

    @Schema(title = "operatorName", description = "操作员姓名")
    private String operatorName;

    @Schema(title = "remark", description = "备注")
    private String remark;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;
}

