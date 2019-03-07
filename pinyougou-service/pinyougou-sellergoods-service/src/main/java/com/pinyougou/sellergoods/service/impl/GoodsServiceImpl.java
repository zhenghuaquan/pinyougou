package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商品服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-25<p>
 */
@Service(interfaceName = "com.pinyougou.service.GoodsService")
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SellerMapper sellerMapper;

    // SqlSession
    @Override
    public void save(Goods goods) {
        try{
            // 1. 往tb_goods表插入数据(SPU表)
            // 设置商品审核状态(未审核)
            goods.setAuditStatus("0");
            goodsMapper.insertSelective(goods);

            // 2. 往tb_goods_desc表插入数据
            // 设置商品描述表的主键列
            goods.getGoodsDesc().setGoodsId(goods.getId());
            goodsDescMapper.insertSelective(goods.getGoodsDesc());

            // 3. 往tb_item表插入数据(SKU表)
            // 判断是否启用了规格
            if ("1".equals(goods.getIsEnableSpec())) {// 启用规格

                for (Item item : goods.getItems()) {
                    // item: {spec : {}, price : 0, num : 0, status : '0', isDefault : '0'}
                    // 设置商品标题: SPU商品名称 + spec : {"网络":"联通4G","机身内存":"128G"}
                    // Apple iPhone XS Max (A2104) 512GB 深空灰色 移动联通电信4G手机 双卡双待
                    StringBuilder title = new StringBuilder(goods.getGoodsName());
                    // 把spec : {"网络":"联通4G","机身内存":"128G"} 转化成Map集合
                    Map specMap = JSON.parseObject(item.getSpec());
                    for (Object value : specMap.values()) {
                        title.append(" " + value);
                    }
                    item.setTitle(title.toString());

                    setItemInfo(goods, item);

                    itemMapper.insertSelective(item);
                }
            }else{ // 没有启用规格
                // SPU就是SKU 只需要往tb_item插入一行数据
                Item item = new Item();
                //  {spec : {}, price : 0, num : 0, status : '0', isDefault : '0'}
                // 设置商品标题
                item.setTitle(goods.getGoodsName());
                // 设置商品规格
                item.setSpec("{}");
                // 设置商品价格
                item.setPrice(goods.getPrice());
                // 设置商品库存数量
                item.setNum(100);
                // 设置商品状态
                item.setStatus("1");
                // 设置商品是否为默认的
                item.setIsDefault("1");

                setItemInfo(goods, item);

                itemMapper.insertSelective(item);
            }

        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }
    // 判断服务接口实现类中的方法是否出现异常，如果出现异常就会调用sqlSession.rollback()
    // 如果没有出现异常就提交事务 sqlSession.commit();


    /** 设置SKU商品的其它信息 */
    private void setItemInfo(Goods goods, Item item){
        // 设置商品图片(商品描述表 itemImages)
        // [{"color":"金色","url":"http://image.pinyougou.com/jd/wKgMg1qtKEOATL9nAAFti6upbx4132.jpg"},
        // {"color":"深空灰色","url":"http://image.pinyougou.com/jd/wKgMg1qtKHmAFxj7AAFZsBqChgk725.jpg"}]
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList != null && imageList.size() > 0) {
            // 取第一张图片
            item.setImage(imageList.get(0).get("url").toString());
        }
        // 设置商品的分类(三级分类)
        item.setCategoryid(goods.getCategory3Id());
        // 设置商品的创建时间
        item.setCreateTime(new Date());
        // 设置商品的修改时间
        item.setUpdateTime(item.getCreateTime());
        // 设置商品关联的SPU的id
        item.setGoodsId(goods.getId());
        // 设置商品的商家id
        item.setSellerId(goods.getSellerId());

        // 设置商品的三级分类名称(搜索商品)
        ItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
        item.setCategory(itemCat != null ? itemCat.getName() : "");
        // 设置商品的品牌名称(搜索商品)
        Brand brand = brandMapper.selectByPrimaryKey(goods.getBrandId());
        item.setBrand(brand != null ? brand.getName() : "");
        // 设置商品的店铺名称(搜索商品)
        Seller seller = sellerMapper.selectByPrimaryKey(goods.getSellerId());
        item.setSeller(seller != null ? seller.getNickName() : "");
    }

    @Override
    public void update(Goods goods) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Goods findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Goods> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Goods goods, int page, int rows) {
        try{
            // 商品ID  商品名称	商品价格	一级分类	二级分类	三级分类	状态 (Map)
            // 开始分页
            PageInfo<Map> pageInfo = PageHelper.startPage(page, rows)
                    .doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    goodsMapper.findAll(goods);
                }
            });
            // 获取分页数据
            List<Map> goodsList = pageInfo.getList();
            for (Map map : goodsList) {
                // 获取三级分类id
                Object category3Id = map.get("category3Id");
                if (category3Id != null) {

                    // 查询一级分类名称
                    long category1Id = Long.valueOf(map.get("category1Id").toString());
                    ItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(category1Id);
                    map.put("category1Name", itemCat1.getName());

                    // 查询二级分类名称
                    long category2Id = Long.valueOf(map.get("category2Id").toString());
                    ItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(category2Id);
                    map.put("category2Name", itemCat2.getName());

                    // 查询三级分类名称
                    ItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(Long.valueOf(category3Id.toString()));
                    map.put("category3Name", itemCat3.getName());
                }
            }

            return new PageResult(pageInfo.getTotal(), goodsList);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 修改商品的状态 */
    public  void updateStatus(String columnName, Long[] ids, String status){
        try{
            // UPDATE `tb_goods` SET audit_status = ? WHERE id IN(?,?,?)
            goodsMapper.updateStatus(columnName, ids, status);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
