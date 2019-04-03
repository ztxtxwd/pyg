package com.pinyougou.sellergoods.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojogroup.WorkBook;
import com.pinyougou.sellergoods.service.OrderService;

import entity.PageResult;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	
	@Override
	public List<TbOrder> findOrderList(WorkBook workBook) {
		// TODO Auto-generated method stub
		System.out.println(workBook.toString());
		String sellerId = workBook.getSellerId();
		
		TbOrderExample example=new TbOrderExample();
		
		Criteria criteria = example.createCriteria();
		if (!(workBook.getStatus().equals("0"))) {
			criteria.andStatusEqualTo(workBook.getStatus());
		}
		
		criteria.andSellerIdEqualTo(sellerId);
		
		criteria.andCreateTimeGreaterThanOrEqualTo(workBook.getStartTime());
		
		criteria.andCreateTimeLessThanOrEqualTo(workBook.getEndTime());
		
		
		
		
		return orderMapper.selectByExample(example);
	}


	@Override
	public PageResult findOrderList(String status,String sellerId, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		PageHelper.startPage(pageNum,pageSize);
		TbOrderExample example=new TbOrderExample();
		Criteria criteria=example.createCriteria();
		criteria.andStatusEqualTo(status);
		criteria.andSellerIdEqualTo(sellerId);
		Page<TbOrder> orderList=(Page<TbOrder>) orderMapper.selectByExample(example);
		for(int i=0;i<orderList.size();i++){
			orderList.get(i).setOrderIdString(orderList.get(i).getOrderId().toString());
		}
		return new PageResult(orderList.getTotal(), orderList.getResult());
	}


	@Override
	public void updateStatus(String status, Long orderId) {
		// TODO Auto-generated method stub
		orderMapper.updateStatus(status,orderId);
	}

}
