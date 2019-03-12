package com.pinyougou.search.listener;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.SolrItem;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.*;

public class ItemMessageListener implements SessionAwareMessageListener<ObjectMessage>{

    @Reference(timeout = 30000)
    private GoodsService goodsService;
    @Reference(timeout = 30000)
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(ObjectMessage objectMessage, Session session) throws JMSException {
        System.out.println("ItemMessageListener监听到消息");
        Long[] ids = (Long[]) objectMessage.getObject();
        System.out.println("ids : " + Arrays.toString(ids));
        // 查询商家的SKU商品数据
        List<Item> itemList = goodsService.findItemByGoodsId(ids);
        if (itemList.size() > 0) {
            // 把List<Item> 转化成List<SolrItem>
            List<SolrItem> solrItemList = new ArrayList<>();
            for (Item item : itemList) {
                SolrItem solrItem = new SolrItem();
                solrItem.setId(item.getId());
                solrItem.setTitle(item.getTitle());
                solrItem.setPrice(item.getPrice());
                solrItem.setImage(item.getImage());
                solrItem.setGoodsId(item.getGoodsId());
                solrItem.setCategory(item.getCategory());
                solrItem.setBrand(item.getBrand());
                solrItem.setSeller(item.getSeller());
                solrItem.setUpdateTime(item.getUpdateTime());
                solrItem.setSpecMap(JSON.parseObject(item.getSpec(),Map.class));
                solrItemList.add(solrItem);
            }
            // 把SKU商品数据同步到索引库
            itemSearchService.saveOrUpdate(solrItemList);
        }
    }
}
