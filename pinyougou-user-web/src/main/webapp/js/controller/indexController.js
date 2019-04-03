//首页控制器
app.controller('indexController',function($scope,loginService,orderService){
	$scope.pageSize=5;
	$scope.showName=function(){
			loginService.showName().success(
					function(response){
						$scope.loginName=response.loginName;
						$scope.headPic=response.headPic;
					}
			);
	}
	$scope.findList=function(status,pageNum){
			$scope.pageNum=pageNum;
			$scope.status=status;
			orderService.findList(status,pageNum,$scope.pageSize).success(
					function(response){
						
						$scope.totalPages=Math.ceil(response.total/$scope.pageSize);
						buildPageLabel();//构建分页栏	
						$scope.orderList=response.rows;
					}
			)
	}
	
	//构建分页栏	
	buildPageLabel=function(){
		//构建分页栏
		$scope.pageLabel=[];
		var firstPage=1;//开始页码
		var lastPage=$scope.totalPages;//截止页码
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后边有点
		
		if($scope.totalPages>5){  //如果页码数量大于5
			
			if($scope.pageNum<=3){//如果当前页码小于等于3 ，显示前5页
				lastPage=5;
				$scope.firstDot=false;//前面没点
			}else if( $scope.pageNum>= $scope.totalPages-2 ){//显示后5页
				firstPage=$scope.totalPages-4;	
				$scope.lastDot=false;//后边没点
			}else{  //显示以当前页为中心的5页
				firstPage=$scope.pageNum-2;
				lastPage=$scope.pageNum+2;
			}			
		}else{
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后边无点
		}
		
		
		//构建页码
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
		
	}
	//提交订单
	$scope.submitOrder=function(orderId){
		location.href='pay.html#?orderId='+orderId;
		/*orderService.submitOrder(orderId).success(
			function(response){
				if(response!={} || response != null){
					if(response.payType == '1'){
						location.href='pay.html#?orderId='+response.outTradeNo;
					}else{
						location.href='paysuccess.html';
					}
				}else{
					alert('订单不存在');
				}
				
			}
		);*/
	}
	
	//修改订单状态(传递订单状态)
	$scope.updateOrderStatus=function(status,orderId){
		orderService.updateOrderStatus(status,orderId).success(
			function(response){
				if(response.success){
					$scope.findList($scope.status,$scope.pageNum,$scope.pagesize);//(当前页,每页显示数,订单状态码)
				}else{
					alert(response.message);
				}
			}
		);
	}
	
});