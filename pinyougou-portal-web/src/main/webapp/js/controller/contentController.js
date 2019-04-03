app.controller('contentController',function($scope,contentService){
	
	$scope.contentList=[];//广告列表
	
	//根据广告分类id查询广告列表
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){ //
				
				/**
				 * 首先注意返回的数据response格式为:[{id:1,url:"http://www.baidu.com",pic:"afd"},{}]
				 * 
				 * 
				 * 这里的处理方式请注意:因为页面有好多个地方都有广告,那针对每个广告都要定义一个变量吗?
				 * 我们可以这样:
				 * 	1,定义一个变量$scope.contentList=[]用于存储所有的广告
				 * 	2,用广告的分类id作为该集合的下标,在对应位置上存储对应的广告
				 * 		比如：
				 * 			首页轮播图的广告分类id为1,查询出来的结果为[{id:1,url:"http://www.baidu.com",pic:"afd"},{}]
				 * 		那在整个变量$scope.contentList的下标为1的位置存储查询出来的结果
				 * 
				 * 
				 * 
				 * 
				 * [[],[{id:1,url:"http://www.baidu.com",pic:"afd"},{}],[],[],[]]
				 */
				$scope.contentList[categoryId]=response;
			}
		);		
	}
	
	
	
	
	
	
	
	//搜索  （传递参数）跳转到搜索页
	$scope.search=function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
	
});