package com.pinyougou.sellergoods.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbOrderItemExample;
import com.pinyougou.pojo.TbOrderItemExample.Criteria;
import com.pinyougou.sellergoods.service.OrderItemService;

@Service
public class OrderItemServiceImpl implements OrderItemService {

	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	@Override
	public List<TbOrderItem> findByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		TbOrderItemExample example = new TbOrderItemExample();
		Criteria criteria=example.createCriteria();
		criteria.andOrderIdEqualTo(orderId);
		List<TbOrderItem> orderItems = (List<TbOrderItem>)orderItemMapper.selectByExample(example);
		
		return orderItems;
	}

}
