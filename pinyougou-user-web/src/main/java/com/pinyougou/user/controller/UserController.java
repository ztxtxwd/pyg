package com.pinyougou.user.controller;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbProvinces;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.PayLogService;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import entity.Result;
import util.PhoneFormatCheckUtils;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	
	@Reference 
	private PayLogService payLogService;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return userService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String smscode){
		
		//校验验证码是否正确
		boolean checkSmsCode = userService.checkSmsCode(user.getPhone(), smscode);
		if(!checkSmsCode){
			return new Result(false, "验证码不正确！");
		}
		
		
		try {
			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbUser findOne(Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbUser user, int page, int rows  ){
		return userService.findPage(user, page, rows);		
	}
	
	@RequestMapping("/sendCode")
	public Result sendCode(String phone){
		
		if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
			return new Result(false, "手机格式不正确");
		}
		
		try {
			userService.createSmsCode(phone);
			return new Result(true, "验证码发送成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "验证码发送失败");
		}
	}
	
	/**
	 * 根据用户名查询
	 * @param username
	 * @return
	 */
	@RequestMapping("/findByUsername")
	public TbUser findByUsername(String username) {
		return userService.findByUsername(username);
	}
	
	@RequestMapping("/selectPro")
	List<TbProvinces> selectPro(){
		return userService.selectPro();
	}
	
	@RequestMapping("/selectCities")
	public List<TbCities> selectCities(String proId){
		return userService.selectCities(proId);
	}
	
	@RequestMapping("/selectAreas")
	public List<TbAreas> selectAreas(String citiId){
		return userService.selectAreas(citiId);
	}
	
	/**
	 * 根据订单id查询支付日志
	 * @param orderId
	 * @return
	 */
	@RequestMapping("/findPayLog")
	public TbPayLog findPayLog(String orderId) {
		 try {
			TbPayLog payLog = payLogService.findPayLog(orderId);
			if(payLog != null) {
				return payLog;
			}else {
				return new TbPayLog();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new TbPayLog();
		}
		 
	}
}
