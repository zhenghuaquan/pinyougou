package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-22<p>
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference(timeout = 10000)
    private SellerService sellerService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // proxy1@18c2605
    /** 商家申请入驻 */
    @PostMapping("/save")
    public boolean save(@RequestBody Seller seller){
        System.out.println("sellerService:" + sellerService);
        try{

            // 密码加密
            String password = passwordEncoder.encode(seller.getPassword());
            seller.setPassword(password);

            sellerService.save(seller);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }


}
