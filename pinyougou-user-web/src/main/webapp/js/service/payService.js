app.service('payService',function($http){
	//本地支付

	this.createNative=function(orderId){
		return $http.get('pay/createNative.do?orderId='+orderId);
	}
	
	//查询支付状态
	this.queryPayStatus=function(orderId,out_trade_no){
		return $http.get('pay/queryPayStatus.do?orderId='+orderId+'&out_trade_no='+out_trade_no);
	}
});