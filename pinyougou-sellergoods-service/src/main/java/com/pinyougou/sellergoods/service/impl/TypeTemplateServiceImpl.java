package com.pinyougou.sellergoods.service.impl;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);	
		
		//缓存处理
		saveToRedis();
		return new PageResult(page.getTotal(), page.getResult());
	}
		
		@Autowired
		private RedisTemplate redisTemplate;
		/**
		 * 将品牌列表与规格列表放入缓存
		 */
		private void saveToRedis(){
			List<TbTypeTemplate> templateList = findAll();
			for(TbTypeTemplate template:templateList){
				//得到品牌列表
				List brandList= JSON.parseArray(template.getBrandIds(), Map.class) ;
				redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);
				//得到规格列表
				List<Map> specList = findSpecList(template.getId());//[{"id":27,"text":"网络","options":[{}]},{"id":32,"text":"机身内存"}]
				redisTemplate.boundHashOps("specList").put(template.getId(), specList);
				
			}
			System.out.println("缓存品牌和规格列表");
		}
		
		
		
		@Autowired
		private TbSpecificationOptionMapper tbSpecificationOptionMapper;
		
		/**
		 * 商品添加页面需要展示规格数据,不仅包括规格名称还需显示规格选项数据
		 * 模板表中存储了规格的id和规格名称，但是未存规格选项，因此还需要根据规格的id去规格选项表中查询规格选项然后一起返回
		 * 详细的数据结构介绍请看图：规格显示后台逻辑分析.png
		 */
		@Override
		public List<Map> findSpecList(Long id) {//id是模板id
			TbTypeTemplate selectByPrimaryKey = typeTemplateMapper.selectByPrimaryKey(id);
			String specIds = selectByPrimaryKey.getSpecIds();//得到了模板表中存储的规格数据
			//specIds的格式为:[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}] 其中id为规格id text为规格名称
			List<Map> list = JSON.parseArray(specIds, Map.class)  ;//注意导fastjson的包
			//缺乏规格选项的数据
			for (Map map : list) {
				//根据规格id去规格选项表tb_specification_option中查询规格选项数据
				
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
				createCriteria.andSpecIdEqualTo(new Long((Integer)map.get("id")));
				List<TbSpecificationOption> options = tbSpecificationOptionMapper.selectByExample(example );
				
				map.put("options", options);
			}
			return list;
		}
	
}
