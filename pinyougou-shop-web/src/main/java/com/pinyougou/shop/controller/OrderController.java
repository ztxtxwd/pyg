package com.pinyougou.shop.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;











import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.WorkBook;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.sellergoods.service.ItemService;
import com.pinyougou.sellergoods.service.OrderItemService;
import com.pinyougou.sellergoods.service.OrderService;

import entity.PageResult;
import entity.Result;



@RestController
@RequestMapping("/order")
public class OrderController {
	
	@Reference
	private OrderService orderService;
	
	@Reference
	private OrderItemService orderItemService;
	
	@Reference
	private ItemService itemService;
	
	@Reference
	private GoodsService goodsService;
	
	@Reference
	private ItemCatService itemCatService;
	
	@RequestMapping("/updateStatus")
	public Result updateStatus(String status,String orderId){
		try {
			orderService.updateStatus(status, Long.parseLong(orderId));
			return new Result(true, "更新成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new Result(false, "更新失败");
		}
		
	}
	
	@RequestMapping("/findOrder")
	public PageResult findOrder(String status,int pageNum,int pageSize){
		String sellerId=SecurityContextHolder.getContext().getAuthentication().getName();
		return orderService.findOrderList(status, sellerId, pageNum, pageSize);
	}
	
	
	@RequestMapping("/exportXls")
	public void exportXls(String status,String date1,String date2,HttpServletResponse response) throws IOException, ParseException{
		String sellerId=SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println(sellerId);
		if (status==null||status==""||"undefined".equals(status)) {
			status="*";
		}

		String[] statuses={"全部","未付款","已付款未发货","","已发货未收货","交易完成（已收货"};
		String[] paymentTypes={"","在线支付","货到付款"};
		String[] orderSources={"","app端","pc端","M端","微信端","手机qq端"};
		
		DateFormat fmt =new SimpleDateFormat("yyyy-MM-dd");
		Date d1=fmt.parse(date1);
		Date d2=fmt.parse(date2);
		
		String fileName="";
		try {
			//定义文件名
			fileName = date1+"至"+date2+statuses[Integer.parseInt(status)]+"订单.xlsx";
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			status="0";
		}
		WorkBook workBook=new WorkBook();
		workBook.setStartTime(d1);
		workBook.setEndTime(d2);
		workBook.setSellerId(sellerId);
		workBook.setStatus(status);
		//1.创建一个工作簿
		//使用字节流输出
		
		
			
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
			
			//2.创建工作表sheel文档
			
			XSSFSheet createSheet = xssfWorkbook.createSheet("订单报表");
			
			//3.准备头数据
			
			String[] header= {"订单号","支付类型","订单状态","下单用户账号","收货人","收货人地址","收货人电话","订单来源","商品标题","商品单价","购买数量","金额小计","所属品牌","一级分类","二级分类","三级分类","下单时间"};
			
			List<TbOrder> orderList = orderService.findOrderList(workBook);
			System.out.println("orderlist:"+orderList.size());
			//3.1创建表头
			
			XSSFRow headerRow = createSheet.createRow(0);
			
			//3.2创建表头所有列
			
			for(int i=0;i<header.length;i++) {
				
				//创建每一列
				
				XSSFCell xssfCell = headerRow.createCell(i);
				//为每个单元格填充值
				
				xssfCell.setCellValue(header[i]);
			}
			
			for(int i=0;i<orderList.size();i++) {
				TbOrder order=orderList.get(i);
				
				Long orderId = order.getOrderId();
				List<TbOrderItem> orderItemList = orderItemService.findByOrderId(orderId);
				System.out.println("orderitemlist:"+orderItemList.size());
				for(TbOrderItem orderItem:orderItemList) {
					//查询SKU
					TbItem item = itemService.findOne(orderItem.getItemId());
					
					TbGoods goods = goodsService.findOne(orderItem.getGoodsId()).getGoods();
					
					TbItemCat itemCat1 = itemCatService.findOne(goods.getCategory1Id());
					
					TbItemCat itemCat2 = itemCatService.findOne(goods.getCategory2Id());
					
					TbItemCat itemCat3 = itemCatService.findOne(goods.getCategory3Id());
					
					
					
					
					XSSFRow row = createSheet.createRow(i+1);
					
					XSSFCell idCell = row.createCell(0);
					idCell.setCellValue(order.getOrderId()+"");//订单号
					
					XSSFCell paymentTypeCell = row.createCell(1);
					paymentTypeCell.setCellValue(paymentTypes[Integer.parseInt(order.getPaymentType())]);//支付类型
					
					XSSFCell statusCell = row.createCell(2);
					statusCell.setCellValue(statuses[Integer.parseInt(order.getStatus())]);//支付状态
					
					XSSFCell userIDCell = row.createCell(3);
					userIDCell.setCellValue(order.getUserId());//下单用户账号
					
					XSSFCell receiverCell = row.createCell(4);
					receiverCell.setCellValue(order.getReceiver());//收货人
					
					XSSFCell receiverAddressCell = row.createCell(5);
					receiverAddressCell.setCellValue(order.getReceiverAreaName());//收货人地址
					
					XSSFCell receiverMobileCell = row.createCell(6);
					receiverMobileCell.setCellValue(order.getReceiverMobile());//收货人电话
					
					XSSFCell sourceTypeCell = row.createCell(7);
					sourceTypeCell.setCellValue(orderSources[Integer.parseInt(order.getSourceType())]);//订单来源
					
					
					XSSFCell titleCell = row.createCell(8);
					titleCell.setCellValue(orderItem.getTitle());//商品标题
					
					XSSFCell priceCell = row.createCell(9);
					priceCell.setCellValue(orderItem.getPrice().doubleValue());//商品单价
					
					XSSFCell numCell = row.createCell(10);
					numCell.setCellValue(orderItem.getNum());//购买数量
					
					XSSFCell totalFeeCell = row.createCell(11);
					totalFeeCell.setCellValue(orderItem.getTotalFee().doubleValue());//金额小计
					
					XSSFCell brandCell = row.createCell(12);
					brandCell.setCellValue(item.getBrand());//所属品牌
					
					XSSFCell categoryCell1 = row.createCell(13);
					categoryCell1.setCellValue(itemCat1.getName());//一级分类
					
					XSSFCell category2 = row.createCell(14);
					category2.setCellValue(itemCat2.getName());//二级分类
					
					XSSFCell category3 = row.createCell(15);
					category3.setCellValue(itemCat3.getName());//三级分类、
					
					XSSFCell createTime = row.createCell(16);
					createTime.setCellValue(fmt.format(order.getCreateTime()));//下单时间
				}
				
				
				
			}
			
				
				//设置文件下载的响应头
				response.setContentType("application/vnd.ms-excel;charset=utf-8");
				response.setHeader("Content-Disposition", "attachment;filename="+new String((fileName).getBytes(), "iso-8859-1"));
				//使用工作簿对象进行输出到字节流  这里的字节流是response对象提供
				xssfWorkbook.write(response.getOutputStream());
				//关闭
				xssfWorkbook.close();
			
				
				
			//4创建数据体行 创建每行对应的列 并为每列赋值
		
	}
	
}
