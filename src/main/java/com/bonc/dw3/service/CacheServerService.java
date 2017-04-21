package com.bonc.dw3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
     * 表格数据缓存
     */
    public static String dataMap = new String();

    public static String currentTime = "";

    /**
     * 用于保存缓存结果的Map
     */
    public static HashMap<String,String> cacheResultMap = new HashMap<>();
    /**
     * 用于保存缓存接口的参数配置
     */
    public static List<HashMap<String,String>> initAllParam;

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
    public static String updateTime = dateFormat.format(new Date()).toString();
    public static int triggerFailTime;
    public static int noticeFailTime;

    /**
     * 定时任务-只要定时获取筛选条件等数据，不必每次请求都查询数据库
     * fixedRate = 43200000启动执行一次，之后每半天执行一次
     * cron = "0 0 0-23 * * ?" 整点执行
     * 1.查询服务优先级列表，以优先级从高到低排序，排除不可用的服务，返回信息包括ip，端口号，优先级
     * 2.如果最高优先级的ip，端口号与本机一致，开始更新
     * 2.1 与上次更新时间作比较，如果超过1小时，开始更新
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
    @Scheduled(fixedRate = 300000)
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

        if (null != mainServer && localIp.equals(mainServer.get("IP_ADDRESS")) && localPort.equals(mainServer.get("PORT"))){ //如果ip端口和最高优先级对应ip端口相同
            //获取当前时间
            String currenTime = dateFormat.format(new Date()).toString();
            boolean flag = hourBetweenTimes(currenTime,updateTime);
            if (flag){//时间间隔大于1小时，开始更新
                getCache();
                updateTime = dateFormat.format(new Date()).toString();
            }else {//时间间隔不足1小时

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
                        noticeRs = "failed";
                        System.out.println("ip"+serverList.get(i).get("IP_ADDRESS")+"通知失败");
                    }
                    if (("notice succeed and trigger failed").equals(noticeRs)){
                        triggerFailTime++;
                    }else if (("notice failed").equals(noticeRs)){
                        noticeFailTime++;
                    }
                }
                if (noticeFailTime >= serverList.size()/2 +1){
                    System.out.println("本机和其余机器通信出现问题");
                    HashMap<String,String> localInfo = new HashMap<>();
                    localInfo.put("ipAddress",localIp);
                    localInfo.put("port",localPort);
                    initService.setServerNotAvailable(localInfo);
                }
                if (triggerFailTime >= serverList.size()/2 +1){
                    System.out.println("优先级最高的服务出现问题");
                    HashMap<String,String> mainInfo = new HashMap<>();
                    mainInfo.put("ipAddress",mainServer.get("IP_ADDRESS"));
                    mainInfo.put("port",mainServer.get("PORT"));
                    initService.setServerNotAvailable(mainInfo);
                }
            }else {//通知成功，完毕
                System.out.println("触发成功");
            }
        }
    }

    /**
     * 计算当前时间和上次更新时间间隔
     * @param startTime
     * @param endTime
     * @return
     */
    public static boolean hourBetweenTimes(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        Calendar cal = Calendar.getInstance();
        long earlyTime = 0;
        long lateTime = 0;
        try{
            cal.setTime(sdf.parse(startTime));
            earlyTime = cal.getTimeInMillis();
            cal.setTime(sdf.parse(endTime));
            lateTime = cal.getTimeInMillis();
        }catch(Exception e){
            e.printStackTrace();
        }
        long betweenHours=(lateTime-earlyTime)/(1000*3600);
        int hours = Integer.parseInt(String.valueOf(betweenHours));
        if (hours > 1){
            return true;
        }else {
            return false;
        }

    }

    /**
     * 获取缓存数据,结果都存放在cacheResultMap里
     */
    public void getCache() {
        initAllParam = initService.getAllInitParam();
        System.out.println("initAllParam为:"+initAllParam);
        //入网月最大最小账期默认数据
        dateEntryList = initService.getMinMaxDateEntry();
        //观察月最大最小账期默认数据
        dateViewList = initService.getMinMaxDateView();
        getStartEndDefaultValues(dateEntryList,dateViewList);

        HttpHeaders httpHeaders = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        httpHeaders.setContentType(type);
        httpHeaders.add("Accept","*/*");

        for (HashMap<String,String> map:initAllParam){
            if ("-".equals(map.get("PARAM_VALUES"))){//地域、筛选条件、指标列表
                HttpEntity formEntity = new HttpEntity(httpHeaders);
                String result = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
                cacheResultMap.put(map.get("CODE"),result);
                System.out.print("result为"+result);
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
                    dataMap = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
                    cacheResultMap.put(map.get("CODE"),dataMap);
                }else if (map.get("CODE").equals("code_newuser_date_view")){//入网月、观察月
                    cacheResultMap.put(map.get("CODE"),dateViewList.toString());
                }else if (map.get("CODE").equals("code_newuser_date_entry")){
                    cacheResultMap.put(map.get("CODE"),dateEntryList.toString());
                }

            }
        }
        System.out.println("cacheResultMap:" + cacheResultMap);

    }


    /**
     * 给默认起止日期entryStartDefault、entryEndDefault、viewStartDefault、viewEndDefault赋值
     */
    private static void getStartEndDefaultValues(List<HashMap<String, String>> minMaxDateListEntry,
                                                 List<HashMap<String, String>> minMaxDateListView) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        GregorianCalendar gc = new GregorianCalendar();
        String endEntry = minMaxDateListEntry.get(0).get("MAX_DATE").replace("-", "");
        String endView = minMaxDateListView.get(0).get("MAX_DATE").replace("-", "");
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
        paramMap.put("provId", null);
        paramMap.put("clientId", null);
        paramMap.put("channel", null);
        paramMap.put("contract", null);
        paramMap.put("network", null);
        paramMap.put("terminal", null);
        paramMap.put("income", null);
        paramMap.put("product", null);
        paramMap.put("entryStartDate", entryStartDefault);
        paramMap.put("entryEndDate", entryEndDefault);
        paramMap.put("viewStartDate", viewStartDefault);
        paramMap.put("viewEndDate", viewEndDefault);
        paramMap.put("kpiCode", kpiDefault);
        //存放用于循环的入网月，每次都进行覆盖
        paramMap.put("entryMonth", null);
        return paramMap;
    }
}
