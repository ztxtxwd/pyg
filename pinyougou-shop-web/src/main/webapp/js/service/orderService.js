app.service("orderService",function($http){
	
	this.exportXls=function(status,date1,date2){
		
		return $http.get('../order/exportXls.do?status='+status+'&date1='+date1+'&date2='+date2);
		
	}
	
	this.findOrder=function(status,pageNum,pageSize){
		
		return $http.get('../order/findOrder.do?status='+status+'&pageNum='+pageNum+'&pageSize='+pageSize);
		
	}
	this.updateStatus=function(status,orderId){
		
		return $http.get('../order/updateStatus.do?status='+status+'&orderId='+orderId);
		
	}
	
})