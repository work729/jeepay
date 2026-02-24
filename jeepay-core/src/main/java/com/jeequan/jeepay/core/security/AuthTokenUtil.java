package com.jeequan.jeepay.core.security;

import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.jwt.JWTPayload;
import com.jeequan.jeepay.core.jwt.JWTUtils;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class AuthTokenUtil {

  private AuthTokenUtil() {}

  public static JeeUserDetails resolveUserByToken(HttpServletRequest request, String jwtSecret) {
    String authToken = request.getHeader(CS.ACCESS_TOKEN_NAME);
    if (StringUtils.isEmpty(authToken)) {
      authToken = request.getParameter(CS.ACCESS_TOKEN_NAME);
    }
    if (StringUtils.isEmpty(authToken)) {
      return null;
    }

    JWTPayload jwtPayload = JWTUtils.parseToken(authToken, jwtSecret);
    if (jwtPayload == null || StringUtils.isEmpty(jwtPayload.getCacheKey())) {
      return null;
    }

    JeeUserDetails jwtBaseUser = RedisUtil.getObject(jwtPayload.getCacheKey(), JeeUserDetails.class);
    if (jwtBaseUser == null) {
      RedisUtil.del(jwtPayload.getCacheKey());
      return null;
    }

    RedisUtil.expire(jwtPayload.getCacheKey(), CS.TOKEN_TIME);
    return jwtBaseUser;
  }
}
