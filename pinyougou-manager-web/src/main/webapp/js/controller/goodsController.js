 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	
	///////////////////////////////////////////////////从shop-web工程中拷贝过来
	//定义商品列表页面显示商品审核状态的数组变量
	$scope.status = ['未审核','已审核','审核未通过','已关闭'];
	
	//同理,将所有的分类信息查询出来,以分类的id当作数组的下标,分类的名称当作对应下标的值
	$scope.itemCatList =  []; 
	$scope.findItemCatList = function(){
		itemCatService.findAll().success(function(response){
			//注意这里是查询tb_item_cat,查询所有,返回的数据格式为:[{id:1,name="手机数码",parentId=0}]
			for(var i=0;i<response.length;i++){
				$scope.itemCatList[response[i].id] = response[i].name;
			}
			
		});
	}
	
	
	//运营商管理后台需要对商品进行审核,审核通过或者驳回其实就是修改商品的状态
	//更新状态
	$scope.updateStatus=function(status){
		//$scope.selectIds是在baseController中定义好的专门保存复选框选中的值的,注意页面需要给复选框绑定在baseController中写好的事件updateSelection
		goodsService.updateStatus( $scope.selectIds ,status).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新页面
					
					//清空复选框选中而保存的值
					$scope.selectIds=[];
				}else{
					alert(response.message);
				}				
			}
		);		
	}
    
});	
