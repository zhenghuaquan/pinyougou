package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.SolrItem;
import com.pinyougou.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.Map;

@Service(interfaceName = "com.pinyougou.service.ItemSearchService")
public class ItemSearchServiceImpl implements ItemSearchService{

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> params) {
        Map<String,Object> data = new HashMap<>();
        /** 获取当前页码 */
        Integer page = (Integer) params.get("page");
        if (page == null) {
            page = 1;
        }
        /** 获取每页显示的记录数 */
        Integer rows = (Integer) params.get("rows");
        if (rows == null) {
            rows = 20;
        }
        try {
            /** 获取检索关键字 */
            String keywords = (String) params.get("keywords");
            /** 判断检索关键字 */
            if (StringUtils.isNoneBlank(keywords)) { // 不为空
                // 创建高亮查询对象
                HighlightQuery highlightQuery = new SimpleHighlightQuery();
                // 创建高亮选项对象
                HighlightOptions highlightOptions = new HighlightOptions();
                // 设置高亮域
                highlightOptions.addField("title");
                // 设置高亮前缀
                highlightOptions.setSimplePrefix("<font color='red'>");
                // 设置高亮后缀
                highlightOptions.setSimplePostfix("</font>");
                // 设置高亮选项
                highlightQuery.setHighlightOptions(highlightOptions);
                /** 创建查询条件 */
                Criteria criteria = new Criteria("keywords").is(keywords);
                /** 添加查询条件 */
                highlightQuery.addCriteria(criteria);

                /** 按商品分类过滤 */
                if (!"".equals(params.get("category"))) {
                    Criteria criteria1 = new Criteria("category").is(params.get("category"));
                    highlightQuery.addFilterQuery(new SimpleFilterQuery(criteria1));
                }

                /** 按品牌过滤 */
                if (!"".equals(params.get("brand"))) {
                    Criteria criteria1 = new Criteria("brand").is(params.get("brand"));
                    highlightQuery.addFilterQuery(new SimpleFilterQuery(criteria1));
                }

                /** 按价格过滤 */
                if (!"".equals(params.get("price"))) {
                    /** 500-1000 */
                    String[] prices = params.get("price").toString().split("-");
                    /** 如果价格区间起点不等于0 */
                    if (!prices[0].equals("0")) {
                        Criteria criteria1 = new Criteria("price").greaterThanEqual(prices[0]);
                        highlightQuery.addFilterQuery(new SimpleFilterQuery(criteria1));
                    }

                    if (!prices[1].equals("*")) {
                        Criteria criteria1 = new Criteria("price").lessThanEqual(prices[1]);
                        highlightQuery.addFilterQuery(new SimpleFilterQuery(criteria1));
                    }
                }

                /** 按规格过滤 */
                if (params.get("spec") != null) {
                    Map<String,String> specMap = (Map<String, String>) params.get("spec");
                    for (String key : specMap.keySet()) {
                        Criteria criteria1 = new Criteria("spec_" + key).is(specMap.get(key));
                        highlightQuery.addFilterQuery(new SimpleFilterQuery(criteria1));
                    }
                }

                /** 添加排序 */
                String sortValue = (String) params.get("sort");
                String sortField = (String) params.get("sortField");
                if (StringUtils.isNoneBlank(sortValue) && StringUtils.isNoneBlank(sortField)) {
                    Sort sort = new Sort("ASC".equalsIgnoreCase(sortValue) ?
                            Sort.Direction.ASC :Sort.Direction.DESC,sortField);
                    /** 增加排序 */
                    highlightQuery.addSort(sort);
                }

                /** 设置起始记录数 */
                highlightQuery.setOffset((page - 1) * rows);
                /** 设置每页显示记录数 */
                highlightQuery.setRows(rows);

                /** 分页检索 */
                HighlightPage<SolrItem> highlightPage = solrTemplate.queryForHighlightPage(highlightQuery, SolrItem.class);
                /** 循环高亮选项集合 */
                for (HighlightEntry<SolrItem> he : highlightPage.getHighlighted()) {
                    // 获取检索到的原实体
                    SolrItem solrItem = he.getEntity();
                    // 判断高亮集合及集合中第一个Filed的高亮内容
                    if (he.getHighlights().size() > 0
                            && he.getHighlights().get(0).getSnipplets().size() > 0) {
                        // 设置高亮的结果
                        solrItem.setTitle(he.getHighlights().get(0).getSnipplets().get(0));
                    }
                }
                data.put("rows", highlightPage.getContent());
                /** 设置总页数 */
                data.put("totalPages",highlightPage.getTotalPages());
                /** 设置总记录数 */
                data.put("total", highlightPage.getTotalElements());
            } else {
                // 创建简单查询对象
                Query query = new SimpleQuery("*:*");
                /** 设置起始记录数 */
                query.setOffset((page - 1) * rows);
                /** 设置每页显示记录数 */
                query.setRows(rows);
                ScoredPage<SolrItem> scoredPage = solrTemplate.queryForPage(query, SolrItem.class);
                /** 获取内容 */
                data.put("rows", scoredPage.getContent());
                /** 设置总页数 */
                data.put("totalPages",scoredPage.getTotalPages());
                /** 设置总记录数 */
                data.put("total", scoredPage.getTotalElements());
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
