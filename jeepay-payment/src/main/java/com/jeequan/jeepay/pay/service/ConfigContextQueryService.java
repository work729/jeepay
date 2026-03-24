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
package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.model.params.IsvParams;
import com.jeequan.jeepay.core.model.params.IsvsubMchParams;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.pay.model.*;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* 配置信息查询服务 （兼容 缓存 和 直接查询方式）
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/11/18 14:41
*/
@Slf4j
@Service
public class ConfigContextQueryService {

    @Autowired ConfigContextService configContextService;
    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAppService mchAppService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;

    private boolean isCache(){
        return SysConfigService.IS_USE_CACHE;
    }

    public MchApp queryMchApp(String mchNo, String mchAppId){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getMchApp();
        }

        return mchAppService.getOneByMch(mchNo, mchAppId);
    }

    public MchAppConfigContext queryMchInfoAndAppInfo(String mchNo, String mchAppId){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchNo, mchAppId);
        }

        MchInfo mchInfo = mchInfoService.getById(mchNo);
        MchApp mchApp = queryMchApp(mchNo, mchAppId);

        if(mchInfo == null || mchApp == null){
            return null;
        }

        MchAppConfigContext result = new MchAppConfigContext();
        result.setMchInfo(mchInfo);
        result.setMchNo(mchNo);
        result.setMchType(mchInfo.getType());

        result.setMchApp(mchApp);
        result.setAppId(mchAppId);

        return result;
    }


    public NormalMchParams queryNormalMchParams(String mchNo, String mchAppId, String ifCode){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getNormalMchParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                .eq(PayInterfaceConfig::getInfoId, mchAppId)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );

        if(payInterfaceConfig == null){
            return null;
        }

        return NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
    }

    // 已废弃：第三方通道封装器访问器

    /** 获取仅商户维度的配置信息上下文（无应用） **/
    public MchAppConfigContext getMchInfoContext(String mchNo){
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        if(mchInfo == null){
            return null;
        }
        MchAppConfigContext ctx = new MchAppConfigContext();
        ctx.setMchNo(mchInfo.getMchNo());
        ctx.setMchType(mchInfo.getType());
        ctx.setMchInfo(mchInfo);

        payInterfaceConfigService.list(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH)
                .eq(PayInterfaceConfig::getInfoId, mchNo)
        ).forEach(cfg -> ctx.getNormalMchParamsMap().put(
                cfg.getIfCode(),
                NormalMchParams.factory(cfg.getIfCode(), cfg.getIfParams())
        ));
        return ctx;
    }

}
