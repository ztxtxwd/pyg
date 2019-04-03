package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//获得组合实体类中的规格实体
		TbSpecification tbSpecification = specification.getSpecification();
		//先保存规格，且会返回保存的规格id
		specificationMapper.insert(tbSpecification);
		//循环保存规格列表
		for(TbSpecificationOption option:specification.getSpecificationOptionList()){
			//给规格选项设置所属的规格id
			option.setSpecId(tbSpecification.getId());
			//调用规格选项的mapper,因此需要注入规格选项的mapper
			specificationOptionMapper.insert(option);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		
		//获得组合实体类中的规格实体
		TbSpecification tbSpecification = specification.getSpecification();
		//先保存规格，且会返回保存的规格id
		specificationMapper.updateByPrimaryKey(tbSpecification);
		
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(tbSpecification.getId()); // where spec_id  = 
		//对于规格列表：删删除原有规格列表，然后重新添加新的规格列表
		specificationOptionMapper.deleteByExample(example );
		
		//循环保存规格列表
		for(TbSpecificationOption option:specification.getSpecificationOptionList()){
			//给规格选项设置所属的规格id
			option.setSpecId(tbSpecification.getId());
			//调用规格选项的mapper,因此需要注入规格选项的mapper
			specificationOptionMapper.insert(option);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//需要返回组合实体类：包含了规格实体和规格选项集合
		Specification specification = new Specification();
		//根据规格id查询规格
		TbSpecification tbSpecification =  specificationMapper.selectByPrimaryKey(id);// select * from sp where id =#{}
		specification.setSpecification(tbSpecification);
		
		//根据规格id去规格选项表查询规格选项列表
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		
		criteria.andSpecIdEqualTo(id);
		
		List<TbSpecificationOption> optonList = specificationOptionMapper.selectByExample(example );
		
		specification.setSpecificationOptionList(optonList);
		
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//删除规格表数据
			specificationMapper.deleteByPrimaryKey(id);
			//删除该规格下对应的规格选项数据
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public List<Map> selectOptionList() {
			return specificationMapper.selectOptionList();
		}
	
}
