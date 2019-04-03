package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Autowired
	private TbSellerMapper sellerMapper;
	
	 //代码优化之前请参看该方法,优化之后只是将一些公共的代码单独提出去封装成方法了 
	public void add1(Goods goods) {
		goods.getGoods().setAuditStatus("0");//状态：未审核
		goodsMapper.insert(goods.getGoods());//插入商品基本信息,注意:需要在对应的mapper文件中添加返回主键字段的配置
		
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//将商品基本表的ID给商品扩展表
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展表数据
		
		
		//保存SKU列表
		for(TbItem item:   goods.getItemList()){
			//构建标题  SPU名称+ 规格选项值
			String title=goods.getGoods().getGoodsName();//SPU名称
			Map<String,Object> map=  JSON.parseObject(item.getSpec());
			for(String key:map.keySet()) {
				title+=" "+map.get(key);
			}
			item.setTitle(title);
			
			//商品分类 
			item.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
			item.setCreateTime(new Date());//创建日期
			item.setUpdateTime(new Date());//更新日期 
			
			item.setGoodsId(goods.getGoods().getId());//商品ID
			item.setSellerId(goods.getGoods().getSellerId());//商家ID
			
			//分类名称			
			TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
			item.setCategory(itemCat.getName());
			//品牌名称
			TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
			item.setBrand(brand.getName());
			//商家名称(店铺名称)			
			TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
			item.setSeller(seller.getNickName());
			
			//图片
			List<Map> imageList = JSON.parseArray( goods.getGoodsDesc().getItemImages(), Map.class) ;
			if(imageList.size()>0){
				item.setImage( (String)imageList.get(0).get("url"));
			}else{
				item.setImage("");
			}
			
			itemMapper.insert(item);
		}
	}
	
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		
		goods.getGoods().setAuditStatus("0");//状态：未审核
		goodsMapper.insert(goods.getGoods());//插入商品基本信息
		
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//将商品基本表的ID给商品扩展表
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展表数据
		
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			for(TbItem item:   goods.getItemList()){
				//构建标题  SPU名称+ 规格选项值
				String title=goods.getGoods().getGoodsName();//SPU名称
				Map<String,Object> map=  JSON.parseObject(item.getSpec());
				for(String key:map.keySet()) {
					title+=" "+map.get(key);
				}
				item.setTitle(title);
				
				setItemValues(item,goods);
				
				itemMapper.insert(item);
			}
		}else{//没有启用规格			
			
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//标题
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setNum(99999);//库存数量
			item.setStatus("1");//状态1-正常，2-下架，3-删除
			item.setIsDefault("1");//默认
			item.setSpec("{}");//规格
			
			setItemValues(item,goods);
			
			itemMapper.insert(item);
		}
	}
	private void setItemValues(TbItem item,Goods goods){
		//商品分类 
		item.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//更新日期 
		
		item.setGoodsId(goods.getGoods().getId());//商品ID
		item.setSellerId(goods.getGoods().getSellerId());//商家ID
		
		//分类名称			
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//商家名称(店铺名称)			
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		
		//图片
		List<Map> imageList = JSON.parseArray( goods.getGoodsDesc().getItemImages(), Map.class) ;
		if(imageList.size()>0){
			item.setImage( (String)imageList.get(0).get("url"));
		}else{
			item.setImage("");
		}
		
	}
	/**
	 * 保存SKU列表的代码在商品的新增和商品的修改都是通用的,因此单独抽取出来直接调用
	 */
	@SuppressWarnings("unused")
	private void saveItem(Goods goods){
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			for(TbItem item:   goods.getItemList()){
				//构建标题  SPU名称+ 规格选项值
				String title=goods.getGoods().getGoodsName();//SPU名称
				Map<String,Object> map=  JSON.parseObject(item.getSpec());
				for(String key:map.keySet()) {
					title+=" "+map.get(key);
				}
				item.setTitle(title);
				
				setItemValues(item,goods);
				
				itemMapper.insert(item);
			}
		}else{//没有启用规格			
			
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//标题
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setNum(99999);//库存数量
			item.setStatus("1");//状态
			item.setIsDefault("1");//默认
			item.setSpec("{}");//规格
			
			setItemValues(item,goods);
			
			itemMapper.insert(item);
		}
	}
	/**
	 * 商品的修改
	 */
	@Override
	public void update(Goods goods){
		//先根据主键更新tb_goods表的数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//然后根据主键更新tb_goods_desc表的数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		
		//更新SKU列表的数据 tb_item表,更新的整体思路是:先删除原有的旧的SKU列表,再插入新的SKU列表
		
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		//根据该商品的ID删除SKU列表
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example );
		
		//然后再插入新的SKU列表,但是由于保存SKU列表的操作和新增时一样,因此可以将保存SKU列表的代码抽取出来直接调用
		saveItem(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//根据主键id查询商品基本信息 tb_goods
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		
		//根据主键id查询商品详情tb_goods_desc
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		//读取SKU列表
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example );
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//这里的删除并不是物理删除而是逻辑删除,逻辑删除就是用一个字段表明是删除还是未删除
		for(Long id:ids){			
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");//表示逻辑删除
			goodsMapper.updateByPrimaryKey(goods);
		}	
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		criteria.andIsDeleteIsNull();//指定条件为未逻辑删除记录    where is_delete = null
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				//注意这里:根据商家id进行条件查询不应该是模糊匹配而是精确匹配  123  1234
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
		/**
		 * 修改商品状态
		 */
	@Override
	public void updateStatus(Long[] ids, String status) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}

	/**
	 * 根据SPU的ID集合查询SKU列表
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	public List<TbItem>	findItemListByGoodsIdListAndStatus(Long []goodsIds,String status){
		/**
		 * 此处代码逻辑有问题:itemMapper是根据goodsId去tb_item表中查询SKU列表,且商品是审核通过的
		 * 而商品是否审核通过的状态是在tb_goods表中的audit_status字段表明的,tb_item表中的status字段并不是审核通过的意思
		 * 以前商品审核通过时修改商品状态也是修改的tb_goods中的audit_status字段
		 */
		
		//先根据SPU 的id也就是商品id以及状态去商品表中查询出符合要求的商品id,然后在根据这些符合条件的商品id去查询对应的SKU列表
		//此处的查询我们就不使用mybatis逆向生成的代码了,同时也为了复习mybatis的一些操作:动态sql语句
		/*List<Long> ids = goodsMapper.findGoodsIdByStatus(goodsIds, status);
		
		TbItemExample example=new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(ids);//指定条件：SPUID集合
		return itemMapper.selectByExample(example);*/
		
		
		return itemMapper.findItemListByGoodsIdAndStatus(goodsIds, status);
		
		
		/*TbItemExample example=new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);//状态
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));//指定条件：SPUID集合
		return itemMapper.selectByExample(example);*/
	}
}
