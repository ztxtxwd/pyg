package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;
/**
 * 品牌接口
 * @author tanshuai
 *
 */
public interface BrandService {
	
	/**
	 * 查询所有品牌列表
	 * @return
	 */
	List<TbBrand> findAll();
	/**
	 * 品牌分页查询
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	PageResult findPage(int pageNum,int pageSize);
	
	/**
	 * 添加品牌
	 * @param brand
	 */
	public void add(TbBrand brand);
	
	/**
	 * 根据id查询品牌信息
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 更新品牌信息
	 * @param brand
	 */
	public void update(TbBrand brand);
	/**
	 * 删除品牌
	 * @param ids
	 */
	public void delete(Long[] ids);
	
	/**
	 * 按照条件查询
	 * @param brand
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult search(TbBrand brand,int pageNum,int pageSize);
	
	/**
     * 查询品牌列表:select2需要返回[{id:"",text:""},{id:"",text:""}]类型的数据
     * @return
     */
    List<Map> selectOptionList();
}
