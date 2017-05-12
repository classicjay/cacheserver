package com.bonc.dw3.service;


import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.bonc.dw3.utils.TimeUtil.hourBetweenTimes;
/**
 * <p>Title: BONC -  CacheServerService</p>
 * <p>Description: 缓存服务的service </p>
 * <p>Copyright: Copyright BONC(c) 2013 - 2025 </p>
 * <p>Company: 北京东方国信科技股份有限公司 </p>
 *
 * @author zhaojie
 * @version 1.0.0
 */
@Service
@CrossOrigin(origins = "*")
public class CacheServerService {

    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private InitService initService;

    /**
     * kpicode出账用户
     */
    public static String kpiDefault;//"CKP_25101"
    /**
     * 入网月起始时间
     */
    public static String entryStartDefault;//"201610"
    /**
     * 入网月终止时间
     */
    public static String entryEndDefault;
    /**
     * 观察月起始时间
     */
    public static String viewStartDefault;
    /**
     * 观察月终止时间
     */
    public static String viewEndDefault;
    /**
     * 入网月最大最小账期数据缓存
     */
    public static List<HashMap<String,String>> dateEntryList = new ArrayList<>();
    /**
     * 观察月最大最小账期数据缓存
     */
    public static List<HashMap<String,String>> dateViewList = new ArrayList<>();

    /**
     * 用于保存缓存结果的Map
     */
    public static HashMap<String,String> cacheResultMap = new HashMap<>();
    /**
     * 用于保存缓存接口的参数配置
     */
    public static List<HashMap<String,String>> initAllParam;

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 上次缓存数据更新时间
     */
    public static String updateTime = "2017-05-01 09:00:00";
    /**
     * 累计触发失败次数
     */
    public static int triggerFailTime;
    /**
     * 累计通知失败次数
     */
    public static int noticeFailTime;

    private static Logger logger = Logger.getLogger(CacheServerService.class);

