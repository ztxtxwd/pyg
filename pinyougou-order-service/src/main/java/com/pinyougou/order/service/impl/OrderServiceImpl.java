package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	/**
	 * 增加
	 */
	public void add1(TbOrder order) {//这里传过来的订单对象:里面包含了用户在订单页面选择的一些相关数据,比如地址,付款方式等等,而订单的其他数据在redis中
		
		//1.从redis中提取购物车列表
		List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
				
		//2.循环购物车列表添加订单
		for(Cart  cart:cartList){
			TbOrder tbOrder=new TbOrder();
			long orderId = idWorker.nextId();	//获取ID		
			tbOrder.setOrderId(orderId);
			tbOrder.setPaymentType(order.getPaymentType());//支付类型
			tbOrder.setStatus("1");//未付款 
			tbOrder.setCreateTime(new Date());//下单时间
			tbOrder.setUpdateTime(new Date());//更新时间
			tbOrder.setUserId(order.getUserId());//当前用户
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//收货人电话
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setSourceType(order.getSourceType());//订单来源
			//tbOrder.setSellerId(order.getSellerId());//商家ID
			tbOrder.setSellerId(cart.getSellerId());//商家ID
			
			double money=0;//合计数
			//循环购物车中每条明细记录
			for(TbOrderItem orderItem:cart.getOrderItemList()  ){
				orderItem.setId(idWorker.nextId());//主键
				orderItem.setOrderId(orderId);//订单编号
				orderItem.setSellerId(cart.getSellerId());//商家ID
				orderItemMapper.insert(orderItem);				
				money+=orderItem.getTotalFee().doubleValue();
			}
			
			tbOrder.setPayment(new BigDecimal(money));//合计
			
			orderMapper.insert(tbOrder);
		}
		
		//3.清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	@Override
	public void add(TbOrder order) {
		
		//1.从redis中提取购物车列表
		List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		
		List<String> orderIdList=new ArrayList();//订单ID集合
		double total_money=0;//总金额
		//2.循环购物车列表添加订单
		for(Cart  cart:cartList){
			TbOrder tbOrder=new TbOrder();
			long orderId = idWorker.nextId();	//获取ID		
			tbOrder.setOrderId(orderId);
			tbOrder.setPaymentType(order.getPaymentType());//支付类型
			tbOrder.setStatus("1");//未付款 
			tbOrder.setCreateTime(new Date());//下单时间
			tbOrder.setUpdateTime(new Date());//更新时间
			tbOrder.setUserId(order.getUserId());//当前用户
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//收货人电话
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setSourceType(order.getSourceType());//订单来源
			tbOrder.setSellerId(cart.getSellerId());//商家ID
			
			double money=0;//合计数
			//循环购物车中每条明细记录
			for(TbOrderItem orderItem:cart.getOrderItemList()  ){
				orderItem.setId(idWorker.nextId());//主键
				orderItem.setOrderId(orderId);//订单编号
				orderItem.setSellerId(cart.getSellerId());//商家ID
				orderItemMapper.insert(orderItem);				
				money+=orderItem.getTotalFee().doubleValue();
			}
			
			tbOrder.setPayment(new BigDecimal(money));//合计
			
			
			orderMapper.insert(tbOrder);
			
			orderIdList.add(orderId+"");
			total_money+=money;
		}
		
		//添加支付日志  每次不管多少订单都只需支付一次,因此千万不要写到循环体内
		if("1".equals(order.getPaymentType())){//支付类型是微信支付时才需要添加支付日志,因为还有一种是货到付款
			TbPayLog payLog=new TbPayLog();
			
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			payLog.setCreateTime(new Date());
			payLog.setUserId(order.getUserId());//用户ID
			payLog.setOrderList(orderIdList.toString().replace("[", "").replace("]", ""));//订单ID串
			payLog.setTotalFee( (long)( total_money*100)   );//金额（分）
			payLog.setTradeState("0");//交易状态
			payLog.setPayType("1");//微信
			payLogMapper.insert(payLog);
			
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存 生成完订单就要跳转到支付页,在支付页就需要生成支付二维码,
		}
		
		
		//3.清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	@Override
	public void addFakeData(){
		for(int i=1;i<6;i++){
			TbOrder tbOrder=new TbOrder();
			long orderId = idWorker.nextId();	//获取ID		
			tbOrder.setOrderId(orderId);
			tbOrder.setPaymentType("1");//支付类型
			tbOrder.setStatus(i+"");//未付款 
			tbOrder.setCreateTime(new Date());//下单时间
			tbOrder.setUpdateTime(new Date());//更新时间
			tbOrder.setUserId("ztx");//当前用户
			tbOrder.setReceiverAreaName("天利商务8楼");//收货人地址
			tbOrder.setReceiverMobile("18633869473");//收货人电话
			tbOrder.setReceiver("小魔仙");//收货人
			tbOrder.setSourceType("2");//订单来源
			tbOrder.setSellerId("qiandu");//商家ID
			tbOrder.setPaymentTime(new Date());
			
			double money=0;//合计数
			//循环购物车中每条明细记录
			
				TbOrderItem orderItem=new TbOrderItem();
				TbOrderItem orderItem2=new TbOrderItem();
				orderItem.setId(idWorker.nextId());//主键
				orderItem.setGoodsId(149187842867992L);//商品ID
				orderItem.setItemId(1369284L);//skuid
				orderItem.setTitle("三星 Galaxy S10 8GB+128GB皓玉白（SM-G9730）超感官全视屏骁龙855双卡双待全网通4G游戏手机 移动4G 128G");
				orderItem.setNum(i);
				orderItem.setPicPath("https://img14.360buyimg.com/n0/jfs/t1/32472/22/3371/192059/5c74b147Ee2eaefb4/8a9eaa0ea1365ed0.jpg");
				orderItem.setPrice(new BigDecimal(9999));
				
				orderItem.setOrderId(orderId);//订单编号
				orderItem.setSellerId("qiandu");//商家ID
				orderItem.setTotalFee(new BigDecimal(9999));
				orderItemMapper.insert(orderItem);	
				orderItem2.setId(idWorker.nextId());//主键
				orderItem2.setGoodsId(149187842867993L);//商品ID
				orderItem2.setItemId(1369286L);//skuid
				orderItem2.setTitle("Apple iPad mini 2019年新款平板电脑 7.9英寸（64G WLAN版/A12芯片/Retina显示屏/MUQY2CH/A）金色");
				orderItem2.setNum(i);
				orderItem2.setPicPath("https://img14.360buyimg.com/n0/jfs/t1/28000/28/11660/111268/5c906504E55f685cb/a5a8a37a2588d180.jpg");
				orderItem2.setPrice(new BigDecimal(9999));
				
				orderItem2.setOrderId(orderId);//订单编号
				orderItem2.setSellerId("qiandu");//商家ID
				orderItem2.setTotalFee(new BigDecimal(2921));
				orderItemMapper.insert(orderItem2);				
				money+=orderItem.getTotalFee().doubleValue();
				money+=orderItem2.getTotalFee().doubleValue();
			
			
			tbOrder.setPayment(new BigDecimal(money));//合计
			
			
			orderMapper.insert(tbOrder);
			if("1".equals(tbOrder.getPaymentType())){//支付类型是微信支付时才需要添加支付日志,因为还有一种是货到付款
				TbPayLog payLog=new TbPayLog();
				
				payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
				payLog.setCreateTime(new Date());
				payLog.setUserId(tbOrder.getUserId());//用户ID
				payLog.setOrderList(orderId+"");//订单ID串
				payLog.setTotalFee( (long)( money*100)   );//金额（分）
				payLog.setTradeState("1");//交易状态
				payLog.setPayType("1");//微信
				payLog.setTransactionId("375017350178"+i);
				payLog.setPayTime(new Date());
				payLogMapper.insert(payLog);
				
				
			}
		}
		
	}
	public static void main(String[] args) {
		
	}
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public TbPayLog searchPayLogFromRedis(String userId) {		
			return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
		}

		@Override
		public void updateOrderStatus(String out_trade_no, String transaction_id) {
			//1.修改支付日志的状态及相关字段
			TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
			payLog.setPayTime(new Date());//支付时间
			payLog.setTradeState("1");//交易成功
			payLog.setTransactionId(transaction_id);//微信的交易流水号
			
			payLogMapper.updateByPrimaryKey(payLog);//修改
			//2.修改订单表的状态
			String orderList = payLog.getOrderList();// 订单ID 串
			String[] orderIds = orderList.split(",");
			
			for(String orderId:orderIds){
				TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
				order.setStatus("2");//已付款状态
				order.setPaymentTime(new Date());//支付时间
				orderMapper.updateByPrimaryKey(order);			
			}
			
			//3.清除缓存中的payLog
			redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
			
		}
		
		
	
}
