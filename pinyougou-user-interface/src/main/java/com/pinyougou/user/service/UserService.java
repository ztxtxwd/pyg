package com.pinyougou.user.service;
import java.util.List;

import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;
import com.pinyougou.pojo.TbUser;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface UserService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbUser> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbUser user);
	
	
	/**
	 * 修改
	 */
	public void update(TbUser user);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbUser findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbUser user, int pageNum,int pageSize);
	
	
	/**
	 * 发送短信验证码
	 * @param phone
	 */
	public void createSmsCode(String phone);
	
	/**
	 * 校验验证码
	 * @param phone
	 * @param code
	 * @return
	 */
	public boolean checkSmsCode(String phone,String code);
	
	/**
	 * 根据用户名查找用户类
	 * @param username
	 * @return
	 */
	public TbUser findByUsername(String username);
	
	/**
	 * 列出省份
	 * @return
	 */
	public List<TbProvinces> selectPro();
	
	/**
	 * 列出区市
	 * @param proid
	 * @return
	 */
	public List<TbCities> selectCities(String proId);
	
	/**
	 * 列出区县
	 * @param citiId
	 * @return
	 */
	public List<TbAreas> selectAreas(String cityId);
	
}
