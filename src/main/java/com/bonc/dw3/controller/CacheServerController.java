package com.bonc.dw3.controller;

import com.bonc.dw3.service.CacheServerService;
import com.bonc.dw3.service.InitService;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public String getResult(String code){
        return cacheServerService.cacheResultMap.get(code);
    }

    /**
     * 直接触发缓存静态方法
     * @return
     */
    @PostMapping("/trigger")
    public String trigger(){
        logger.info("触发成功");
        cacheServerService.getCache();
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