package com.bonc.dw3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@CrossOrigin(origins ="*")
@EnableAspectJAutoProxy
//用于发现创建的定时任务
@EnableScheduling
//@EnableCircuitBreaker
//ysl 修改，很多注解重复，且使用ribbon或feign才能使用断路由功能。
//extends SpringBootServletInitializer
public class CacheServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CacheServerApplication.class, args);
	}

}
