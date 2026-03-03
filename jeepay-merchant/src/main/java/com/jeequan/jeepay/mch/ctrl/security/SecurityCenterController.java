package com.jeequan.jeepay.mch.ctrl.security;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import java.util.UUID;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.TotpUtil;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "安全中心")
@RestController
@RequestMapping("/api/security")
public class SecurityCenterController extends CommonCtrl {

    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAppService mchAppService;
    @Autowired private SysUserService sysUserService;

    @Operation(summary = "安全中心-状态信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @GetMapping("/info")
    public ApiRes info() {
        String mchNo = getCurrentMchNo();
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        JeeUserDetails cu = getCurrentUser();
        SysUser user = sysUserService.getById(cu.getSysUser().getSysUserId());
        JSONObject data = new JSONObject();
        data.put("loginSecurityType", mchInfo == null ? null : mchInfo.getLoginSecurityType());
        data.put("paySecurityType", mchInfo == null ? null : mchInfo.getPaySecurityType());
        data.put("payPasswordSet", mchInfo != null && StringUtils.isNotBlank(mchInfo.getPayPassword()));
        data.put("googleBound", user.getGoogleAuthEnabled() != null && user.getGoogleAuthEnabled() == CS.YES);
        List<JSONObject> apps = new ArrayList<>();
        List<MchApp> list = mchAppService.list(MchApp.gw().eq(MchApp::getMchNo, mchNo));
        boolean secretSet = mchInfo != null && StringUtils.isNotBlank(mchInfo.getMchSecret());
        for (MchApp app : list) {
            JSONObject o = new JSONObject();
            o.put("appId", app.getAppId());
            o.put("appName", app.getAppName());
            o.put("state", app.getState());
            o.put("secretSet", secretSet);
            apps.add(o);
        }
        data.put("apps", apps);
        data.put("mchSecretSet", mchInfo != null && StringUtils.isNotBlank(mchInfo.getMchSecret()));
        return ApiRes.ok(data);
    }

    @Operation(summary = "更新登录安全类型")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "type", description = "登录安全类型: 0-仅密码, 1-密码+谷歌", required = true)
    })
    @PostMapping("/login/type")
    public ApiRes updateLoginType() {
        Byte type = getValByteRequired("type");
        if (type != 0 && type != 1) {
            throw new BizException("登录安全类型参数有误");
        }
        MchInfo update = new MchInfo();
        update.setMchNo(getCurrentMchNo());
        update.setLoginSecurityType(type);
        mchInfoService.updateById(update);
        return ApiRes.ok();
    }

    @Operation(summary = "更新支付安全类型")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "type", description = "支付安全类型: 0-无需验证, 1-仅支付密码, 2-仅谷歌, 3-支付密码+谷歌", required = true)
    })
    @PostMapping("/pay/type")
    public ApiRes updatePayType() {
        Byte type = getValByteRequired("type");
        if (type < 0 || type > 3) {
            throw new BizException("支付安全类型参数有误");
        }
        if (type == 2 || type == 3) {
            SysUser user = sysUserService.getById(getCurrentUser().getSysUser().getSysUserId());
            if (user.getGoogleAuthEnabled() == null || user.getGoogleAuthEnabled() != CS.YES || user.getGoogleAuthSecret() == null) {
                throw new BizException("需先绑定谷歌验证后方可设置");
            }
        }
        MchInfo update = new MchInfo();
        update.setMchNo(getCurrentMchNo());
        update.setPaySecurityType(type);
        mchInfoService.updateById(update);
        return ApiRes.ok();
    }

    @Operation(summary = "设置/修改支付密码")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "newPwd", description = "新支付密码（明文）", required = true)
    })
    @PostMapping("/pay/password")
    public ApiRes updatePayPassword() {
        String newPwd = getValStringRequired("newPwd");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(newPwd);
        MchInfo update = new MchInfo();
        update.setMchNo(getCurrentMchNo());
        update.setPayPassword(encoded);
        mchInfoService.updateById(update);
        return ApiRes.ok();
    }

    @Operation(summary = "商户应用列表（密钥状态）")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @GetMapping("/apps")
    public ApiRes apps() {
        String mchNo = getCurrentMchNo();
        List<MchApp> list = mchAppService.list(MchApp.gw().eq(MchApp::getMchNo, mchNo));
        List<JSONObject> apps = new ArrayList<>();
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        boolean secretSet = mchInfo != null && StringUtils.isNotBlank(mchInfo.getMchSecret());
        for (MchApp app : list) {
            JSONObject o = new JSONObject();
            o.put("appId", app.getAppId());
            o.put("appName", app.getAppName());
            o.put("state", app.getState());
            o.put("secretSet", secretSet);
            apps.add(o);
        }
        return ApiRes.ok(apps);
    }

    @Operation(summary = "查看应用密钥")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID")
    })
    @PostMapping("/app/secret/view")
    public ApiRes viewSecret() {
        MchInfo mchInfo = mchInfoService.getById(getCurrentMchNo());
        return ApiRes.ok4newJson("appSecret", mchInfo == null ? null : mchInfo.getMchSecret());
    }

    @Operation(summary = "生成应用密钥（首次设置）")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID")
    })
    @PostMapping("/app/secret/generate")
    public ApiRes generateSecret() {
        MchInfo mchInfo = mchInfoService.getById(getCurrentMchNo());
        if (mchInfo != null && StringUtils.isNotBlank(mchInfo.getMchSecret())) {
            //throw new BizException("已设置密钥，无需生成");
        }
        String newSecret = UUID.randomUUID().toString().replace("-", "");
        MchInfo update = new MchInfo();
        update.setMchNo(getCurrentMchNo());
        update.setMchSecret(newSecret);
        mchInfoService.updateById(update);
        return ApiRes.ok4newJson("appSecret", newSecret);
    }

    @Operation(summary = "重置应用密钥（若未绑定谷歌可直接保存）")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "gaCode", description = "谷歌验证码")
    })
    @PostMapping("/app/secret/rotate")
    public ApiRes rotateSecret() {
        String gaCode = getValString("gaCode");
        String inputSecret = getValString("mchSecret");
        SysUser user = sysUserService.getById(getCurrentUser().getSysUser().getSysUserId());
        boolean googleBound = user.getGoogleAuthEnabled() != null && user.getGoogleAuthEnabled() == CS.YES && user.getGoogleAuthSecret() != null;
        if (googleBound) {
            if (org.apache.commons.lang3.StringUtils.isBlank(gaCode)) {
                throw new BizException("请输入谷歌验证码");
            }
            String secret = JeepayKit.aesDecode(user.getGoogleAuthSecret());
            boolean ok = TotpUtil.verifyCode(secret, gaCode, 6, 30, 1);
            if (!ok) {
                throw new BizException("谷歌验证码有误");
            }
        }
        String newSecret = StringUtils.isNotBlank(inputSecret) ? inputSecret : UUID.randomUUID().toString().replace("-", "");
        MchInfo update = new MchInfo();
        update.setMchNo(getCurrentMchNo());
        update.setMchSecret(newSecret);
        mchInfoService.updateById(update);
        return ApiRes.ok4newJson("appSecret", newSecret);
    }
}
