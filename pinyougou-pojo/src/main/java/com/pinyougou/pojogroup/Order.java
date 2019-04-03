package com.pinyougou.pojogroup;


import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;

public class Order implements Serializable {
	private TbOrder tbOrder;
	private List<TbOrderItem> tbOrderItemList;
	public TbOrder getTbOrder() {
		return tbOrder;
	}
	public void setTbOrder(TbOrder tbOrder) {
		this.tbOrder = tbOrder;
	}
	public List<TbOrderItem> getTbOrderItemList() {
		return tbOrderItemList;
	}
	public void setTbOrderItemList(List<TbOrderItem> tbOrderItemList) {
		this.tbOrderItemList = tbOrderItemList;
	}
	
}
