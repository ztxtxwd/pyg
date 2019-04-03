app.controller('orderController',function($scope,$controller,orderService){

	$controller('baseController',{$scope:$scope});//继承

	
	$scope.exportXls=function(){
		location.href="localhost:9102/order/exportXls.do?status="+$scope.status+"&date1="+$scope.date1+"&date2="+$scope.date2;
		
	}
	$scope.status='2';
	$scope.search=function(page,size){
		orderService.findOrder($scope.status,page,size).success(
				function(response){
					$scope.list=response.rows;//显示当前页数据 	
					$scope.paginationConf.totalItems=response.total;//更新总记录数 
				}		
		);
		
	}
	$scope.updateStatus=function(status,orderId){
		orderService.updateStatus(status,orderId).success(
				function(response){
					if(response.success){
						$scope.reloadList();
					}else{
						alert(response.message);
					}
					
				}		
		);
		
	}
	
	
	
})
