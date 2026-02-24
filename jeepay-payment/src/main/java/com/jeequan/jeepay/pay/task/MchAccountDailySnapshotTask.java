package com.jeequan.jeepay.pay.task;

import com.jeequan.jeepay.core.entity.MchAccountDailySnapshot;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.service.impl.MchAccountDailySnapshotService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class MchAccountDailySnapshotTask {

    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAccountDailySnapshotService snapshotService;

    // 每天0点执行
    @Scheduled(cron = "0 0 0 * * ?")
    public void start() {
        List<MchInfo> all = mchInfoService.list(MchInfo.gw());
        Date today = new Date();
        int count = 0;
        for (MchInfo info : all) {
            Long balance = info.getAccountBalance() == null ? 0L : info.getAccountBalance();
            MchAccountDailySnapshot exist = snapshotService.getOne(
                    MchAccountDailySnapshot.gw()
                            .eq(MchAccountDailySnapshot::getMchNo, info.getMchNo())
                            .eq(MchAccountDailySnapshot::getSnapshotDate, today),
                    false
            );
            if (exist == null) {
                MchAccountDailySnapshot snap = new MchAccountDailySnapshot()
                        .setMchNo(info.getMchNo())
                        .setSnapshotAmount(balance)
                        .setSnapshotDate(today);
                snapshotService.save(snap);
                count++;
            } else {
                exist.setSnapshotAmount(balance);
                snapshotService.updateById(exist);
                count++;
            }
        }
        log.info("账户每日快照写入完成，处理商户数: {}", count);
    }
}
