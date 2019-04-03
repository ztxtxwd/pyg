app.controller('searchController',function($scope,$location,searchService){
	//定义前台传递给后台的搜索对象的结构  keywords:用户输入的关键字  category:商品分类   brand:品牌    spec:规格
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
	
	
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo);//转换为数字
		searchService.search($scope.searchMap).success(
			function(response){
				//搜索方法后台返回的也是一个map,map中的rows就是数据集合
				//{rows:[{},{},{}]}
				$scope.resultMap=response;	
				
				
				
				buildPageLabel();//构建分页栏	
			}
		);		
	}
	
	//构建分页栏	
	buildPageLabel=function(){
		//构建分页栏
		$scope.pageLabel=[];
		var firstPage=1;//开始页码
		var lastPage=$scope.resultMap.totalPages;//截止页码
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后边有点
		
		if($scope.resultMap.totalPages>5){  //如果页码数量大于5
			
			if($scope.searchMap.pageNo<=3){//如果当前页码小于等于3 ，显示前5页
				lastPage=5;
				$scope.firstDot=false;//前面没点
			}else if( $scope.searchMap.pageNo>= $scope.resultMap.totalPages-2 ){//显示后5页
				firstPage=$scope.resultMap.totalPages-4;	
				$scope.lastDot=false;//后边没点
			}else{  //显示以当前页为中心的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
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
	
	
	//添加搜索项  改变searchMap的值
	$scope.addSearchItem=function(key,value){
		
		if(key=='category' || key=='brand' || key=='price'){//如果用户点击的是分类或品牌或价格
			$scope.searchMap[key]=value;
			
		}else{//用户点击的是规格
			$scope.searchMap.spec[key]=value;		
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price' ){//如果用户点击的是分类或品牌或价格
			$scope.searchMap[key]="";
		}else{//用户点击的是规格
			delete $scope.searchMap.spec[key];		
		}
		$scope.search();//查询
	}
	
	//分页查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return ;
		}		
		$scope.searchMap.pageNo=pageNo;
		$scope.search();//查询
	}
	
	//判断当前页是否为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}		
	}
	
	//判断当前页是否为最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}	
	}
	
	//排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		
		$scope.search();//查询
	}
	
	//判断关键字是否是品牌
	$scope.keywordsIsBrand=function(){
		//遍历每次查询出来的品牌列表[{"id":16,"text":"TCL"},{"id":13,"text":"长虹"},{"id":14,"text":"海尔"}],
		for(var i=0;i< $scope.resultMap.brandList.length;i++){	
			//查询我们输入的关键字是否是品牌列表中字符串的子字符串
			if( $scope.searchMap.keywords.indexOf( $scope.resultMap.brandList[i].text )>=0){
				return true;				
			}			
		}
		return false;
	}
	
	//搜索页肯定是从首页跳转过来的,在首页输入关键字,然后跳转到搜索页,搜索耶接收关键字并自动查询
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords= $location.search()['keywords'];
		$scope.search();//查询
	}
});