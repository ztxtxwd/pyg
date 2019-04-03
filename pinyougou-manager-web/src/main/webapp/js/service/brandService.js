//创建品牌服务
app.service("brandService",function($http){
	//查询所有
	this.findAll = function(){
		return $http.get("../brand/findAll.do");
	}
	//分页查询
	this.findPage = function(page,size){
		return $http.get('../brand/findPage.do?page='+page+'&size='+size+'')
	}
	//添加
	this.add = function(entity){
		return $http.post('../brand/add.do',entity);
	}
	//更新
	this.update = function(entity){
		return $http.post('../brand/update.do',entity);
	}
	//根据id查询
	this.findOne = function(id){
		return $http.get('../brand/findOne.do?id='+id);
	}
	//根据选中的id数组删除
	this.dele = function(ids){
		return $http.get('../brand/delete.do?ids='+ids);
	}
	//条件查询
	this.search = function(page,size,searchEntity){
		return $http.post('../brand/search.do?page='+page +'&size='+size, searchEntity);
	}
	
	//获取select2需要格式的品牌列表数据
	this.selectOptionList = function(){
		return $http.get('../brand/selectOptionList.do');
	}
});
		