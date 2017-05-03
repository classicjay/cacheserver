package com.bonc.dw3.controller;

import com.bonc.dw3.service.CacheServerService;
import com.bonc.dw3.service.InitService;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.bonc.dw3.service.CacheServerService.cacheResultMap;
import static com.bonc.dw3.service.CacheServerService.dateFormat;
import static com.bonc.dw3.service.CacheServerService.updateTime;
import static com.bonc.dw3.utils.TimeUtil.hourBetweenTimes;
/**
 * <p>Title: BONC -  CacheServerController</p>
 * <p>Description: 缓存服务的controller </p>
 * <p>Copyright: Copyright BONC(c) 2013 - 2025 </p>
 * <p>Company: 北京东方国信科技股份有限公司 </p>
 *
 * @author zhaojie
 * @version 1.0.0
 */
@Api(value = "缓存服务", description = "cacheserver")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/CacheServer")
public class CacheServerController {

    @Autowired
    CacheServerService cacheServerService;

    @Autowired
    InitService initService;


    private static Logger logger = Logger.getLogger(CacheServerController.class);

    @PostMapping("/result")
    public String getResult(@RequestBody String code){
        logger.info("参数code为："+code);
        String str = cacheResultMap.get(code);
        logger.info("返回值result为："+str);
        return str;
    }

    /**
     * 直接触发缓存方法
     * @return
     */
    @PostMapping("/trigger")
    public String trigger(){

        String currenTime = dateFormat.format(new Date()).toString();
        boolean flag = hourBetweenTimes(currenTime,updateTime);
        if (flag){//时间间隔大于1小时，开始更新
            cacheServerService.getCache();
            logger.info("上次更新时间为："+updateTime+"，距离上次更新缓存已超过1小时，触发成功");
        }else {//时间间隔不足1小时
            logger.info("上次更新时间为："+updateTime+"，距离上次更新缓存不足1小时，不触发缓存");
        }
//        cacheServerService.getCache();
        return "succeed";
    }

    /**
     * 通知优先级最高的机器触发缓存
     * @return
     */
    @PostMapping("/notice")
    public String notice(){
        RestTemplate ipRestTemplate = new RestTemplate();
        String noticeRs = new String();
        List<HashMap<String,String>> serverList = new ArrayList<>();
        serverList = initService.getServerList();
        HashMap<String,String> mainServer = new HashMap<>();
        if (null != serverList && !serverList.isEmpty()){
            mainServer = serverList.get(0);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept","*/*");
        HttpEntity formEntity = new HttpEntity(httpHeaders);
        String triggerRs = null;
        try {
            triggerRs = ipRestTemplate.postForObject("http://"+mainServer.get("IP_ADDRESS")+":"+mainServer.get("PORT")+"/CacheServer/trigger",formEntity,String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            triggerRs = "notice succeed and trigger failed";
            noticeRs = triggerRs;
        }
        return noticeRs;
    }
}