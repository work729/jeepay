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
package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchAccountChangeLog;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.MchAccountChangeLogMapper;
import com.jeequan.jeepay.service.mapper.MchInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchAccountChangeService extends ServiceImpl<MchAccountChangeLogMapper, MchAccountChangeLog> {

    public static final byte ACCOUNT_TYPE_BALANCE = 1;
    public static final byte ACCOUNT_TYPE_PAYOUT_QUOTA = 2;

    public static final byte DIRECTION_INCREASE = 1;
    public static final byte DIRECTION_DECREASE = 2;

    @Autowired
    private MchInfoMapper mchInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    public void changeAccount(String mchNo, byte accountType, byte direction, long amount, Long operatorId, String operatorName, String remark) {
        MchInfo mchInfo = mchInfoMapper.selectById(mchNo);
        if (mchInfo == null) {
            throw new BizException("商户不存在");
        }
        Long current;
        if (accountType == ACCOUNT_TYPE_BALANCE) {
            current = mchInfo.getAccountBalance() == null ? 0L : mchInfo.getAccountBalance();
        } else if (accountType == ACCOUNT_TYPE_PAYOUT_QUOTA) {
            current = mchInfo.getPayoutQuota() == null ? 0L : mchInfo.getPayoutQuota();
        } else {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        long after;
        if (direction == DIRECTION_INCREASE) {
            after = current + amount;
        } else if (direction == DIRECTION_DECREASE) {
            after = current - amount;
            if (after < 0) {
                throw new BizException("账户余额不足");
            }
        } else {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        LambdaUpdateWrapper<MchInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MchInfo::getMchNo, mchNo);
        if (accountType == ACCOUNT_TYPE_BALANCE) {
            updateWrapper.eq(MchInfo::getAccountBalance, current).set(MchInfo::getAccountBalance, after);
        } else {
            updateWrapper.eq(MchInfo::getPayoutQuota, current).set(MchInfo::getPayoutQuota, after);
        }
        int rows = mchInfoMapper.update(null, updateWrapper);
        if (rows != 1) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }

        MchAccountChangeLog log = new MchAccountChangeLog();
        log.setMchNo(mchNo);
        log.setAccountType(accountType);
        log.setChangeDirection(direction);
        log.setAmount(amount);
        log.setBeforeAmount(current);
        log.setAfterAmount(after);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        save(log);
    }
}

