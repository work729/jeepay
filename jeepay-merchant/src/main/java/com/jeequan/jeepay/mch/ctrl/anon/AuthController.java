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
package com.jeequan.jeepay.mch.ctrl.anon;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.service.AuthService;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.service.impl.MchInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录鉴权
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/anon/auth")
public class AuthController extends CommonCtrl {

	@Autowired private AuthService authService;
	@Autowired private MchInfoService mchInfoService;

	/** 用户信息认证 获取iToken  **/
	@Operation(summary = "登录认证")
	@Parameters({
			@Parameter(name = "ia", description = "用户名 i account, 需要base64处理", required = true),
			@Parameter(name = "ip", description = "密码 i passport,  需要base64处理", required = true),
			@Parameter(name = "vc", description = "证码 vercode,  需要base64处理", required = true),
			@Parameter(name = "vt", description = "验证码token, vercode token ,  需要base64处理", required = true)
	})
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	@MethodLog(remark = "登录认证")
	public ApiRes validate() throws BizException {

		String account = Base64.decodeStr(getValStringRequired("ia"));  //用户名 i account, 已做base64处理
		String ipassport = Base64.decodeStr(getValStringRequired("ip"));	//密码 i passport,  已做base64处理
        String vercode = Base64.decodeStr(getValStringRequired("vc"));	//验证码 vercode,  已做base64处理
        String vercodeToken = Base64.decodeStr(getValStringRequired("vt"));	//验证码token, vercode token ,  已做base64处理

        String cacheCode = RedisUtil.getString(CS.getCacheKeyImgCode(vercodeToken));
        if(StringUtils.isEmpty(cacheCode) || !cacheCode.equalsIgnoreCase(vercode)){
            throw new BizException("验证码有误！");
        }

		JeeUserDetails details = authService.preAuth(account, ipassport);
		MchInfo mchInfo = mchInfoService.getById(details.getSysUser().getBelongInfoId());
		if (mchInfo != null) {
			String ip = getIp();
			if (mchInfo.getLoginIpWhitelist() != null && !mchInfo.getLoginIpWhitelist().isEmpty()) {
				if (!matchIp(ip, mchInfo.getLoginIpWhitelist())) {
					throw new BizException("当前IP未在登录白名单内");
				}
			}
			if (mchInfo.getLoginIpBlacklist() != null && !mchInfo.getLoginIpBlacklist().isEmpty()) {
				if (matchIp(ip, mchInfo.getLoginIpBlacklist())) {
					throw new BizException("当前IP已在登录黑名单内");
				}
			}
		}
		boolean needGoogle = (details.getSysUser().getGoogleAuthEnabled() != null && details.getSysUser().getGoogleAuthEnabled() == CS.YES)
				|| (mchInfo != null && mchInfo.getLoginSecurityType() != null && mchInfo.getLoginSecurityType() == 1);
		if (needGoogle) {
			if (details.getSysUser().getGoogleAuthEnabled() == null || details.getSysUser().getGoogleAuthEnabled() != CS.YES) {
				throw new BizException("当前商户要求谷歌验证，请先在个人中心绑定谷歌验证");
			}
			String pendingToken = UUID.fastUUID().toString();
			RedisUtil.set(CS.getCacheKeyGoogleLogin(pendingToken), details, CS.GOOGLE_LOGIN_CACHE_TIME);
			RedisUtil.del(CS.getCacheKeyImgCode(vercodeToken));
			JSONObject result = new JSONObject();
			result.put("googleRequired", true);
			result.put("pendingToken", pendingToken);
			return ApiRes.ok(result);
		}
		String accessToken = authService.issueToken(details);

        // 删除图形验证码缓存数据
        RedisUtil.del(CS.getCacheKeyImgCode(vercodeToken));

		return ApiRes.ok4newJson(CS.ACCESS_TOKEN_NAME, accessToken);
	}

	/** 图片验证码  **/
	@Operation(summary = "图片验证码")
	@RequestMapping(value = "/vercode", method = RequestMethod.GET)
	public ApiRes vercode() throws BizException {

		//定义图形验证码的长和宽 // 4位验证码
		LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(137, 40, 4, 80);
        lineCaptcha.createCode(); //生成code

        //redis
		String vercodeToken = UUID.fastUUID().toString();
        RedisUtil.setString(CS.getCacheKeyImgCode(vercodeToken), lineCaptcha.getCode(), CS.VERCODE_CACHE_TIME ); //图片验证码缓存时间: 1分钟

        JSONObject result = new JSONObject();
        result.put("imageBase64Data", lineCaptcha.getImageBase64Data());
        result.put("vercodeToken", vercodeToken);
		result.put("expireTime", CS.VERCODE_CACHE_TIME);

		return ApiRes.ok(result);
	}

	@Operation(summary = "谷歌验证码二次校验后登录")
	@Parameters({
			@Parameter(name = "pt", description = "pendingToken", required = true),
			@Parameter(name = "gc", description = "谷歌验证码，Base64", required = true)
	})
	@RequestMapping(value = "/google/validate", method = RequestMethod.POST)
	@MethodLog(remark = "谷歌二次认证")
	public ApiRes googleValidate() throws BizException {
		String pendingToken = getValStringRequired("pt");
		String googleCode = Base64.decodeStr(getValStringRequired("gc"));
		JeeUserDetails details = RedisUtil.getObject(CS.getCacheKeyGoogleLogin(pendingToken), JeeUserDetails.class);
		if (details == null) {
			throw new BizException("会话已失效，请重新登录！");
		}
		SysUser user = details.getSysUser();
		if (user.getGoogleAuthEnabled() == null || user.getGoogleAuthEnabled() != CS.YES || user.getGoogleAuthSecret() == null) {
			throw new BizException("未开启谷歌验证！");
		}
		String secret = com.jeequan.jeepay.core.utils.JeepayKit.aesDecode(user.getGoogleAuthSecret());
		boolean ok = com.jeequan.jeepay.core.utils.TotpUtil.verifyCode(secret, googleCode, 6, 30, 1);
		if (!ok) {
			throw new BizException("谷歌验证码有误！");
		}
		String accessToken = authService.issueToken(details);
		RedisUtil.del(CS.getCacheKeyGoogleLogin(pendingToken));
		return ApiRes.ok4newJson(CS.ACCESS_TOKEN_NAME, accessToken);
	}

	private boolean matchIp(String ip, String list) {
		String[] arr = list.split(",");
		for (String s : arr) {
			if (ip.equals(s.trim())) {
				return true;
			}
		}
		return false;
	}
	/** 根据商户号生成登录token（运营平台单点登录使用） **/
	@Operation(summary = "根据商户号生成登录token（运营平台单点登录使用）")
	@Parameters({
			@Parameter(name = "mchNo", description = "商户号", required = true)
	})
	@RequestMapping(value = "/ssoLoginMch", method = RequestMethod.POST)
	public ApiRes ssoLoginMch() throws BizException {

		String mchNo = getValStringRequired("mchNo");

		String accessToken = authService.authByMchNo(mchNo);

		return ApiRes.ok4newJson(CS.ACCESS_TOKEN_NAME, accessToken);
	}

}
