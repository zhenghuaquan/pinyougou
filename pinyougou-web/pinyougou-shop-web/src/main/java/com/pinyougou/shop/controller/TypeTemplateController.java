package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 类型模板控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-25<p>
 */
@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference(timeout = 10000)
    private TypeTemplateService typeTemplateService;

    /** 根据主键id查询类型模板对象 */
    @GetMapping("/findOne")
    public TypeTemplate findOne(Long id){
        return typeTemplateService.findOne(id);
    }

    /** 根据模板id查询规格与规格选项 */
    @GetMapping("/findSpecByTemplateId")
    public List<Map> findSpecByTemplateId(Long id){
        return typeTemplateService.findSpecByTemplateId(id);
    }
}
