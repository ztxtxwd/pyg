package com.pinyougou.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;

@RestController
@RequestMapping("/login")
public class LoginController {

	@Reference
	private UserService userService;
	
	@RequestMapping("/name")
	public Map showName(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		TbUser user = userService.findByUsername(name);
		Map map=new HashMap();
		map.put("loginName", name);
		map.put("headPic", user.getHeadPic());
		return map;		
	}
	
}
