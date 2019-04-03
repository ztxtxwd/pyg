app.service('uploadService',function($http){
	
	//上传图片
	
	this.uploadFile = function(){
		//创建表单数据对象
		/**
		 * FormData:主要用于发送表单数据,但也可以独立使用于传输key-value数据。与普通的Ajax相比，它能异步上传二进制文件
		 * 但要注意:由于 FormData html新特性,不是所有的浏览器都支持
		 * 异步上传文件时:
		 * 	1,创建FormData对象,直接new
		 * 		var formdata=new FormData();
		 * 	2,调用它的append()方法来添加字段key-value
		 */
		var formdata=new FormData();
		// IE: 6  7 
		// key-value  name=adfa&password=23432
		// HTML 文件类型input
		//第一个参数：file往后台提交时的名字,跟controller中方法的形参保持一致
		//第二个参数:file.files[0]    经测试file指的是<input type="file" id="file">中的id属性
		formdata.append('file',file.files[0]);
		return $http({
			url:'../upload.do',
			method:'post',
			data:formdata,//数据就是刚刚由formdata组装好的数据
			headers:{ 'Content-Type':undefined },// application/json
			transformRequest: angular.identity	
		});
	}
});