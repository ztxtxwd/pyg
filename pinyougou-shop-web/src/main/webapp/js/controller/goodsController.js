 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){	
	
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
	$scope.findOne=function(){
		/**
		 * 静态页面之间的页面跳转,如果传递了参数可以通过angular提供的地址路由服务$location.search方法来获取
		 * 但是这个时候angular要求的地址路由的写法为: http://localhost:9102/admin/goods_edit.html#?id=12321321
		 */
		var id = $location.search()['id'];
		//注意:因为添加商品和修改商品去到的页面都是goods_edit.html 在这个页面中都引入了该js文件，因此当我们修改商品的时候去查询商品信息即可,添加操作直接返回
		if(id == null || id == undefined){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//注意:商品介绍是用富文本编辑器,需要单独处理
				editor.html($scope.entity.goodsDesc.introduction);
				
				//注意:通过id查询出来的也是一个组合实体类对象,整个entity是一个json对象,但是对于里面的属性比如图片列表,只是json格式的字符串
				//转换图片字段
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				/**
				 * 转换扩展属性字段:这里一定要注意还需要同步去修改一段代码:在js文件的第225行
				 * 为什么要修改:
				 * 	当我们编辑商品时从商品的详情表tb_goods_desc中查询出来的扩展属性是有名称且有值的,
				 * 	但是由于我们添加商品的时候监听了模板变量,当我们模板发生改变时会去查询模板中管理的扩展属性的名称,但是模板中的扩展属性只有名称没有值,从而覆盖了
				 * 	从商品表中查询出来的数据，
				 */
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				
				//转换规格选项数据
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				
				//转换SKU列表中的spec属性的值,返回组合实体类中的itemList代表了SKU列表,列表中的每个元素都需要转换,因此需要循环转换
				for(var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	//保存 
	$scope.save=function(){	
		//注意：这里entity是一个组合实体类，其结构为entity:{goods:{},goodsDesc:{},itemList:[{}]}
		//将富文本中的商品介绍提取出来赋给相应字段
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("商品保存成功!");
					 //清空页面数据
					$scope.entity={};
					//清空富文本
					editor.html("");
					
					/*
					 *不管新增还是修改,完成之后应该跳转到商品列表页面goods.html
					*/
					location.href='goods.html';
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	//保存 商品
	$scope.add=function(){	
		//注意：这里entity是一个组合实体类，其结构为entity:{goods:{},goodsDesc:{},itemList:[{}]}
		
		
		//将富文本中的商品介绍提取出来赋给相应字段
		$scope.entity.goodsDesc.introduction=editor.html();
		
		goodsService.add( $scope.entity).success(
			function(response){
				if(response.success){
					alert("商品保存成功!");
					//清空页面数据
					$scope.entity={};
					//清空富文本
					editor.html("");
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
    
	
	
	//上传图片
	$scope.uploadFile = function(){
		uploadService.uploadFile().success(function(response){
			if(response.success){
				//注意如果上传成功返回的数据response.message代表的就是图片的完整路径
				$scope.image_entity.url = response.message;
			}else{
				alert(response.message);
			}
		});
	}
	
	//预定义代表整个商品的实体entity,是一个组合实体类
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[{}]} //最好把结构定义完整
	
	//将当前上传的图片实体存入图片列表
	$scope.add_image_entity=function(){
		//图片列表存储在商品详情表中tb_goods_desc 在组合实体类中的属性是entity.goodsDesc.itemImages
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);	//把我们每次上传的图片实体添加到图片列表中		
	}
	
	//移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	
	//查询一级商品分类列表  需要页面加载初始化调用ng-init
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(
			function(response){
			//查询分类表tb_item_cat返回的数据,其格式为:[{id:1,name:"分类mingc",parentId:0,typeId:35},{id:1,name:"分类mingc",parentId:0,typeId:35}]
			$scope.itemCat1List=response;			
			}
		);
	}
	
	
	//当一级分类的下拉列表发生改变时
	//通过angular提供的监控变量的思想完成当一级分类下来列表发生改变时查询二级分类
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		$scope.itemCat3List=[];
		//当一级分类变量发生改变时查询二级分类
		itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat2List=response;			
				}
		);
	});
	//同理 当二级分类发生改变时查询三级分类
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
		//当二级分类变量发生改变时查询三级级分类
		itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat3List=response;			
				}
		);
	});
	
	
	//同理当选择了三级分类后，后面的模板id应该显示的是刚刚所选择的分类对应的模板id
	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		//当三级分类变量发生改变时查询对应的模板id----分类表中已保存了该分类对应的模板id，因此只需要根据分类id查询分类表tb_item_cat取出type_id字段即可
		itemCatService.findOne(newValue).success(
				function(response){
					//根据分类id查询分类对象返回的数据格式:{id:1,name:"分类mingc",parentId:0,typeId:35}
					$scope.entity.goods.typeTemplateId=response.typeId;
				}
		);
	});
	
	
	//添加商品的页面需要展示品牌列表,但这里的品牌列表并不是查询品牌表中的所有数据,而是根据模板id查找模板对象,因为模板里已经关联了品牌列表了
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
		
		//newValue就是模板id,以此来查询模板对象
		typeTemplateService.findOne(newValue).success(function(response){
			//返回的数据格式为:response:{id:1,name:"aa",brandIds:[{id:1,text:"好品牌"}],specIds:[{id:1,text:"好规格"}],customAttributeItems:[{"text":"内存大小"},{"text":"颜色"}]}
			$scope.typeTemplate=response;//定义模板对象
			
			//注意:这个时候$scope.typeTemplate虽然是json对象,但是它的属性brandIds只是一个json格式的字符串,因此需要转换
			$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
			
			//通过模板id查询出来的扩展属性格式为:[{"text":"内存大小"},{"text":"颜色"}]
			//需要存储到entity.goodsDesc.customAttributeItems
			if($location.search()['id']==null||$location.search()['id']==undefined){//当添加商品的时候才做这步
				$scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.typeTemplate.customAttributeItems);
			}
		});
		
		//同理当模板id发生变化时需要查询规格列表
		typeTemplateService.findSpecList(newValue).success(
				function(response){
					//注意这里specList的格式:[ {"options":[{"id":98,"optionName":"移动3G"}],"id":27,"text":"网络"},{"options":[{"id":118,"optionName":"16G"}],"id":32,"text":"机身内存"}]
					$scope.specList=response;
				}
		);	
	});
	
	
	
	//页面点击规格选项$scope.entity.goodsDesc.specificationItems存储的就是用户勾选的规格选项列表,关于这段代码的解析参看:规格-保存选择的规格选项.png
	$scope.updateSpecAttribute = function($event,name,value){
		var object= $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems ,'attributeName', name);
		if(object!=null){//集合中已经找到
			if($event.target.checked){//选中
				object.attributeValue.push(value);
			}else{//取消选中
				var index = object.attributeValue.indexOf(value);
				object.attributeValue.splice(index,1);
			}
			//若选项都取消了则移除
			if(object.attributeValue.length==0){
				var index1 = $scope.entity.goodsDesc.specificationItems.indexOf(object);
				$scope.entity.goodsDesc.specificationItems.splice(index1,1);
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	//$scope.entity.goodsDesc.specificationItems保存的就是用户勾选的规格选项的数据
	
	
	//点击规格选项生成SKU列表  参考图片:SKU列表的生成.png
	$scope.createItemList = function(){
		//变量:$scope.entity.itemList就是最终存储SKU列表的变量,该变量是个集合,集合中的每个值最终存储到tb_item表中
		
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'} ];//列表初始化
		
		//SKU列表的生成是根据页面用户选择的规格选项列表来产生的,
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i=0;i<items.length;i++){
			var item = items[i];
			$scope.entity.itemList = addColumn($scope.entity.itemList,item.attributeName,item.attributeValue);
		}
	}
	addColumn=function(list,columnName,columnValues){
		var newList=[];	
		for(var i=0;i<list.length;i++){
			var oldRow = list[i];
			for(j=0;j<columnValues.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));//深度克隆
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	
	
	/**
	 * 商品编辑时,根据商品id查询出商品信息后,让复选框根据我们保存的值进行选中  用到了ng-checked指令
	 * ng-checked如果为true则选中,ng-checked为false则不选中
	 * 
	 * specName:规格名称
	 * optionName:规格选项名称
	 * 
	 * 参看图片:读取规格数据时-规格选项默认选中的问题.png
	 */
	$scope.checkAttributeValue = function(specName,optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		
		var object = $scope.searchObjectByKey(items,'attributeName',specName);
		if(object!=null){
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	
	
});	
