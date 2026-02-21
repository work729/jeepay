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
package com.jeequan.jeepay.mgr.ctrl.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.AgentInfo;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.AgentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "代理商管理（基本信息）")
@RestController
@RequestMapping("/api/agentInfo")
public class AgentInfoController extends CommonCtrl {

    @Autowired
    private AgentInfoService agentInfoService;

    @Operation(summary = "代理商列表", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数（-1时查全部数据）"),
            @Parameter(name = "agentId", description = "代理ID"),
            @Parameter(name = "agentName", description = "代理名称"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_AGENT_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<AgentInfo> list() {
        AgentInfo agentInfo = getObject(AgentInfo.class);
        LambdaQueryWrapper<AgentInfo> wrapper = AgentInfo.gw();
        if (agentInfo.getId() != null) {
            wrapper.eq(AgentInfo::getId, agentInfo.getId());
        }
        if (StringUtils.isNotEmpty(agentInfo.getAgentName())) {
            wrapper.eq(AgentInfo::getAgentName, agentInfo.getAgentName());
        }
        if (agentInfo.getState() != null) {
            wrapper.eq(AgentInfo::getState, agentInfo.getState());
        }
        wrapper.orderByDesc(AgentInfo::getCreatedAt);
        IPage<AgentInfo> pages = agentInfoService.page(getIPage(true), wrapper);
        return ApiPageRes.pages(pages);
    }

    @Operation(summary = "新增代理商", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "agentName", description = "代理名称", required = true),
            @Parameter(name = "contactName", description = "联系人姓名"),
            @Parameter(name = "contactTel", description = "联系人手机号"),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_AGENT_INFO_ADD')")
    @MethodLog(remark = "新增代理商")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ApiRes add() {
        AgentInfo agentInfo = getObject(AgentInfo.class);
        agentInfo.setCreatedUid(getCurrentUser().getSysUser().getSysUserId());
        agentInfo.setCreatedBy(getCurrentUser().getSysUser().getRealname());
        boolean result = agentInfoService.save(agentInfo);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    @Operation(summary = "删除代理商", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "代理ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_AGENT_INFO_DEL')")
    @MethodLog(remark = "删除代理商")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("id") Long id) {
        boolean result = agentInfoService.removeById(id);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }

    @Operation(summary = "更新代理商信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "代理ID", required = true),
            @Parameter(name = "agentName", description = "代理名称", required = true),
            @Parameter(name = "contactName", description = "联系人姓名"),
            @Parameter(name = "contactTel", description = "联系人手机号"),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_AGENT_INFO_EDIT')")
    @MethodLog(remark = "更新代理商信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("id") Long id) {
        AgentInfo agentInfo = getObject(AgentInfo.class);
        agentInfo.setId(id);
        boolean result = agentInfoService.updateById(agentInfo);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @Operation(summary = "查看代理商信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "代理ID", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_AGENT_INFO_VIEW', 'ENT_AGENT_INFO_EDIT')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ApiRes<AgentInfo> detail(@PathVariable("id") Long id) {
        AgentInfo agentInfo = agentInfoService.getById(id);
        if (agentInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(agentInfo);
    }
}

