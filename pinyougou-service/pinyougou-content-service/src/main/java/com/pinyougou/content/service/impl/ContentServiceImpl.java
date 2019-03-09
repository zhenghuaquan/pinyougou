package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.Content;
import com.pinyougou.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
/**
 * ContentServiceImpl 服务接口实现类
 * @date 2019-02-28 17:06:03
 * @version 1.0
 */
@Service(interfaceName = "com.pinyougou.service.ContentService")
@Transactional
public class ContentServiceImpl implements ContentService {

	@Autowired
	private ContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/** 添加方法 */
	@Override
	public void save(Content content){
		try {
			contentMapper.insertSelective(content);
			redisTemplate.delete("content");
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/** 修改方法 */
	@Override
	public void update(Content content){
		try {
			contentMapper.updateByPrimaryKeySelective(content);
            redisTemplate.delete("content");
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/** 根据主键id删除 */
	@Override
	public void delete(Serializable id){
		try {
			contentMapper.deleteByPrimaryKey(id);
            redisTemplate.delete("content");
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/** 批量删除 */
	@Override
	public void deleteAll(Serializable[] ids){
		try {
			// 创建示范对象
			Example example = new Example(Content.class);
			// 创建条件对象
			Example.Criteria criteria = example.createCriteria();
			// 创建In条件
			criteria.andIn("id", Arrays.asList(ids));
			// 根据示范对象删除
			contentMapper.deleteByExample(example);
            redisTemplate.delete("content");
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/** 根据主键id查询 */
	@Override
	public Content findOne(Serializable id){
		try {
			return contentMapper.selectByPrimaryKey(id);
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/** 查询全部 */
	@Override
	public List<Content> findAll(){
		try {
			return contentMapper.selectAll();
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/** 多条件分页查询 */
	@Override
	public PageResult findByPage(Content content, int page, int rows){
		try {
			PageInfo<Content> pageInfo = PageHelper.startPage(page, rows)
				.doSelectPageInfo(new ISelect() {
					@Override
					public void doSelect() {
						contentMapper.selectAll();
					}
				});
			return new PageResult(pageInfo.getTotal(), pageInfo.getList());
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	/** 根据分类id查询广告内容 */

	@Override
	public List<Content> findContentByCategoryId(Long categoryId) {

		System.out.println("RedisTemplate :" + redisTemplate);
		/** 定义广告数据 */
		List<Content> contentList = null;
		try {
			/** 从Redis中获取广告 */
			contentList = (List<Content>) redisTemplate.boundValueOps("content").get();
			if (contentList != null && contentList.size() > 0) {
				System.out.println("从Redis中读取缓存的数据");
                return contentList;
            }
		}catch (Exception e){
			throw new RuntimeException(e);
		}

		try {
			// 创建示范对象
			Example example = new Example(Content.class);
			// 创建查询条件对象
            Example.Criteria criteria = example.createCriteria();
            // 添加等于条件category_id = id;
            criteria.andEqualTo("categoryId",categoryId);
            // 添加等于条件status=1
            criteria.andEqualTo("status","1");
            // 排序(升序)order by sort_order asc
            example.orderBy("sortOrder").asc();

            /** 查询广告数据 */
            contentList = contentMapper.selectByExample(example);

            try {
                redisTemplate.boundValueOps("content").set(contentList);
            }catch (Exception e){
            }
            return contentList;
        }catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}
}