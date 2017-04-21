//package com.bonc.dw3.scheduled;
//
//import com.bonc.dw3.service.InitService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
///**
// * Created by Candy on 2017/3/22.
// */
//@Component
//public class ScheduledInit {
//
//
//    @Autowired
//    RestTemplate restTemplate;
//
//    @Autowired
//     InitService initService;
//
//    //默认值:kpicode出账用户
//    public static String kpiDefault;//"CKP_25101"
//
//
//
//    //时间窗口默认值选用最大账期向前推6个月
//    //默认值:入网月起始时间
//    public static String entryStartDefault;//"201610"
//    //默认值:入网月终止时间
//    public static String entryEndDefault;
//    //默认值:观察月起始时间
//    public static String viewStartDefault;
//    //默认值:观察月终止时间
//    public static String viewEndDefault;
//
//
//    //入网月最大最小账期数据缓存
//    public static List<HashMap<String,String>> dateEntryList = new ArrayList<>();
//    //观察月最大最小账期数据缓存
//    public static List<HashMap<String,String>> dateViewList = new ArrayList<>();
//    //表格数据缓存
//    public static String dataMap = new String();
//
//    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//    public static String currentTime = "";
//
//
//    public static HashMap<String,String> resultMap = new HashMap<>();
//
//    public static List<HashMap<String,String>> initAllParam;
//
//    public  void scheduledInit() {
//        currentTime = dateFormat.format(new Date()).toString();
//        System.out.println("初始化开始！正在进行数据缓存！！！！！" + currentTime);
//        initAllParam = initService.getAllInitParam();
//        System.out.println("initAllParam为:"+initAllParam);
//        //入网月最大最小账期默认数据
//        dateEntryList = initService.getMinMaxDateEntry();
//        //观察月最大最小账期默认数据
//        dateViewList = initService.getMinMaxDateView();
//        getStartEndDefaultValues(dateEntryList,dateViewList);
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
//        httpHeaders.setContentType(type);
//        httpHeaders.add("Accept","*/*");
//
//        for (HashMap<String,String> map:initAllParam){
//            if ("-".equals(map.get("PARAM_VALUES"))){//地域、筛选条件、指标列表
//                HttpEntity formEntity = new HttpEntity(httpHeaders);
//                String result = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
//                resultMap.put(map.get("CODE"),result);
//                System.out.print("result为"+result);
//            }else {
//                if (map.get("CODE").equals("code_newuser_datatable")){//
//                    String[] paramValues = new String[256];
//
//                    if (map.get("PARAM_VALUES") != null){
//                        paramValues = map.get("PARAM_VALUES").split(",");
//                    }
//                    List dataTableVal = new ArrayList(Arrays.asList(paramValues));
//                    kpiDefault = (String) dataTableVal.get(3);
//                    HashMap<String, Object> paramMap = getDataTableParamMap();
//                    HttpEntity<HashMap<String,Object>> formEntity = new HttpEntity<HashMap<String, Object>>(paramMap,httpHeaders);
//                    dataMap = restTemplate.postForObject(map.get("URL"),formEntity,String.class);
//                    resultMap.put(map.get("CODE"),dataMap);
//                }else if (map.get("CODE").equals("code_newuser_date_view")){//入网月、观察月
//                    resultMap.put(map.get("CODE"),dateViewList.toString());
//                }else if (map.get("CODE").equals("code_newuser_date_entry")){
//                    resultMap.put(map.get("CODE"),dateEntryList.toString());
//                }
//
//            }
//        }
//        System.out.println("resultMap:" + resultMap);
//        System.out.println("初始化结束！本次数据缓存结束！！！！！！" + currentTime);
//
//    }
//
//
//    /**
//     * 给默认起止日期entryStartDefault、entryEndDefault、viewStartDefault、viewEndDefault赋值
//     */
//    private static void getStartEndDefaultValues(List<HashMap<String, String>> minMaxDateListEntry,
//                                          List<HashMap<String, String>> minMaxDateListView) {
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
//        GregorianCalendar gc = new GregorianCalendar();
//        String endEntry = minMaxDateListEntry.get(0).get("MAX_DATE").replace("-", "");
//        String endView = minMaxDateListView.get(0).get("MAX_DATE").replace("-", "");
//        try {
//            //入网月起（最大账期往前2个月）止（最大账期）时间默认值
//            Date endEntryDate = format.parse(endEntry);
//            gc.setTime(endEntryDate);
//            gc.add(2, -5);
//            Date startEntryDate = gc.getTime();
//            entryStartDefault = format.format(startEntryDate);
//            entryEndDefault = endEntry;
//            //观察月起（最大账期往前2个月）止（最大账期）时间默认值
//            Date endViewDate = format.parse(endView);
//            gc.setTime(endViewDate);
//            gc.add(2, -5);
//            Date startViewDate = gc.getTime();
//            viewStartDefault = format.format(startViewDate);
//            viewEndDefault = endView;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * 得到表格接口默认的参数列表
//     */
//    public static HashMap<String, Object> getDataTableParamMap() {
//        HashMap<String, Object> paramMap = new HashMap<>();
//        paramMap.put("provId", null);
//        paramMap.put("clientId", null);
//        paramMap.put("channel", null);
//        paramMap.put("contract", null);
//        paramMap.put("network", null);
//        paramMap.put("terminal", null);
//        paramMap.put("income", null);
//        paramMap.put("product", null);
//        paramMap.put("entryStartDate", entryStartDefault);
//        paramMap.put("entryEndDate", entryEndDefault);
//        paramMap.put("viewStartDate", viewStartDefault);
//        paramMap.put("viewEndDate", viewEndDefault);
//        paramMap.put("kpiCode", kpiDefault);
//        //存放用于循环的入网月，每次都进行覆盖
//        paramMap.put("entryMonth", null);
//        return paramMap;
//    }
//
//}
