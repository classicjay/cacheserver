package com.bonc.dw3.common;

import com.alibaba.druid.support.json.JSONUtils;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClientHelper {

	@Autowired
	RestTemplate client;

	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") }, 
			fallbackMethod = "serviceFallback")
	public Object doGet(String url) {
		String str = client.getForEntity(url, String.class).getBody();
		List<Map<String, Object>> list = null;
		try {
			list = (List<Map<String, Object>>) JSONUtils.parse(str);
		} catch (Exception e) {
			System.out.println("can't parse to list");
		}
		if (list == null) {
			return str;
		} else {
			return list;
		}
	}

	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") }, 
			fallbackMethod = "serviceFallback")
	public Object doPost(String url) {

		String str = client.postForEntity(url, null,String.class).getBody();
		List<Map<String, Object>> list = null;
		try {
			list = (List<Map<String, Object>>) JSONUtils.parse(str);
		} catch (Exception e) {
			System.out.println("can't parse to list");
		}
		if (list == null) {
			return str;
		} else {
			return list;
		}
	}

//	@HystrixCommand(commandProperties = {
//			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") },
//			fallbackMethod = "serviceFallback")
//	public Object doPost(String url, HashMap<String, String> param) {
//		String str = client.postForEntity(url, param, String.class).getBody();
//		List<HashMap<String, Object>> list = null;
//		try {
//			list = (List<HashMap<String, Object>>) JSONUtils.parse(str);
//		} catch (Exception e) {
//			System.out.println("can't parse to list");
//		}
//		if (list == null) {
//			return str;
//		} else {
//			return list;
//		}
//	}

	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") },
			fallbackMethod = "serviceFallback")
	public Object doPost(String url, HashMap<String, Object> param) {
		String str = client.postForEntity(url, param, String.class).getBody();
		List<HashMap<String, Object>> list = null;
		try {
			list = (List<HashMap<String, Object>>) JSONUtils.parse(str);
		} catch (Exception e) {
			System.out.println("can't parse to list");
		}
		if (list == null) {
			return str;
		} else {
			return list;
		}
	}

	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") },
			fallbackMethod = "serviceFallback")
	public Object doPost(String url, String param) {
		String str = client.postForEntity(url, param, String.class).getBody();
		List<HashMap<String, Object>> list = null;
		try {
			list = (List<HashMap<String, Object>>) JSONUtils.parse(str);
		} catch (Exception e) {
			System.out.println("can't parse to list");
		}
		if (list == null) {
			return str;
		} else {
			return list;
		}
	}

//	@HystrixCommand(commandProperties = {
//			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") },
//			fallbackMethod = "serviceFallback")
//	public Object doPost(String url,HashMap<String,Object> param){
//		String s = client.postForEntity(url,param,String.class).getBody();
//		List<Map<String, Object>> list = null;
//		try {
//			list = (List<Map<String, Object>>) JSONUtils.parse(s);
//		} catch (Exception e) {
//			System.out.println("can't parse to list");
//		}
//		if (list == null) {
//			return s;
//		} else {
//			return list;
//		}
//
//	}


	public Object serviceFallback(String url) {
		System.out.println(url + " 调用失败");
		return "error";

	}
	public Object serviceFallback(String url,HashMap<String, String> param) {
		System.out.println(url + " 调用失败");
		return "error";

	}
}
