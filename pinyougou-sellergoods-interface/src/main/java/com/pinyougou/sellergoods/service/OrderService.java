package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojogroup.WorkBook;

import entity.PageResult;

public interface OrderService {

	
	public List<TbOrder> findOrderList(WorkBook workBook);
	
	PageResult findOrderList(String status,String sellerId,int pageNum,int pageSize);
	
	void updateStatus(String status,Long orderId);
}
