package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.user.service.OrderService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Reference
	private OrderService orderService;
	
	@RequestMapping("/findList")
	public PageResult findList(String status,int pageNum,int pageSize){
		return orderService.findList(status, SecurityContextHolder.getContext().getAuthentication().getName(), pageNum, pageSize);
	}
	
	@RequestMapping("/updateOrderStatus")
	public Result updateOrderStatus(String status, String orderId) {
		try {
			orderService.updateOrderStatus(status, orderId);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "非法操作");
			
		}
	}
}
