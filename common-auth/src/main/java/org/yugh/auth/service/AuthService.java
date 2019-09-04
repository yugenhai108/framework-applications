package org.yugh.auth.service;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.yugh.auth.common.constants.Constant;
import org.yugh.auth.config.AuthConfig;
import org.yugh.auth.pojo.dto.User;
import org.yugh.auth.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Main auth service
 * <p>
 * <p>
 * Servlet  --- > Reactive
 *
 * @author yugenhai
 */
@Slf4j
@Component
public class AuthService {

    @Autowired
    private AuthCookieUtils authCookieUtils;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private JwtHelper jwtHelper;


    private void doSomeThing(Map<String, Object> paramsMap, String appKey, String secret) {
        log.info("doSomeThing for map-> appKey-> secret");
    }

    private String doSomeThing(String identity, HashMap map, Map<String, Object> paramsMap) {
        return "do something!";
    }


    /**
     * token 转 对象
     *
     * @param request
     * @return
     */
    public User parseUserToJwt(HttpServletRequest request) {
        String token = this.getTokenByHeader(request);
        Assert.notNull(token, "parseUserToJwt token is null");
        Claims claims = jwtHelper.getAllClaimsFromToken(token);
        Map<String, Object> userMap = (Map<String, Object>) claims.get(StringPool.DATAWORKS_USER_INFO);
        try {
            User user = (User) BeanMapUtils.map2Object(userMap, User.class);
            return user;
        } catch (Exception e) {
            log.error("parseUserToJwt Exception :{}", e.getMessage());
        }
        return null;
    }


