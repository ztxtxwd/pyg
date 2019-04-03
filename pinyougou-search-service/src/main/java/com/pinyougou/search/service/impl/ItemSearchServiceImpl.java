package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	/**
	 * 第九天搜索请参看该代码实现
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map search1(Map searchMap) {
		Map map=new HashMap<>();
		
		Query query=new SimpleQuery("*:*");
		//在指定域的时候：因为前台客户搜索输入的不确定是具体哪个域的,因此我们可以用到我们之前配置好的复制域
		/**
		 * 以下是我们当时配置的域：item_keywords是复制域,且type="text_ik"代表是可以分词的
			<field name="item_keywords"    type="text_ik" indexed="true" stored="false" multiValued="true"/>
			<copyField source="item_title" 		dest="item_keywords"/> 
			<copyField source="item_category"   dest="item_keywords"/>
			<copyField source="item_seller"     dest="item_keywords"/>
			<copyField source="item_brand"      dest="item_keywords"/>
		 */
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords")); // item_keywords  = 
		query.addCriteria(criteria);
		
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		
		map.put("rows", page.getContent());//返回查询到的数据
		return map;
	}
	/**
	 * 搜索：带高亮显示
	 * 第十天:第二个视频=====高亮显示的后台参考该代码
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map search2(Map searchMap) {
		Map map=new HashMap<>();
		//创建高亮显示查询条件
		HighlightQuery query = new SimpleHighlightQuery();
		//通过高亮选项添加高亮显示的域
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		//通过高亮选项设置高亮前缀
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		//通过高亮选项设置高亮后缀
		highlightOptions.setSimplePostfix("</em>");
		//在查询条件中设置高亮选项
		query.setHighlightOptions(highlightOptions );
		
		//用户页面在搜索框中输入了关键字,创建条件指定item_keywords域及从前台传过来的值     并将该条件添加到查询条件中
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query , TbItem.class);
		//高亮入口集合(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();		
		for(HighlightEntry<TbItem> entry:entryList  ){
			//获取每条记录的高亮列表======为什么是集合?因为需要高亮显示的域可能不止一个
			List<Highlight> highlightList = entry.getHighlights();
			/*
			for(Highlight h:highlightList){
				List<String> sns = h.getSnipplets();//每个域有可能存储多值
				System.out.println(sns);				
			}*/			
			if(highlightList.size()>0 &&  highlightList.get(0).getSnipplets().size()>0 ){
				TbItem item = entry.getEntity(); //可以得到原始的数据
				item.setTitle(highlightList.get(0).getSnipplets().get(0));			
			}			
		}
		map.put("rows", page.getContent());
		return map;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 搜索：视频4-视频13的搜索参考该代码
	 * 前台没构建搜索条件之前请参考这个
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map search3(Map searchMap) {
		Map map=new HashMap<>();
		//1.查询列表
		map.putAll(searchList(searchMap));
		//2.分组查询 商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);//["手机","平板电视"]
		//3.查询品牌和规格列表
		if(categoryList.size()>0){			
			map.putAll(searchBrandAndSpecList(categoryList.get(0)));
		}	
		return map;
	}
	/**
	 * 搜索:前台构建搜索条件之后最终的代码参看这个
	 */
	@SuppressWarnings("all")
	@Override
	public Map search(Map searchMap) {
		Map map=new HashMap<>();
		//空格处理
		String keywords= (String)searchMap.get("keywords");
		System.out.println("接收到的关键字:"+keywords);
		searchMap.put("keywords", keywords.replace(" ", ""));//关键字去掉空格 
		//1.查询列表
		map.putAll(searchList(searchMap));
		//2.分组查询 商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		//3.查询品牌和规格列表
		String category= (String) searchMap.get("category");
		if(!"".equals(category)){						
			map.putAll(searchBrandAndSpecList(category));//如果页面用户选择了按照某个分类进行查询则需要根据该分类去查询品牌和规格
		}else{
			if(categoryList.size()>0){			
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));//如果页面用户没有选择分类则按照分组得到的第一个分类去查询品牌和规格
			}	
		}
		return map;
	}
	/**
	 * @param searchMap
	 * @return
	 */
	private Map searchList(Map searchMap) {
		Map map=new HashMap<>();
		//创建高亮显示查询条件
		HighlightQuery query = new SimpleHighlightQuery();
		//通过高亮选项添加高亮显示的域
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		//通过高亮选项设置高亮前缀
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		//通过高亮选项设置高亮后缀
		highlightOptions.setSimplePostfix("</em>");
		//在查询条件中设置高亮选项
		query.setHighlightOptions(highlightOptions );
		
		////1.1 关键字查询  用户页面在搜索框中输入了关键字,创建条件指定item_keywords域及从前台传过来的值     并将该条件添加到查询条件中
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		/*******************根据前台搜索面板的各种搜索条件进行过滤:分类   品牌   规格**start 第十天第18个视频才写到这没学到这之前不要硬抄********/
		//1.2 按商品分类过滤
		if(!"".equals(searchMap.get("category"))  )	{//如果用户选择了分类 
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		//1.3 按品牌过滤
		if(!"".equals(searchMap.get("brand"))  )	{//如果用户选择了品牌
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);			
		}
		//1.4 按规格过滤
		if(searchMap.get("spec")!=null){			
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key :specMap.keySet()){
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);					
			}		
		}
		/************************根据前台搜索面板的各种搜索条件进行过滤:分类   品牌   规格**end 第十天第18个视频才写到这没学到这之前不要硬抄***/
		/************************第十一天:进行过滤:价格区间,分页,排序**start**没学到这之前不要硬抄*********************************/
		//1.5按价格过滤
		if(!"".equals(searchMap.get("price")) ){//传递过来的价格的格式为:500-100  0-100   3000-* 元 角  分
			String[] price = ((String) searchMap.get("price")).split("-");
			if(!price[0].equals("0")){ //如果最低价格不等于0
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);	
			}
			if(!price[1].equals("*")){ //如果最高价格不等于*
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);	
			}			
		}
		
		//1.6 分页
		Integer pageNo= (Integer) searchMap.get("pageNo");//获取前台页面传递过来的页码--当前页
		Object pageNo_str = searchMap.get("pageNo");
		if(pageNo==null){//如果前台页面没传默认第一页
			pageNo=1;
		}
		Integer pageSize= (Integer) searchMap.get("pageSize");//获取前台页面传递过来的每页大小
		if(pageSize==null){
			pageSize=20;
		}
		
		query.setOffset( (pageNo-1)*pageSize  );//limit a,b设置查询的起始索引   注意:查询起始索引和页码之间的关系为:起始索引=(当前页码-1)*每页大小
		query.setRows(pageSize);//每页记录数
		
		
		//1.7 排序
		String sortValue= (String)searchMap.get("sort");//前台页面传递过来的 升序ASC 降序DESC
		String sortField=  (String)searchMap.get("sortField");//前台页面传递过来的排序字段
		
		if(sortValue!=null && !sortValue.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);				
			}
			if(sortValue.equals("DESC")){
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);				
			}
		}
		/************************第十一天:进行过滤:价格区间,分页,排序**end**没学到这之前不要硬抄*****************************/
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query , TbItem.class);
		//高亮入口集合(每条记录的高亮入口)
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();		
		for(HighlightEntry<TbItem> entry:entryList  ){
			//获取每条记录的高亮列表======为什么是集合?因为需要高亮显示的域可能不止一个
			List<Highlight> highlightList = entry.getHighlights();
			/*
			for(Highlight h:highlightList){
				List<String> sns = h.getSnipplets();//每个域有可能存储多值
				System.out.println(sns);				
			}*/			
			if(highlightList.size()>0 &&  highlightList.get(0).getSnipplets().size()>0 ){
				TbItem item = entry.getEntity(); //可以得到原始的数据
				item.setTitle(highlightList.get(0).getSnipplets().get(0));			
			}			
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//总页数
		map.put("total", page.getTotalElements());//总记录数
		return map;
	}
	
	
	
	/**
	 * 分组查询（查询商品分类列表）
	 * 该方法的目的是：通过用户输入的关键字去查询跟该关键字相关的分类的名称
	 * @return
	 */
	@SuppressWarnings({"rawtypes" })
	private List<String> searchCategoryList(Map searchMap){
		List<String> list=new ArrayList<String>();
		Query query=new SimpleQuery("*:*");
		//根据关键字查询  
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));// 相当于where ...
		query.addCriteria(criteria);
		
		//给分组选项中添加域,然后将分组选项设置到查询条件中
		GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");  //相当于group by ...
		query.setGroupOptions(groupOptions);
		
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");//按照哪些域分组的就可以根据哪些域获取分组结果
		//获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for(GroupEntry<TbItem> entry:entryList  ){
			list.add(entry.getGroupValue()	);//entry.getGroupValue()获得根据条件查询的分组域上的值，且将分组的结果添加到返回值中
		}
		return list;
	}
	
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 根据商品分类名称查询品牌和规格列表
	 * @param category 商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map=new HashMap();
		//1.根据商品分类名称得到模板ID	
		Long templateId= (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(templateId!=null){
			//2.根据模板ID获取品牌列表,取出来的数据结构为:[{"id":16,"text":"TCL"},{"id":13,"text":"长虹"},{"id":14,"text":"海尔"}]
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);	
			System.out.println("品牌列表条数："+brandList.size());
			
			//3.根据模板ID获取规格列表 取出来的数据结构为:[{"id":27,"text":"网络",options:[{optionName:'4G'}]},{"id":32,"text":"机身内存",options:[{optionName:'4G'}]}]
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);		
			System.out.println("规格列表条数："+specList.size());
		}	
		
		return map;
	}
	
	
	/**
	 * 将商品SKU列表导入到索引库
	 */
	@Override
	public void importList(List<TbItem> list) {
		if(list==null ||list.size()<1){
			return;
		}
		for(TbItem item:list){
			//item.getSpec()的数据格式为:{"机身内存":"16G","网络":"联通3G"}
			Map specMap = JSON.parseObject(item.getSpec(), Map.class);//从数据库中提取规格json字符串转换为map
			item.setSpecMap(specMap);
		}
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	/**
	 * 根据商品spu的id删除商品数据
	 */
	@Override
	public void deleteByGoodsIds(List goodsIds) {
		Query query=new SimpleQuery("*:*");		
		Criteria criteria=new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);		
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
