package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Brand;
import com.pinyougou.service.BrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 品牌控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-16<p>
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    /**
     * 配置引用服务接口代理对象
     * timeout: 连接超时 1000毫秒
     * */
    @Reference(timeout = 10000)
    private BrandService brandService;

    /** 查询全部品牌 */
    @GetMapping("/findByPage")
    public PageResult findByPage(Brand brand, Integer page, Integer rows){
        // {total : 100, rows : [{},{}]}
        try {
            // GET请求中文转码
            if (brand != null && StringUtils.isNoneBlank(brand.getName())) {
                // 品牌名称
                brand.setName(new String(brand.getName().getBytes("ISO-8859-1"), "UTF-8"));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return brandService.findByPage(brand, page, rows);
    }

    /** 添加品牌 */
    @PostMapping("/save")
    public boolean save(@RequestBody Brand brand){
        try{
            brandService.save(brand);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 修改品牌 */
    @PostMapping("/update")
    public boolean update(@RequestBody Brand brand){
        try{
            brandService.update(brand);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 删除品牌 */
    @GetMapping("/delete")
    public boolean delete(Long[] ids){
        try{
            // delete from tb_brand where id in (?,?,?)
            brandService.deleteAll(ids);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 查询品牌列表 */
    @GetMapping("/findBrandList")
    public List<Map<String, Object>> findBrandList(){
        // [{id : 1, text : '小米'},{id : 2, text : '华为'},{id : 3, text : '苹果'}]
        return brandService.findAllByIdAndName();
    }
}
