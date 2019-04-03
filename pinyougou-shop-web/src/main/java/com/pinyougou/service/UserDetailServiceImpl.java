package com.pinyougou.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

public class UserDetailServiceImpl implements UserDetailsService {
	
	//此处需要注意:该接口的具体实现在远程,已经被抽取成一个服务了,根本不在本工程中因此Autowired肯定注入不进来
	private SellerService sellerService;
	
	//通过配置文件set方法注入
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}



	@Override
	/**
	 * 方法的参数username:登陆页面用户输入的用户名
	 */
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("UserDetailServiceImpl执行了");
		/*Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		return new User(username, "123456", authorities);*/
		
		
		
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		
		//调用远程服务或商家信息
		TbSeller seller = sellerService.findOne(username);
		if(seller!=null){
			//如果商家通过了审核
			if("1".equals(seller.getStatus())){
				System.out.println(username);
				return new User(username, seller.getPassword(), authorities);
			}
		}
		return null;
	}

}
