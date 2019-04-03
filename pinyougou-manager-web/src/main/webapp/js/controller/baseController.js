app.controller("baseController",function($scope){
	

	//分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法 
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function(){
			$scope.reloadList(); //页面加载就会触发，因此也不用ng-init触发了
		}
	};
	

	//刷新列表
	$scope.reloadList = function(){
		//第一个参数当前页，第二个参数每页大小
		//$scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
		$scope.search( $scope.paginationConf.currentPage ,  $scope.paginationConf.itemsPerPage );
	}
	

	//保存选中的品牌id数组
	$scope.selectIds = [];
	
	//点击复选框动态组合品牌id数组
	$scope.updateSelection = function($event,id){
		if($event.target.checked){
			$scope.selectIds.push(id);//push向集合添加元素 	
		}else{
			var index = $scope.selectIds.indexOf(id);//查找值的 位置
			$scope.selectIds.splice(index,1);//参数1：移除的位置 参数2：移除的个数  
		}
	}
	
	
	$scope.jsonToString=function(jsonString,key){
		//注意：jsonString 传递过来数据格式为:[{id:1,text:""},{}]  key的值text
		var json= JSON.parse(jsonString);
		var value="";
		for(var i=0;i<json.length;i++){
			if(i>0){
				value+=",";
			}
			//注意这里为什么不能用json[i].key
			/**
			 * json[i]:{id:1,text:"ad"}
			 * json[i].key其实是想找{id:1,key:"ad"}
			 * 但是我们这里key是变量因此这样写json[i][key]
			 * 注意这是去json值的一种特殊情况
			 */
			value +=json[i][key];			
		}
		return value;
	}
});