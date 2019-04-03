package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.Date;

public class WorkBook implements Serializable{

	private String status;
	
	private Date startTime;
	
	private Date endTime;
	
	private String sellerId;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	@Override
	public String toString() {
		return "WorkBook [status=" + status + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", sellerId=" + sellerId + "]";
	}
	
	

	
	
	
	
}
