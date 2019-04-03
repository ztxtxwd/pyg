
//创建品牌控制器
app.controller("brandController",function($scope,$controller,brandService){
	
	//引入baseController
	
	$controller("baseController",{$scope:$scope});
	
	//定义方法查询所有品牌列表
	$scope.findAll = function(){
		brandService.findAll().success(function(response){
			//response就是后台返回的品牌列表[{"firstChar":"L","id":1,"name":"联想"},{"firstChar":"L","id":1,"name":"联想"}]
			$scope.list = response;
		});
	}
	
	//分页查询
	$scope.findPage = function(page,size){
		brandService.findPage(page,size).success(function(response){
			//注意此刻分页返回的数据格式response:{"rows":[{"firstChar":"K","id":21,"name":"康佳"},{"firstChar":"L","id":22,"name":"LG"}],"total":22}
			$scope.list  = response.rows;// $scope.list是页面循环的变量
			$scope.paginationConf.totalItems = response.total;//更新总记录数
		});
	}
	
	//新增品牌
	/* $scope.add = function(){
		//后台需要传品牌实体TbBrand  $scope.entity 就代表了页面的品牌实体
		brandService.add($scope.entity).success(function(response){
			//response:{"success":true,"message":"增加成功"}
			if(response.success){
				//品牌增加成功后重新加载品牌列表
				$scope.reloadList();
			}else{
				alert(response.message);
			}
		});
	} */
	
	//保存品牌
	$scope.save = function(){
		var object = null;
		if($scope.entity.id!=null){
			object = brandService.update($scope.entity);
		}else{
			object = brandService.add($scope.entity);
		}
		object.success(function(response){
			//response:{"success":true,"message":"增加成功"}
			if(response.success){
				//品牌保存成功后重新加载品牌列表
				$scope.reloadList();
			}else{
				alert(response.message);
			}
		});
	}
	
	//根据id查询品牌
	$scope.findOne = function(id){
		
		brandService.findOne(id).success(function(response){
			//response是后台返回的品牌对象，$scope.entity是控制器绑定的品牌变量
			$scope.entity = response;
		});
	}
	
	
	
	//删除品牌
	$scope.dele = function(){
		//删除之前询问一下是否删除
		if(confirm('确定要删除吗?')){
			brandService.dele($scope.selectIds).success(function(response){
				if(response.success){
					//删除完成后清空选中的品牌id数组
					$scope.selectIds = [];
					//删除完后刷新品牌列表数据
					$scope.reloadList();
				}else{
					alert(response.message);
				}
			});
		}
	}
	
	//初始化条件查询对象
	$scope.searchEntity = {};
	//品牌条件查询
	$scope.search = function(page,size){
		
		//post提交跟在路径后面的page  size相当于是在请求行中，后台接收的时候跟以前一样接收，searchEntity是在请求体,后台使用了@RequstBody
		brandService.search(page,size,$scope.searchEntity).success(
				function(response){
					$scope.list=response.rows;//显示当前页数据 	
					$scope.paginationConf.totalItems=response.total;//更新总记录数 
				}		
			);	
	}
});