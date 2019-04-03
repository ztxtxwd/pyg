//服务层
app.service('orderService',function($http){
	//读取列表数据绑定到表单中
	this.findList=function(status,pageNum,pageSize){
		return $http.get('../order/findList.do?status='+status+'&pageNum='+pageNum+"&pageSize="+pageSize);
	}
	this.submitOrder=function(order){
		return $http.post('/user/findPayLog.do?orderId='+order);
	}
	this.updateOrderStatus=function(status,orderId){
		return $http.get('/order/updateOrderStatus.do?status='+status+'&orderId='+orderId);
	}
});