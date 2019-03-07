package com.pinyougou.shop.service;

import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户认证服务类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-22<p>
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("username:" + username);

        System.out.println("sellerService: " + sellerService);
        // 调用服务接口查询商家对象
        Seller seller = sellerService.findOne(username);
        // 判断seller对象是否为空
        if (seller != null && "1".equals(seller.getStatus())){ // 用户名正确

            // 定义List集合封装角色
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            return new User(username, seller.getPassword(), authorities);
        }
        return null;
    }

    /** Spring的setter注入 */
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
}
