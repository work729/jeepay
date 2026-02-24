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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.MchPayPassageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户支付通道表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Service
public class MchPayPassageService extends ServiceImpl<MchPayPassageMapper, MchPayPassage> {

    @Autowired private PayInterfaceDefineService payInterfaceDefineService;

    /**
     * @Author: ZhuXiao
     * @Description: 根据支付方式查询可用的支付接口列表
     * @Date: 9:56 2021/5/10
    */
    public List<JSONObject> selectAvailablePayInterfaceList(String wayCode, String appId, Byte infoType, Byte mchType) {
        // t_mch_pay_passage 已废弃，返回空集合
        return new ArrayList<>();
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateBatchSelf(List<MchPayPassage> mchPayPassageList, String mchNo) {
        // t_mch_pay_passage 已废弃，不再持久化通道配置
    }


    /** 根据应用ID 和 支付方式， 查询出商户可用的支付接口 **/
    public MchPayPassage findMchPayPassage(String mchNo, String appId, String wayCode){
        // t_mch_pay_passage 已废弃，返回空
        return null;
    }


}
