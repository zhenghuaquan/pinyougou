package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-22<p>
 */
@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    /** 登录方法 */
    @RequestMapping("/user/login")
    public String login(HttpServletRequest request,
                        String username, String password, String code){
        // 判断请求方式
        if ("POST".equalsIgnoreCase(request.getMethod())){
            System.out.println(username + "====" + password + "===" + code);
            // 1. 先判断验证码
            String oldCode = (String)request.getSession().getAttribute(VerifyController.VERIFY_CODE);
            System.out.println("oldCode: " + oldCode);
            if (oldCode.equalsIgnoreCase(code)){
                // 2. 登录验证(由SpringSecurity完成)
                UsernamePasswordAuthenticationToken token =
                        new UsernamePasswordAuthenticationToken(username, password);
                try {
                    // 3. 认证用户，返回认证对象
                    Authentication authenticate = authenticationManager.authenticate(token);
                    // 4. 判断是否登录成功
                    if (authenticate.isAuthenticated()) { // true
                        // 5. 获取安全上下文，设置认证对象
                        SecurityContext securityContext = SecurityContextHolder.getContext();
                        securityContext.setAuthentication(authenticate);

                        return "redirect:/admin/index.html";
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        return "redirect:/login.html";
    }

    /** 获取登录用户名 */
    @GetMapping("/showLoginName")
    @ResponseBody
    public Map<String,String> showLoginName(){
        // 获取安全上下文对象
        SecurityContext securityContext = SecurityContextHolder.getContext();
        // 获取当前登录用户名
        String loginName = securityContext.getAuthentication().getName();

        Map<String, String> data = new HashMap<>();
        data.put("loginName", loginName);
        return data;
    }
}
