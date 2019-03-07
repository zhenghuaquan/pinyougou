package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 类型模板服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-21<p>
 */
@Service(interfaceName = "com.pinyougou.service.TypeTemplateService")
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Autowired
    private TypeTemplateMapper typeTemplateMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public void save(TypeTemplate typeTemplate) {
        try{
            // 判断类型模板对象中的属性是否有值，有值的话就生成到insert语句中
            // insert into 表 (name)
            typeTemplateMapper.insertSelective(typeTemplate);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        try{
            // 判断类型模板对象中的属性是否有值，有值的话就生成到update语句中
            // update 表 set name = ? where id = ?
            typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {
        try{
            // DELETE FROM tb_type_template WHERE ( id in ( ? , ? ) )
            // 创建示例对象
            Example example = new Example(TypeTemplate.class); // delete from tb_type_template
            // 创建条件对象
            Example.Criteria criteria = example.createCriteria();
            // 添加条件 id in (?,?,?)
            criteria.andIn("id", Arrays.asList(ids));
            // 条件删除
            typeTemplateMapper.deleteByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TypeTemplate findOne(Serializable id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<TypeTemplate> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(TypeTemplate typeTemplate, int page, int rows) {
        try{
            // 开始分页
            PageInfo<Object> pageInfo = PageHelper.startPage(page, rows)
                    .doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    typeTemplateMapper.findAll(typeTemplate);
                }
            });
            return new PageResult(pageInfo.getTotal(), pageInfo.getList());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据模板id查询规格与规格选项 */
    public List<Map> findSpecByTemplateId(Long id){
        try{
            /**
             * [{"id":27,"text":"网络", options : [{},{}]},
             * {"id":32,"text":"机身内存",  options : [{},{}]}]
             * List<Map<String, Object>>
             */
            // 1. 获取类型模板中的一行数据得到  spec_ids列
            TypeTemplate typeTemplate = findOne(id);
            // [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            // specIds 是json数组格式的字符串
            String specIds = typeTemplate.getSpecIds();
            // 把specIds 转化成 List<Map> JSON处理框架 fastjson框架 JSON工具类
            // JSON.parseObject();  {}
            // JSON.parseArray();  []
            List<Map> specList = JSON.parseArray(specIds, Map.class);
            for (Map map : specList) {

                // 获取规格id
                Long specId = Long.valueOf(map.get("id").toString());

                // SELECT  * FROM `tb_specification_option` WHERE spec_id = 32
                SpecificationOption so = new SpecificationOption();
                so.setSpecId(specId); //  spec_id = 32
                List<SpecificationOption> options = specificationOptionMapper.select(so);

                map.put("options", options);
            }

            return specList;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
