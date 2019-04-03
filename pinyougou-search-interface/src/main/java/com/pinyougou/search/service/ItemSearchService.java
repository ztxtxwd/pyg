package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbItem;

public interface ItemSearchService {

	
	/**
	 * 搜索方法:
	 * 	传入的参数返回的参数类型都是Map
	 * @param searchMap
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map search(Map searchMap);
	
	/**
	 * 导入列表
	 * @param list
	 */
	public void importList(List<TbItem> list);
	/**
	 * 删除商品列表
	 * @param goodsIds  (SPU)
	 */
	public void deleteByGoodsIds(List goodsIds);
	
	
}
