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

@Schema(description = "代理商信息表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_agent_info")
public class AgentInfo extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<AgentInfo> gw() {
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID = 1L;

    @Schema(title = "id", description = "代理ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(title = "agentName", description = "代理名称")
    private String agentName;

    @Schema(title = "contactName", description = "联系人姓名")
    private String contactName;

    @Schema(title = "contactTel", description = "联系人手机号")
    private String contactTel;

    @Schema(title = "contactEmail", description = "联系人邮箱")
    private String contactEmail;

    @Schema(title = "state", description = "状态: 0-停用, 1-正常")
    private Byte state;

    @Schema(title = "remark", description = "备注")
    private String remark;

    @Schema(title = "createdUid", description = "创建者用户ID")
    private Long createdUid;

    @Schema(title = "createdBy", description = "创建者姓名")
    private String createdBy;

    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;

    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;
}

