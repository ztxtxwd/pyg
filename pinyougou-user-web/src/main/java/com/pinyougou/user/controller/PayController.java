package com.pinyougou.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.user.service.PayLogService;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private PayLogService payLogService;
	
	@RequestMapping("/createNative")
	public Map createNative(String orderId){
		//1.获取当前登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//2.提取支付日志（支付日志）
		TbPayLog payLog = payLogService.findPayLog(orderId);
		//3.调用微信支付接口
		if(payLog!=null){
			return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");		
		}else{
			return new HashMap<>();
		}		
	}

	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String orderId,String out_trade_no){
		Result result=null;
		int x=0;
		while(true){
			
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);//调用查询
			if(map==null){
				result=new Result(false, "支付发生错误");
				break;
			}
			if(map.get("trade_state").equals("SUCCESS")){//支付成功
				result=new Result(true, "支付成功");				
				//orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));//修改订单状态
				payLogService.updatePaymentstatus(orderId, map.get("transaction_id"));
				break;
			}
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			x++;
			if(x>=100){				
				result=new Result(false, "二维码超时");
				break;				
			}
			
		}
		return result;
	}
	
	
}
