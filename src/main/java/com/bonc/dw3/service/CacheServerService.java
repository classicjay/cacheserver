package com.bonc.dw3.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

@Service
@CrossOrigin(origins = "*")
public class CacheServerService {
	
	/**
     * 定时任务-只要定时获取筛选条件等数据，不必每次请求都查询数据库
     * fixedRate = 43200000启动执行一次，之后每半天执行一次
     * cron = "0 0 0-23 * * ?" 整点执行
     * 
     *
     */
    @Scheduled(fixedRate = 3600000)
    public void scheduledInit() {
    	//1.查询服务优先级列表，以优先级从高到低排序，排除不可用的服务，返回信息包括ip，端口号，优先级
    	//2.如果最高优先级的ip，端口号与本机一致，开始更新
    		//2.1 与上次更新时间作比较，如果超过1小时，开始更新
    			//2.1.1  获取缓存配置条件，查询数据库，加入缓存中，把缓存内容向其它机器转发
    		//2.2 不超过1小时，不更新
    	//3.如果最高优先级的ip，端口号与本机不一致，通知优先级最高的服务
    		//3.1 如果通知优先级最高的服务成功，完毕
    		//3.2 如果通知失败，则更改失败次数+1，再邀请其它机器通知优先级最高的服务
    			//3.2.1 其它机器通知成功，说明网络存在问题，重试3次把自己从服务列表移除
    			//3.2.2 其它机器通知失败，说明优先级最高的master挂了，
    			//判断如果优先级最高的服务被标记的失败次数超过一半，则标记为服务不可用，重新执行更新缓存操作
    }
}
