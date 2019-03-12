package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;

/**
 * 商品控制器(SPU标准商品)
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-25<p>
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination solrQueue;
    @Autowired
    private Destination solrDeleteQueue;
    @Autowired
    private Destination pageTopic;

    /** 添加商品 */
    @PostMapping("/save")
    public boolean save(@RequestBody Goods goods){
        try{
            // 获取当前登录用户名
            SecurityContext securityContext = SecurityContextHolder.getContext();
            String sellerId = securityContext.getAuthentication().getName();
            // 设置商家id
            goods.setSellerId(sellerId);
            goodsService.save(goods);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 多条件分页查询商品 */
    @GetMapping("/findByPage")
    public PageResult findByPage(Goods goods, Integer page, Integer rows){
        /** 获取登录商家名称 */
        String sellerId = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        /** 添加查询条件 */
        goods.setSellerId(sellerId);
        /** GET请求中文转码 */
        if (StringUtils.isNoneBlank(goods.getGoodsName())) {
            try {
                goods.setGoodsName(new String(goods
                        .getGoodsName().getBytes("ISO8859-1"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /** 调用服务层方法查询 */
        return goodsService.findByPage(goods, page, rows);
    }

    /** 商品上下架 */
    @GetMapping("/updateMarketable")
    public boolean updateMarketable(Long[] ids, String status){
        try{
            goodsService.updateStatus("is_marketable", ids, status);
            // 判断商品上下架状态
            if ("1".equals(status)) {
                // 发送消息，生成商品索引
                jmsTemplate.send(solrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        ObjectMessage message = session.createObjectMessage(ids);
                        return message;
                    }
                });

                /** 发送消息生成静态网页 */
                for (Long goodsId : ids) {
                    jmsTemplate.send(pageTopic, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(goodsId.toString());
                        }
                    });
                }
            } else {
                // 商品下架
                jmsTemplate.send(solrDeleteQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

}
