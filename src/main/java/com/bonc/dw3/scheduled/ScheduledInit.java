package com.bonc.dw3.scheduled;

import com.bonc.dw3.service.InitService;
import com.bonc.dw3.service.NewuserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Candy on 2017/3/22.
 */
@Component
public class ScheduledInit {

    @Autowired
    NewuserService newuserService;

    @Autowired
    InitService initService;

    //数据库中观察月入网月的key，用于查询相应的默认值
    private static final String ENTRYMONTHKEY = "entry_month";
    private static final String VIEWMONTHKEY = "view_month";
    //数据库中筛选条件的key，用于查询相应的默认值
    private static final String ALLKEY = "all";
    private static final String INCOMEKEY = "income";
    private static final String PRODUCTKEY = "product";
    private static final String KPIKEY = "kpiCode";

    //存放从数据库查询出的观察月入网月标识
    public static String entryMonthId;
    public static String viewMonthId;

    //存放从数据库中查询出的各种默认值
    //默认值：全部
    public static String allDefault;//-1
    //默认值:收入分档<100的id
    public static String incomeDefault;// "006002"
    //默认值:产品类型为CBSS的id
    public static String productDefault;//"007002"
    //默认值:kpicode出账用户
    public static String kpiDefault;//"CKP_25101"

    //时间窗口默认值选用最大账期向前推6个月
    //默认值:入网月起始时间
    public static String entryStartDefault;//"201610"
    //默认值:入网月终止时间
    public static String entryEndDefault;
    //默认值:观察月起始时间
    public static String viewStartDefault;
    //默认值:观察月终止时间
    public static String viewEndDefault;

    //地域接口数据缓存
    public static List<Map<String, Object>> areaList = new ArrayList<>();
    //筛选条件接口数据缓存
    public static List<Map<String, Object>> conditionList = new ArrayList<>();
    //入网月最大最小账期数据缓存
    public static List<Map<String, String>> minMaxDateListEntry = new ArrayList<>();
    //观察月最大最小账期数据缓存
    public static List<Map<String, String>> minMaxDateListView = new ArrayList<>();
    //kpi指标列表接口数据缓存
    public static List<Map<String, String>> kpiList = new ArrayList<>();
    //表格数据缓存
    public static Map<String, Object> dataMap = new HashMap<>();

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    public static String currentTime = "";

    /**
     * 定时任务-只要定时获取筛选条件等数据，不必每次请求都查询数据库
     * fixedRate = 43200000启动执行一次，之后每半天执行一次
     * cron = "0 0 0-23 * * ?" 整点执行
     *
     * @Author gp
     * @Date 2017/3/22
     */
    @Scheduled(fixedRate = 3600000)
    public void scheduledInit() {
        currentTime = dateFormat.format(new Date()).toString();
        System.out.println("初始化开始！正在进行数据缓存！！！！！" + currentTime);
        //从数据库得到入网月观察月标识列表
        List<Map<String, String>> entryViewMonthIdsList = initService.getEntryViewMonthIdsList();
        //赋值给相应的存放默认值的变量
        getEntryViewMonthIds(entryViewMonthIdsList);
        //从数据库得到筛选条件的默认值
        List<Map<String, String>> conditionDefaultValuesList = initService.getConditionDefaultValuesList();
        //赋值给相应的存放默认值的变量
        getConditionDefaultValues(conditionDefaultValuesList);
        //1.地域接口默认数据
        areaList = newuserService.getProv();
        //2.筛选条件接口默认数据
        conditionList = newuserService.selectCondition();
        //3.指标列表接口默认数据
        kpiList = newuserService.getKpiList();
        //4.入网月最大最小账期默认数据
        minMaxDateListEntry = newuserService.getMinMaxDateEntry();
        //5.观察月最大最小账期默认数据
        minMaxDateListView = newuserService.getMinMaxDateView();
        //给入网月观察月起止时间默认条件赋值
        getStartEndDefaultValues(minMaxDateListEntry, minMaxDateListView);
        //默认参数列表
        Map<String, Object> paramMap = getDataTableParamMap();
        //6.表格接口默认数据
        dataMap = newuserService.getDataTable(paramMap);
        //System.out.println("date:" + entryStartDefault + "," + entryEndDefault + "," + viewStartDefault + "," + viewEndDefault + ",dataMap=========>" + dataMap);
        System.out.println("初始化结束！本次数据缓存结束！！！！！！" + currentTime);
    }


    /**
     * 给默认起止日期entryStartDefault、entryEndDefault、viewStartDefault、viewEndDefault赋值
     *
     * @Author gp
     * @Date 2017/3/23
     */
    private void getStartEndDefaultValues(List<Map<String, String>> minMaxDateListEntry,
                                          List<Map<String, String>> minMaxDateListView) {
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
     * 给默认筛选条件incomeDefault、productDefault、allDefault、kpiDefault赋值
     *
     * @Author gp
     * @Date 2017/3/23
     */
    public void getConditionDefaultValues(List<Map<String, String>> conditionDefaultValuesList) {
        Map<String, String> conditionsMap = new HashMap<>();
        for (Map<String, String> map : conditionDefaultValuesList) {
            conditionsMap.put(map.get("KEY"), map.get("VALUE"));
        }
        allDefault = conditionsMap.get(ALLKEY);
        incomeDefault = conditionsMap.get(INCOMEKEY);
        productDefault = conditionsMap.get(PRODUCTKEY);
        kpiDefault = conditionsMap.get(KPIKEY);
    }


    /**
     * 给入网月观察月标识entryMonthId、viewMonthId赋值
     *
     * @Author gp
     * @Date 2017/3/23
     */
    public void getEntryViewMonthIds(List<Map<String, String>> entryViewMonthIdsList) {
        Map<String, String> idsMap = new HashMap<>();
        for (Map<String, String> map : entryViewMonthIdsList) {
            idsMap.put(map.get("KEY"), map.get("VALUE"));
        }
        entryMonthId = idsMap.get(ENTRYMONTHKEY);
        viewMonthId = idsMap.get(VIEWMONTHKEY);
    }


    /**
     * 得到表格接口默认的参数列表
     *
     * @Author gp
     * @Date 2017/3/23
     */
    public Map<String, Object> getDataTableParamMap() {
        Map<String, Object> paramMap = new HashMap<>();
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
