package com.pinyougou.manager.controller;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.MSUtils;
//import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
//import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 要注意:这里为什么将新增商品的方法注释掉：因为该工程是运营商管理后台,不涉及商品的添加，商品都是由商家添加的
	 */
	/*@RequestMapping("/add")
	public Result add(@RequestBody TbGoods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}*/
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
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
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	
	@Autowired
	private Destination queueSolrDeleteDestination;
	
	@Autowired
	private Destination topicPageDeleteDestination;
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//从索引库中删除
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			
			//异步调用ActiveMQ 删除索引库中的数据
			JmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);//Long是实现了可序列化接口的
				}
			});
			//删除每个服务器上的商品详细页
			JmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			
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
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	/**
	 * 修改商品状态
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateStatus1")
	public Result updateStatus1(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}		
	}
	
	//@Reference(timeout=100000)
	//private ItemSearchService itemSearchService;
	
	@Autowired
	private JmsTemplate JmsTemplate;
	
	/**
	 * 注意这里名字和配置文件中的保持一致,@Autowired按类型注入,容器中到时候Destination类型的不止一个,
	 * 但是如果这里的变量名和容器中bean的名称保持一致，则autowired可以在按照类型注入的基础之上按照名称注入queueSolrDestination
	 * 
	 * 当然:我们可以直接使用@Resource注解按照名称注入
	 */
	@Autowired 
	private Destination queueSolrDestination;
	
	@Resource(name="topicPageDestination")
	private Destination topicPageDestination;
	/**
	 * 修改商品状态
	 * 然后如果商品状态为：审核通过,则需要将该商品的数据导入到索引库
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			if("1".equals(status)){//如果是审核通过 
				//得到需要导入的SKU列表
				List<TbItem> itemList = goodsService.findItemListByGoodsIdListAndStatus(ids, status);
				//导入到solr
				//itemSearchService.importList(itemList);	
				
				//利用ActiveMQQueue点对点将商品信息导入索引库
				System.out.println("导入到索引库之前查询到的数据:"+itemList);
				final String jsonString = JSON.toJSONString(itemList);//转换为json传输
				System.out.println("导入到索引库之前转换为json:"+jsonString);
				JmsTemplate.send(queueSolrDestination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(jsonString);
					}
				});
				
				//****生成商品详细页
				/*for(Long goodsId:ids){
					itemPageService.genItemHtml(goodsId);
				}*/
				
				//利用ActiveMQTopic发布订阅的方式将商品id传输,然后生成商品详情页
				System.out.println("-----审核时生成商品详情页:得到的topic"+topicPageDestination);
				for(final Long goodsId:ids){
					JmsTemplate.send(topicPageDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(goodsId+"");
						}
					});
				}
				
			}	
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}		
	}
	
	//@Reference(timeout=40000)
	//private ItemPageService itemPageService;
	
	/**
	 * 调用itemPageService生成静态html
	 * @param goodsId
	 */
	/*@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}*/
}