    /**
     * 验证token是否失效
     * <p>
     * 异常由业务使用时捕捉，会出现用非法token校验
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        return jwtHelper.validateToken(token);
    }


    /**
     * Get token By Gateway
     *
     * @param request
     * @return
     */
    @Deprecated
    public String getToken(HttpServletRequest request) {
        String token;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies).filter(cookie -> Constant.SESSION_TOKEN.equals(cookie.getName())).map(Cookie::getValue).findFirst().orElse(null);
        }
        if (!StringUtils.isEmpty(token = (String) request.getAttribute(Constant.SESSION_TOKEN))) {
            return token;
        }
        if (!StringUtils.isEmpty((token = request.getParameter(Constant.SESSION_TOKEN)))) {
            return token;
        }
        if (!StringUtils.isEmpty(request.getHeader(Constant.DATAWORKS_GATEWAY_HEADERS)) &&
                !StringUtils.isEmpty(token = request.getHeader(Constant.SESSION_TOKEN))) {
            return token;
        }
        if (!StringUtils.isEmpty((token = request.getHeader(Constant.SESSION_TOKEN)))) {
            WebUtils.setSession(request, StringPool.FEIGN, token);
            return token;
        }
        return null;
    }


    /**
     * Get Jwt
     *
     * @param request
     * @return
     */
    public String getTokenByHeader(HttpServletRequest request) {
        String userToken;
        if (!StringUtils.isEmpty(userToken = request.getHeader(StringPool.DATAWORKS_TOKEN))) {
            return userToken;
        }
        if (!StringUtils.isEmpty((userToken = (String) request.getAttribute(StringPool.DATAWORKS_TOKEN)))) {
            return userToken;
        }
        return null;
    }


    /**
     * HttpServletResponse Remove Cookies
     *
     * @param response
     */
    public void removeCookieByAspect(HttpServletResponse response) {
        this.authCookieUtils.removeCookie(response, authConfig.getGatewayCloud(), Constant.SESSION_TOKEN);
        this.authCookieUtils.removeCookie(response, authConfig.getGatewayApps(), Constant.SESSION_TOKEN);
        this.authCookieUtils.removeCookie(response, authConfig.getXxCorp(), Constant.SESSION_TOKEN);
        this.authCookieUtils.removeCookie(response, authConfig.getXxCloud(), Constant.SESSION_TOKEN);
        this.authCookieUtils.removeCookie(response, authConfig.getXxApps(), Constant.SESSION_TOKEN);
        this.authCookieUtils.removeCookie(response, authConfig.getXxCom(), Constant.SESSION_TOKEN);
    }

    /**
     * Reactive Remove Cookies
     *
     * @param response
     */
    private void removeCookieByGateway(ServerHttpResponse response) {
        this.authCookieUtils.removeCookieByReactive(response, Constant.SESSION_TOKEN, null, authConfig.getGatewayCloud());
        this.authCookieUtils.removeCookieByReactive(response, Constant.SESSION_TOKEN, null, authConfig.getGatewayApps());
        this.authCookieUtils.removeCookieByReactive(response, Constant.SESSION_TOKEN, null, authConfig.getXxApps());
        this.authCookieUtils.removeCookieByReactive(response, Constant.SESSION_TOKEN, null, authConfig.getXxCloud());
        this.authCookieUtils.removeCookieByReactive(response, Constant.SESSION_TOKEN, null, authConfig.getXxCorp());
        this.authCookieUtils.removeCookieByReactive(response, Constant.SESSION_TOKEN, null, authConfig.getXxCom());
    }


    /**
     * Check Env
     *
     * @param paramsMap
     * @param appKey
     * @param secret
     * @param identity
     * @return
     * @author yugenhai
     */
    private boolean logoutByTestOrProd(Map<String, Object> paramsMap, String appKey, String secret, String
            identity) {
        this.doSomeThing(paramsMap, appKey, secret);
        String resp = this.doSomeThing(identity, new HashMap(16), paramsMap);
        if (StringUtils.isEmpty(resp)) {
            throw new RuntimeException("SSO Logout Failed !!!");
        }
        Map respMap = JsonUtils.jsonToObject(resp, Map.class);
        Object status = respMap.get("status");
        if (status == null || (!status.toString().equalsIgnoreCase(StringPool.TRUE))) {
            Object msg = respMap.get("msg");
            log.error(msg == null ? "SSO Logout Failed !!!" : (String) msg);
            return false;
        }
        return true;
    }


    /**
     * Gateway Get token
     *
     * @param request
     * @return
     */
    public String getUserTokenByGateway(ServerHttpRequest request) {
        return this.authCookieUtils.getCookieByNameByReactive(request, Constant.SESSION_TOKEN);
    }


    /**
     * Gateway Login
     *
     * @param request
     * @return
     */
    public boolean isLoginByReactive(ServerHttpRequest request) {
        String token = authCookieUtils.getCookieByNameByReactive(request, Constant.SESSION_TOKEN);
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            User user = this.getUserByToken(token);
            if (StringUtils.isEmpty(user)) {
                return false;
            }
        } catch (Exception e) {
            log.error("获取SSO用户信息失败", e);
            return false;
        }
        return true;
    }


    /**
     * Gateway Get user
     *
     * @param request
     * @return
     */
    public User getUserByReactive(ServerHttpRequest request) {
        String token = authCookieUtils.getCookieByNameByReactive(request, Constant.SESSION_TOKEN);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {
            User user = this.getUserByToken(token);
            if (StringUtils.isEmpty(user)) {
                return null;
            }
            return user;
        } catch (Exception e) {
            log.error("获取SSO用户信息失败", e);
            return null;
        }
    }


    /**
     * Gateway logout User
     *
     * @param request
     * @param response
     */
    public void logoutByGateway(ServerHttpRequest request, ServerHttpResponse response) {
        String token = authCookieUtils.getCookieByNameByReactive(request, Constant.SESSION_TOKEN);
        if (StringUtils.isEmpty(token)) {
            return;
        } else {
            try {
                this.logoutByToken(token);
                this.removeCookieByGateway(response);
            } catch (Exception e) {
                log.error("注销失败 : {}", e);
                throw new RuntimeException("注销失败 : {}" + e.getMessage());
            }
        }
    }


    /**
     * Aspect Get token
     *
     * @param request
     * @return
     */
    public User getUserByAuthToken(HttpServletRequest request) {
        Cookie cookie = authCookieUtils.getCookieByName(request, Constant.SESSION_TOKEN);
        String token = cookie != null ? cookie.getValue() : null;
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {
            User user = this.getUserByToken(token);
            if (Objects.nonNull(user)) {
                return user;
            }
        } catch (Exception e) {
            log.error("获取SSO用户信息失败", e);
            return null;
        }
        return null;
    }


    /**
     * Check login
     *
     * @param request
     * @return
     */
    public boolean isLogin(HttpServletRequest request) {
        Cookie cookie = authCookieUtils.getCookieByName(request, Constant.SESSION_TOKEN);
        String token = cookie != null ? cookie.getValue() : null;
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        try {
            User user = this.getUserByToken(token);
            if (Objects.isNull(user)) {
                return false;
            }
        } catch (Exception e) {
            log.error("获取SSO用户信息失败", e);
            return false;
        }
        return true;
    }

    /**
     * Login by feign
     *
     * @param token
     * @return
     */
    public boolean isLoginByFeign(String token) {
        try {
            User user = this.getUserByToken(token);
            if (Objects.isNull(user)) {
                return false;
            }
        } catch (Exception e) {
            log.error(" isLoginByFeign : {} ", e);
            return false;
        }
        return true;
    }


    /**
     * GetUserByToken
     *
     * @param token
     * @return
     */
    public User getUserByToken(String token) {
        Map<String, Object> paramsMap = new HashMap(16);
        paramsMap.put("token", token);
        String env = authConfig.getEnvSwitch();
        Assert.hasText(env, "envSwitch is Empty");
        String resp;
        switch (env) {
            case StringPool
                    .TEST:
                this.doSomeThing(paramsMap, authConfig.getSsoTestAppKey(), authConfig.getSsoTestAppSecret());
                resp = this.doSomeThing(authConfig.getSsoTestIdentity(), new HashMap(16), paramsMap);
                return parseAsUser(resp);
            case StringPool
                    .PROD:
                this.doSomeThing(paramsMap, authConfig.getSsoProdAppKey(), authConfig.getSsoProdAppSecret());
                resp = this.doSomeThing(authConfig.getSsoProdIdentity(), new HashMap(16), paramsMap);
                return parseAsUser(resp);
            default:
                return null;
        }
    }


    /**
     * Logout By token
     *
     * @param token
     * @return
     * @throws Exception
     * @author yugenhai
     */
    public boolean logoutByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return true;
        } else {
            Map<String, Object> paramsMap = new HashMap(16);
            paramsMap.put("token", token);
            String env = authConfig.getEnvSwitch();
            switch (env) {
                case StringPool
                        .TEST:
                    boolean test = this.logoutByTestOrProd(paramsMap, authConfig.getSsoTestAppKey(), authConfig.getSsoTestAppSecret(), authConfig.getSsoTestIdentity());
                    if (test) {
                        return true;
                    }
                    return false;
                case StringPool
                        .PROD:
                    boolean prod = this.logoutByTestOrProd(paramsMap, authConfig.getSsoProdAppKey(), authConfig.getSsoProdAppSecret(), authConfig.getSsoProdIdentity());
                    if (prod) {
                        return true;
                    }
                default:
                    return false;
            }
        }
    }

    /**
     * 这是别人业务代码，只做参考
     * 解析SSO返回的用户信息
     *
     * @param userJSON
     * @return
     */
    private User parseAsUser(String userJSON) {
        Map respMap = JsonUtils.jsonToObject(userJSON, Map.class);
        Object status = respMap.get("status");
        if (status == null || (StringPool.FALSE).equalsIgnoreCase(status.toString())) {
            log.error("get user info error: " + userJSON);
            Object errorMsgObject = respMap.get("msg");
            String errorMsg = "get user info failed";
            if (errorMsgObject != null) {
                errorMsg = String.valueOf(errorMsgObject);
            }
            try {
                throw new RuntimeException(errorMsg);
            } catch (Exception e) {
                log.error("get user info error: " + userJSON);
                throw new RuntimeException(errorMsg);
            }
        }
        Map userInfo = (Map) respMap.get("userInfo");
        String userId = (String) userInfo.get("user_id");
        String email = (String) userInfo.get("email");
        String userNameEn = email.replace("@xx.com", "");
        String userNameCn = (String) userInfo.get("fullname");
        User user = User.builder().build();
        user.setNo(userId);
        user.setUserName(userNameEn);
        user.setAliasName(userNameCn);
        user.setEmail(email);
        return user;
    }
}
