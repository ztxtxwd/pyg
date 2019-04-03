 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService,loginService, uploadService){	
	
	//注册用户
	$scope.reg=function(){
		
		//比较两次输入的密码是否一致
		if($scope.password!=$scope.entity.password){
			alert("两次输入密码不一致，请重新输入");
			$scope.entity.password="";
			$scope.password="";
			return ;			
		}
		//新增
		userService.add($scope.entity,$scope.smscode).success(
			function(response){
				alert(response.message);
			}		
		);
	}
    
	//发送验证码
	$scope.sendCode=function(){
		if($scope.entity.phone==null || $scope.entity.phone==""){
			alert("请填写手机号码");
			return ;
		}
		
		userService.sendCode($scope.entity.phone  ).success(
			function(response){
				alert(response.message);
			}
		);		
	}
	
	$scope.save=function(){	
		
		var serviceObject;//服务层对象  			
		var mon = $scope.month - 1;
		if($scope.year != undefined ){
			$scope.entity.birthday = new Date($scope.year, mon, $scope.day);
		}
		
		if($scope.headPic != undefined){
			$scope.entity.headPic = $scope.headPic;
		}
		$scope.entity.birthday = new Date($scope.year, mon, $scope.day);
		$scope.entity.headPic = $scope.headPic;
		$scope.entity.province = getProById($scope.provinces, $scope.entity.province.provinceid);
		$scope.entity.city = getCityById($scope.cities, $scope.entity.city.cityid);
		$scope.entity.area = getAreaById($scope.areas, $scope.entity.area.areaid);
		serviceObject=userService.update( $scope.entity ); //修改  
						
		serviceObject.success(
			function(response){
				if(response.success){
					 //清空页面数据
					
					/*
					 *不管新增还是修改,完成之后应该跳转到商品列表页面goods.html
					*/
					alert(response.message);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	
	$scope.showName=function(){
		loginService.showName().success(
				function(response){
					$scope.loginName=response.loginName;
					$scope.headPic=response.headPic;
					findByUsername();
					
//					$scope.year = $scope.entity.birthday.split()
				}
		);
	}
	

	findByUsername = function(){
		userService.findByUsername($scope.loginName).success(function(response){
			$scope.entity = response;
			$scope.year = $scope.entity.birthday.split("-")[0];
			$scope.month = $scope.entity.birthday.split("-")[1];
			$scope.day = $scope.entity.birthday.split("-")[2].split(" ")[0];
			
//			alert($scope.year);
//			alert($scope.month);
//			alert($scope.day);
			$scope.entity.province = JSON.parse($scope.entity.province);
			$scope.entity.city = JSON.parse($scope.entity.city);
			$scope.entity.area = JSON.parse($scope.entity.area);
		})
	}
	
	$scope.selectPro = function(){
		userService.selectPro().success(function(response){
			$scope.provinces = response;
		})
	}
	
	$scope.entity={province:{},city:{}};
	$scope.$watch("entity.province.provinceid", function(newValue, oldValue){
		userService.selectCity($scope.entity.province.provinceid).success(function(response){
			$scope.cities = response;
			if(oldValue!=null){
				$scope.entity.city={};
				$scope.entity.area={};
			}
			
			
		})
	})
	
	$scope.$watch("entity.city.cityid", function(newValue, oldValue){
		userService.selectAreas($scope.entity.city.cityid).success(function(response){
			
			$scope.areas = response;
			if(oldValue!=null){
				$scope.entity.area={};
			}
		})
	})
	
	$scope.jobs = ['程序员', '产品经理', 'UI设计师'];
	//上传图片
	$scope.uploadFile = function(){
		uploadService.uploadFile().success(function(response){
			if(response.success){
				//注意如果上传成功返回的数据response.message代表的就是图片的完整路径
				$scope.headPic = response.message;
			}else{
				alert(response.message);
			}
		});
	}
	
	//构建日期下拉列表
	
	
	$scope.createDatePicker=function(){
		$scope.years=[];
		$scope.months=[];
		$scope.days=[];
		var yearNow = new Date().getFullYear();
		for (var i = yearNow; i >= 1900; i--) {
			$scope.years.push(i);
		}
		for (var i = 1; i <13; i++) {
			$scope.months.push(i);
		}
	}
	//监控年修改
	$scope.$watch("year", function(newValue, oldValue){
		if(($scope.year%4==0&&$scope.year%100!=0)||($scope.year%400==0)){
			$scope.isLeapYear=true;
		}else{
			$scope.isLeapYear=false;
		}
		$scope.createDatePicker();
	})
	//监控月修改
	$scope.$watch("month", function(newValue, oldValue){
		var dayCount;
		switch ($scope.month) {
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            dayCount = 31;
            break;
        case 4:
        case 6:
        case 9:
        case 11:
            dayCount = 30;
            break;
        case 2:
            dayCount = 28;
            if ($scope.isLeapYear) {
                dayCount = 29;
            }
            break;
        default:
            break;
		}
		for (var i = 1; i <dayCount+1; i++) {
			$scope.days.push(i);
		}
		$scope.createDatePicker();
	})
	getProById = function(list, id){
		for(var i = 0; i < list.length; i ++){
			if(list[i].provinceid == id){
				return list[i];
			}
		}
	}
	
	getCityById = function(list, id){
		for(var i = 0; i < list.length; i ++){
			if(list[i].cityid == id){
				return list[i];
			}
		}
	}
	
	getAreaById = function(list, id){
		for(var i = 0; i < list.length; i ++){
			if(list[i].areaid == id){
				return list[i];
			}
		}
	}
});	
