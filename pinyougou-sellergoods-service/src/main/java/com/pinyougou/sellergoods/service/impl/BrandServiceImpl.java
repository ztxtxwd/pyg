package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service  //Service注解千万别写错
@Transactional
public class BrandServiceImpl implements BrandService {
	
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {
		return brandMapper.selectByExample(null);//没有条件则查询所有,返回列表
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);//mybatis分页查询
		
		Page<TbBrand> page = (Page<TbBrand>)brandMapper.selectByExample(null);
		page.getPages();//总页数
		page.getPageSize();//每页大小
		return  new PageResult(page.getTotal(),page.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		brandMapper.insert(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id); // select name,firstChar from tb_brand where id = #{}
	}

	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);//update tb_brand set name = #{} where id =#{}
	}

	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id); //  delete from tb_brand wher id =#{}
		}
	}

	@Override
	public PageResult search(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);//mybatis分页查询
		
		TbBrandExample example = new TbBrandExample();
		
		
		//这里的Criteria一定要注意:包不能导错了,mybatis逆向生成的每个pojo都对应的有个Criteria
		Criteria criteria = example.createCriteria();
		if(brand!=null){
			
			//criteria.andFirstCharEqualTo("C"); // where  firstChar = "c"
			// criteria.andFirstCharIn(values) ;//  firstChar in ('c','d' )
			//criteria.andFirstCharIsNull(); // firstChar is null 
			if(brand.getName()!=null&&brand.getName().length()>0){
				criteria.andNameLike("%"+brand.getName()+"%");  // and name like  '%张三%'
			}
			if(brand.getFirstChar()!=null&&brand.getFirstChar().length()>0){
				criteria.andFirstCharLike(brand.getFirstChar()); // and fistChar like  ''
			}
		}
		Page<TbBrand> page = (Page<TbBrand>)brandMapper.selectByExample(example );
		
		return  new PageResult(page.getTotal(),page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}

}