    /**
     * 定时任务-只要定时获取筛选条件等数据，不必每次请求都查询数据库
     * fixedRate = 43200000启动执行一次，之后每半天执行一次
     * cron = "0 0 0-23 * * ?" 整点执行
     * 1.查询服务优先级列表，以优先级从高到低排序，排除不可用的服务，返回信息包括ip，端口号，优先级
     * 2.如果最高优先级的ip，端口号与本机一致，开始更新
     * 2.1 与上次更新时间作比较，如果超过1小时，开始更新，
     * 2.1.1  获取缓存配置条件，查询数据库，加入缓存中，把缓存内容向其它机器转发
     * 2.2 不超过1小时，不更新
     * 3.如果最高优先级的ip，端口号与本机不一致，通知优先级最高的服务
     * 3.1 如果通知优先级最高的服务成功，完毕
     * 3.2 如果通知失败，则更改失败次数+1，再邀请其它机器通知优先级最高的服务
     * 3.2.1 其它机器通知成功，说明网络存在问题，重试3次把自己从服务列表移除
     * 3.2.2 其它机器通知失败，说明优先级最高的master挂了，
     * 判断如果优先级最高的服务被标记的失败次数超过一半，则标记为服务不可用，重新执行更新缓存操作
     *
     */


//    @Scheduled(cron = "0 0 0-23 * * ?")//整点执行定时任务
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void scheduledInit() {
        RestTemplate ipRestTemplate = new RestTemplate();
        triggerFailTime = 0;
        noticeFailTime = 0;
        List<HashMap<String,String>> serverList = new ArrayList<>();
        serverList = initService.getServerList();
        HashMap<String,String> mainServer = new HashMap<>();
        if (null != serverList && !serverList.isEmpty()){
            mainServer = serverList.get(0);
        }
        InetAddress inetAddress = null;
        String localIp = new String();
        String localPort = env.getProperty("server.port");
        try{//获取本机ip
            inetAddress = inetAddress.getLocalHost();
            localIp = inetAddress.getHostAddress();//获取ip和端口
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("本机IP："+localIp);
        logger.info("本机端口："+localPort);
        if (null != mainServer && localIp.equals(mainServer.get("IP_ADDRESS")) && localPort.equals(mainServer.get("PORT"))){
            //如果本机IP端口号和最高优先级对应IP端口号相同
            //获取当前时间
            String currenTime = dateFormat.format(new Date()).toString();
            boolean flag = hourBetweenTimes(updateTime,currenTime);
            if (flag){//时间间隔大于1小时，开始更新
                logger.info("当前本服务优先级最高，上次更新时间为："+updateTime+"，当前时间为："+currenTime+"，距离上次更新时间超过1小时，进行缓存");
                getCache();
            }else {//时间间隔不足1小时
                logger.info("当前本服务优先级最高，上次更新时间为："+updateTime+"，当前时间为："+currenTime+"，距离上次更新时间不足1小时，不进行缓存");
            }
        }else {//ip不一致，通知优先级最高的服务
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Accept","*/*");
            HttpEntity formEntity = new HttpEntity(httpHeaders);
            String triggerRs = null;
            try {
                triggerRs = ipRestTemplate.postForObject("http://"+mainServer.get("IP_ADDRESS")+":"+mainServer.get("PORT")+"/CacheServer/trigger",formEntity,String.class);
            } catch (RestClientException e) {
                e.printStackTrace();
                triggerRs = "failed";
            }
            if (("failed").equals(triggerRs)){//通知失败
//                int failTime = Integer.parseInt(mainServer.get("FAIL_TIME"));
//                initService.setFailTime(String.valueOf(failTime+1));
                triggerFailTime++;
                //邀请其他机器通知优先级最高的服务
                for (int i = 1;i < serverList.size();i++){
                    String noticeRs = null;
                    try {
                        noticeRs = ipRestTemplate.postForObject("http://"+serverList.get(i).get("IP_ADDRESS")+":"+serverList.get(i).get("PORT")+"/CacheServer/notice",formEntity,String.class);
                    } catch (RestClientException e) {
                        e.printStackTrace();
                        noticeRs = "notice failed";
                        logger.info("ip"+serverList.get(i).get("IP_ADDRESS")+"通知失败");
                    }
                    if (("notice succeed and trigger failed").equals(noticeRs)){
                        triggerFailTime++;
                    }else if (("notice failed").equals(noticeRs)){
                        noticeFailTime++;
                    }
                }
                if (noticeFailTime >= serverList.size()/2 +1){
                    logger.info("本机和其它服务通信出现问题");
                    HashMap<String,String> localInfo = new HashMap<>();
                    localInfo.put("ipAddress",localIp);
                    localInfo.put("port",localPort);
                    initService.setServerNotAvailable(localInfo);
                }
                if (triggerFailTime >= serverList.size()/2 +1){
                    logger.info("优先级最高的服务出现问题");
                    HashMap<String,String> mainInfo = new HashMap<>();
                    mainInfo.put("ipAddress",mainServer.get("IP_ADDRESS"));
                    mainInfo.put("port",mainServer.get("PORT"));
                    initService.setServerNotAvailable(mainInfo);
                }
            }else {//通知成功，完毕
                logger.info("触发最高优先级服务缓存成功");
            }
        }
    }


    /**
     * 获取缓存数据,结果都存放在cacheResultMap里
     */
    public void getCache() {
        logger.info("正在执行缓存......");
        updateTime = dateFormat.format(new Date()).toString();
        initAllParam = initService.getAllInitParam();
        //入网月最大最小账期默认数据
        dateEntryList = initService.getMinMaxDateEntry();
        //观察月最大最小账期默认数据
        dateViewList = initService.getMinMaxDateView();
        getStartEndDefaultValues(dateEntryList,dateViewList);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("cacheType","cache");
        httpHeaders.add("Accept","*/*");

        RestTemplate ipRestTemplate = new RestTemplate();
        String result = null;
        for (HashMap<String,String> map:initAllParam){
            if ("-".equals(map.get("PARAM_VALUES"))){//地域、筛选条件、指标列表
                HttpEntity formEntity = new HttpEntity(httpHeaders);
                try {
                    result = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
                } catch (RestClientException | IllegalStateException e) {
                    e.printStackTrace();
                    logger.info("访问"+map.get("URL")+"时出错");
                }
            }else {
                if (map.get("CODE").equals("code_newuser_datatable")){//
                    String[] paramValues = new String[256];
                    if (map.get("PARAM_VALUES") != null){
                        paramValues = map.get("PARAM_VALUES").split(",");
                    }
                    List dataTableVal = new ArrayList(Arrays.asList(paramValues));
                    kpiDefault = (String) dataTableVal.get(3);
                    HashMap<String, Object> paramMap = getDataTableParamMap();
                    HttpEntity<HashMap<String,Object>> formEntity = new HttpEntity<HashMap<String, Object>>(paramMap,httpHeaders);
                    try {
                        result = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
                    } catch (RestClientException | IllegalStateException e) {
                        e.printStackTrace();
                        logger.info("访问"+map.get("URL")+"时出错");
                    }
                }else if (map.get("CODE").equals("code_newuser_date_view")){//入网月、观察月
                    JSONArray viewArr = JSONArray.fromObject(dateViewList);
                    result = viewArr.toString();
                }else if (map.get("CODE").equals("code_newuser_date_entry")){
                    JSONArray entryArr = JSONArray.fromObject(dateEntryList);
                    result = entryArr.toString();
                }else {
                    MultiValueMap<String,Object> valueMap = new LinkedMultiValueMap<>();
                    String[] paramValues = new String[256];
                    String[] keyValues = new String[256];
                    if (map.get("PARAM_VALUES") != null){
                        paramValues = map.get("PARAM_VALUES").split(",");
                    }
                    if (map.get("PARAM_KEY") != null){
                        keyValues = map.get("PARAM_KEY").split(",");
                    }
                    for (int i=0 ;i<paramValues.length;i++){
                        valueMap.add(keyValues[i],paramValues[i]);
                    }
                    HttpEntity<MultiValueMap<String,Object>> formEntity = new HttpEntity<MultiValueMap<String,Object>>(valueMap,httpHeaders);
                    try {
                        result = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
                    } catch (RestClientException | IllegalStateException e) {
                        e.printStackTrace();
                        logger.info("访问"+map.get("URL")+"时出错");
                    }
//                    HttpEntity<String> formEntity = new HttpEntity<>(map.get("PARAM_VALUES"),headers);
//                    String result = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
//                    cacheResultMap.put(map.get("CODE"),result);
                }
            }
            if (!StringUtils.isEmpty(result)){
                result.replaceAll(" ","");
                result.replaceAll("[\\n\\r]*","");
                cacheResultMap.put(map.get("CODE"),result);
                logger.info("CODE为"+map.get("CODE")+"数据存入缓存");
            }
        }
        //将缓存内容转发
        List<HashMap<String,String>> serverList = new ArrayList<>();
        serverList = initService.getServerList();
        HttpEntity<HashMap<String,String>> formEntity = new HttpEntity<>(cacheResultMap,httpHeaders);
        for (int i=1;i<serverList.size();i++){
            if (null != serverList && !serverList.isEmpty()){
                try {
                    ipRestTemplate.postForObject("http://"+serverList.get(i).get("IP_ADDRESS")+":"+serverList.get(i).get("PORT")+"/CacheServer/receiveCache",formEntity,String.class);
                } catch (RestClientException e) {
                    e.printStackTrace();
                    logger.info("访问IP为"+serverList.get(i).get("IP_ADDRESS")+"端口号"+serverList.get(i).get("PORT")+"时出错");
                }
            }
        }
        logger.info("转发完毕");
    }


    /**
     * 给默认起止日期entryStartDefault、entryEndDefault、viewStartDefault、viewEndDefault赋值
     */
    private static void getStartEndDefaultValues(List<HashMap<String, String>> minMaxDateListEntry,
                                                 List<HashMap<String, String>> minMaxDateListView) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        GregorianCalendar gc = new GregorianCalendar();
        String endEntry = minMaxDateListEntry.get(0).get("MAXDATE").replace("-", "");
        String endView = minMaxDateListView.get(0).get("MAXDATE").replace("-", "");
        try {
            //入网月起（最大账期往前2个月）止（最大账期）时间默认值
            Date endEntryDate = format.parse(endEntry);
            gc.setTime(endEntryDate);
            gc.add(2, -5);
            Date startEntryDate = gc.getTime();
            entryStartDefault = format.format(startEntryDate);
            entryEndDefault = endEntry;
            //观察月起（最大账期往前2个月）止（最大账期）时间默认值
            Date endViewDate = format.parse(endView);
            gc.setTime(endViewDate);
            gc.add(2, -5);
            Date startViewDate = gc.getTime();
            viewStartDefault = format.format(startViewDate);
            viewEndDefault = endView;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * 得到表格接口默认的参数列表
     */
    public static HashMap<String, Object> getDataTableParamMap() {
        HashMap<String, Object> paramMap = new HashMap<>();
        HashMap<String,Object> entryDate = new HashMap<>();
        entryDate.put("startDate",entryStartDefault);
        entryDate.put("endDate",entryEndDefault);
        HashMap<String,Object> viewDate = new HashMap<>();
        viewDate.put("startDate",viewStartDefault);
        viewDate.put("endDate",viewEndDefault);
        List<String> idList = new ArrayList<>();
        idList.add("-1");
        paramMap.put("provId", "-1");
        paramMap.put("clientId", "-1");
        paramMap.put("channelId", idList);
        paramMap.put("contractId", idList);
        paramMap.put("networkId", idList);
        paramMap.put("terminalId", idList);
        paramMap.put("incomeId", idList);
        paramMap.put("productId", idList);
        paramMap.put("entryDate", entryDate);
        paramMap.put("viewDate", viewDate);
        paramMap.put("kpi", kpiDefault);
        return paramMap;
    }

}
