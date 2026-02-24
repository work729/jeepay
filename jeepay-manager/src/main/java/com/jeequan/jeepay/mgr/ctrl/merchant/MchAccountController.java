package com.jeequan.jeepay.mgr.ctrl.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.MchAccountDailySnapshot;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchAccountDailySnapshotService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "商户账户列表")
@RestController
@RequestMapping("/api/mch/account")
public class MchAccountController extends CommonCtrl {

    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAccountDailySnapshotService snapshotService;

    @Operation(summary = "查询商户账户列表", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "mchNo", description = "商户号"),
            @Parameter(name = "mchName", description = "商户名称")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_ACCOUNT_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<MchInfo> list() {
        MchInfo mchInfo = getObject(MchInfo.class);

        IPage<MchInfo> pages = mchInfoService.page(getIPage(),
                MchInfo.gw()
                        .like(StringUtils.isNotBlank(mchInfo.getMchNo()), MchInfo::getMchNo, mchInfo.getMchNo())
                        .like(StringUtils.isNotBlank(mchInfo.getMchName()), MchInfo::getMchName, mchInfo.getMchName())
                        .orderByDesc(MchInfo::getCreatedAt)
        );

        List<String> mchNos = pages.getRecords().stream().map(MchInfo::getMchNo).collect(Collectors.toList());
        if (!mchNos.isEmpty()) {
            Date today = new Date();
            // 查询当日快照（按日期比较，DB中为DATE）
            List<MchAccountDailySnapshot> snaps = snapshotService.list(
                    MchAccountDailySnapshot.gw().in(MchAccountDailySnapshot::getMchNo, mchNos)
            );
            for (MchInfo info : pages.getRecords()) {
                Long dailyAmount = null;
                for (MchAccountDailySnapshot s : snaps) {
                    if (info.getMchNo().equals(s.getMchNo())) {
                        dailyAmount = s.getSnapshotAmount();
                        break;
                    }
                }
                info.addExt("dailyAmount", dailyAmount);
                info.addExt("currentBalance", info.getAccountBalance());
            }
        }
        return ApiPageRes.pages(pages);
    }
}
