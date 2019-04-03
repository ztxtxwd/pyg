package com.pinyougou.sellergoods.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pinyougou.pojo.TbOrderItem;


public interface OrderItemService {

	public List<TbOrderItem> findByOrderId(Long orderId);
	
}
