package com.pinyougou.user.service;

import entity.PageResult;

public interface OrderService {
	public PageResult findList(String status,String userId,int pageNum,int pageSize);
	public void updateOrderStatus(String status, String orderId);
}
