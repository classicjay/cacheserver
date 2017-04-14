package com.bonc.dw3.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.bonc.dw3.mapper.NewuserMapper;

@Service
@CrossOrigin(origins = "*")
public class NewuserService {

    @Autowired
    private NewuserMapper newuserMapper;

    /**
     * 地域接口-全国所有省份
     *
     * @Author gp
     * @Date 2017/3/2
     */
    public List<Map<String, Object>> getProv() {
        List<Map<String, Object>> areaList = newuserMapper.getProv();
        return area(areaList);
    }

    /**
     * 筛选条件接口（维度）
     *
     * @Author gp
     * @Date 2017/3/2
     */
    public List<Map<String, Object>> selectCondition() {
        List<Map<String, Object>> conditionList = newuserMapper.selectCondition();
        return condition(conditionList);
    }


    /**
     * 入网月最大最小账期
     *
     * @Author gp
     * @Date 2017/3/2
     */
    public List<Map<String, String>> getMinMaxDateEntry() {
        List<Map<String, String>> minMaxDateList = newuserMapper.getMinMaxDateEntry();
        return minMaxDateList;
    }


    /**
     * 观察月最大最小账期
     *
     * @Author gp
     * @Date 2017/3/2
     */
    public List<Map<String, String>> getMinMaxDateView() {
        List<Map<String, String>> minMaxDateList = newuserMapper.getMinMaxDateView();
        return minMaxDateList;
    }


    /**
     * 指标列表接口
     *
     * @Author gp
     * @Date 2017/3/2
     */
    public List<Map<String, String>> getKpiList() {
        List<Map<String, String>> kpiList = newuserMapper.getKpiList();
        return kpi(kpiList);
    }


    /**
     * 表格接口
     *
     * @param paramMap json串(包含所有查询维度等)：{"provId":"010","clientId":"001002", "channelId":["002002","002003"], "contractId":["003002","003003"], "networkId":["004002","004003"], "terminalId":["005002","005003"], "incomeId":["006002","006003"], "productId":["007002","007003"], "entryDate":{"startDate":"2016-10","endDate":"2016-11"},"viewDate":{"startDate":"2016-11","endDate":"2017-01"},"kpi":"CKP_25106"}
     * @Author gp
     * @Date 2017/3/2
     */
    public Map<String, Object> getDataTable(Map<String, Object> paramMap) {
        Map<String, Object> dataMap = new HashMap<>();
        //得到kpi信息
        Map<String, String> kpiMap = getKpiMap(paramMap);
        dataMap.put("kpi", kpiMap);
        //得到thData的内容
        List<Map<String, Object>> thDataList = getThDataList(paramMap);
        dataMap.put("thData", thDataList);
        //得到tbodyData的内容
        List<Map<String, Object>> tBodyDataList = getTbodyDataList(paramMap);
        dataMap.put("tbodyData", tBodyDataList);
        return dataMap;
    }


    /**
     * 表格接口：得到kpi信息
     *
     * @Author gp
     * @Date 2017/3/9
     */
    public Map<String, String> getKpiMap(Map<String, Object> paramMap){
        Map<String, String> kpiMap = new HashMap<>();
        String kpiCode = paramMap.get("kpiCode").toString();
        String kpiName = newuserMapper.getKpiName(kpiCode);
        kpiMap.put("id", kpiCode);
        kpiMap.put("title", kpiName);
        return kpiMap;
    }


    /**
     * 表格接口：得到thData信息（观察月信息）
     *
     * @Author gp
     * @Date 2017/3/9
     */
    public List<Map<String, Object>> getThDataList(Map<String, Object> paramMap){
        List<Map<String, Object>> thDataList = new ArrayList<>();
        //用于存放观察月起止时间参数
        Map<String, String> paramForViewDateList = new HashMap<>();
        paramForViewDateList.put("viewStartDate", paramMap.get("viewStartDate").toString());
        paramForViewDateList.put("viewEndDate", paramMap.get("viewEndDate").toString());
        //得到入网月和观察月列表
        List<Map<String, String>> viewDateList = getViewDateList(paramForViewDateList);

        //用来计数map的个数
        int yearMapCount = 0;
        for(int i = 0; i < viewDateList.size(); i ++){
            String year = viewDateList.get(i).get("VIEW_YEAR");
            String month = viewDateList.get(i).get("VIEW_MONTH");
            //如果thDataList不为空，则判断它的上一个map的year信息和当前循环的year是否相同
            //相同的添加year信息，不同的添加months信息
            if(thDataList.size() > 0){
                //相同，添加months信息
                if (thDataList.get(yearMapCount - 1).get("year").toString().equals(year)){
                    ((List<String>)thDataList.get(yearMapCount - 1).get("months")).add(month);
                }else{
                    //不同，添加year信息
                    Map<String, Object> yearMap = new HashMap<>();
                    yearMap.put("year", year);
                    List<String> monthsList = new ArrayList<>();
                    monthsList.add(month);
                    yearMap.put("months", monthsList);
                    thDataList.add(yearMap);
                    yearMapCount = yearMapCount + 1;
                }
            }else{
                //thDataList为空，添加year信息（map）
                Map<String, Object> yearMap = new HashMap<>();
                yearMap.put("year", year);
                List<String> monthsList = new ArrayList<>();
                monthsList.add(month);
                yearMap.put("months", monthsList);
                thDataList.add(yearMap);
                yearMapCount += 1;
            }
        }
        return thDataList;
    }


    /**
     * 表格接口：得到tbodayData
     *
     * @Author gp
     * @Date 2017/3/9
     */
    public List<Map<String, Object>> getTbodyDataList(Map<String, Object> paramMap){
        List<Map<String, Object>> tBodyDataList = new ArrayList<>();
        //用于存放入网月起止时间参数
        Map<String, String> paramForEntryDateList = new HashMap<>();
        paramForEntryDateList.put("entryStartDate", paramMap.get("entryStartDate").toString());
        paramForEntryDateList.put("entryEndDate", paramMap.get("entryEndDate").toString());
        //得到入网月和观察月列表
        List<Map<String, String>> entryDateList = getEntryDateList(paramForEntryDateList);
        //用来计数：tBodyDataList里面有count个map
        int count = 0;
        for (int i = 0; i < entryDateList.size(); i++) {
            //entryDateList=[{ENTRY_YEAR=2016,ENTRY_MONTH=10}, ...]
            String year = entryDateList.get(i).get("ENTRY_YEAR");
            String month = entryDateList.get(i).get("ENTRY_MONTH");
            //给paramMap中的entryMonth赋值
            paramMap.put("entryMonth", year + month);
            //tBodyDataList:[{"year": "2015年","months": [{"month": "1月","net_value": "220","obs_values": []},....}]
            //如果tBodyDataList不为空，就比较上一条map，看它的年份跟本次的是否相同
            //相同的就添加months信息，不同的添加year信息
            if (tBodyDataList.size() > 0) {
                //上一条map的年份跟本次相同，添加months信息(map)
                if (tBodyDataList.get(count - 1).get("year").toString().equals(year)) {
                    Map<String, Object> monthMap = new HashMap<>();
                    monthMap.put("month", month);
                    String netValue = newuserMapper.getNewUserNum(paramMap);
                    if (netValue == null || netValue.equals("")) {
                        monthMap.put("net_value", "");
                    }else{
                        monthMap.put("net_value", netValue);
                    }
                    List<String> obsValues = getObsValues(paramMap);
                    monthMap.put("obs_values", obsValues);
                    ((List<Map<String,Object>>) tBodyDataList.get(count - 1).get("months")).add(monthMap);
                } else {
                    //上一条map的年份跟本次不同，添加year信息(map)
                    Map<String, Object> yearMap = new HashMap<>();
                    yearMap.put("year", year);
                    List<Map<String, Object>> monthsList = new ArrayList<>();
                    Map<String, Object> monthMap = new HashMap<>();
                    monthMap.put("month", month);
                    String netValue = newuserMapper.getNewUserNum(paramMap);

                    if (netValue == null || netValue.equals("")) {
                        monthMap.put("net_value", "");
                    }else{
                        monthMap.put("net_value", netValue);
                    }
                    List<String> obsValues = getObsValues(paramMap);
                    monthMap.put("obs_values", obsValues);
                    monthsList.add(monthMap);
                    yearMap.put("months", monthsList);
                    tBodyDataList.add(yearMap);
                    count += 1;
                }
            } else {
                //tBodyDataList为空，就直接添加一条map
                //只执行一次
                Map<String, Object> tBodyDataMap = new HashMap<>();
                tBodyDataMap.put("year", year);

                List<Map<String, Object>> monthsList = new ArrayList<>();
                Map<String, Object> monthMap = new HashMap<>();
                monthMap.put("month", month);
                String netValue = newuserMapper.getNewUserNum(paramMap);
                if (netValue == null || netValue.equals("")) {
                    monthMap.put("net_value", "");
                }else{
                    monthMap.put("net_value", netValue);
                }
                List<String> obsValues = getObsValues(paramMap);
                monthMap.put("obs_values", obsValues);
                monthsList.add(monthMap);

                tBodyDataMap.put("months", monthsList);
                tBodyDataList.add(tBodyDataMap);
                count += 1;
            }
        }
        return tBodyDataList;
    }


    /**
     * 表格接口：得到obs_values
     *
     * @Author gp
     * @Date 2017/3/9
     */
    public List<String> getObsValues(Map<String, Object> paramMap) {
        List<String> obsValues = new ArrayList<>();
        List<Map<String, String>> dataList = newuserMapper.getDataTable(paramMap);
        for(int i = 0; i < dataList.size(); i ++){
            String kpiValue = dataList.get(i).get("KPI_VALUE");
            if (kpiValue == null || kpiValue.equals("")){
                obsValues.add("");
            }else{
                obsValues.add(kpiValue);
            }
        }
        return obsValues;
    }


    /**
     * 得到入网月列表
     *
     * @Author gp
     * @Date 2017/3/7
     */
    public List<Map<String, String>> getEntryDateList(Map<String, String> paramMap) {
        List<Map<String, String>> entryDateList = newuserMapper.getEntryDateList(paramMap);
        return entryDateList;
    }


    /**
     * 得到观察月列表
     *
     * @Author gp
     * @Date 2017/3/7
     */
    public List<Map<String, String>> getViewDateList(Map<String, String> paramMap) {
        List<Map<String, String>> viewDateList = newuserMapper.getViewDateList(paramMap);
        return viewDateList;
    }


    /**
     * 地域接口：prov整体数据处理
     *
     * @param areaList 数据库查出的原始数据
     * @Author gp
     * @Date 2017/3/2
     */
    private List<Map<String, Object>> area(List<Map<String, Object>> areaList) {
        Map<String, List<Map<String, Object>>> provMap = new LinkedHashMap<String, List<Map<String, Object>>>();
        List<Map<String, Object>> resultList = new LinkedList<Map<String, Object>>();
        if (areaList != null) {
            for (Map<String, Object> map : areaList) {
                if (map.get("PROV_ID") != null) {
                    if (provMap.get(map.get("PROV_ID")) != null) {
                        provMap.get(map.get("PROV_ID")).add(map);
                    } else {
                        List<Map<String, Object>> provList = new LinkedList<Map<String, Object>>();
                        provList.add(map);
                        provMap.put(map.get("PROV_ID").toString(), provList);
                    }
                }
            }
            for (Map.Entry<String, List<Map<String, Object>>> provEntry : provMap.entrySet()) {
                Map<String, Object> areaMap = new LinkedHashMap<String, Object>();
                areaMap.put("pro_id", provEntry.getKey());
                for (Map<String, Object> provValue : provEntry.getValue()) {
                    areaMap.put("pro_name", provValue.get("PRO_NAME"));
                }
                resultList.add(areaMap);
            }
        }
        return resultList;
    }


    /**
     * 筛选条件接口：condition整体数据处理
     *
     * @param conditionList 数据库查出的原始数据
     * @Author gp
     * @Date 2017/3/2
     */
    private List<Map<String, Object>> condition(List<Map<String, Object>> conditionList) {
        Map<String, List<Map<String, Object>>> typeMap = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (conditionList != null) {
            for (Map<String, Object> map : conditionList) {
                if (map.get("T_ID") != null) {
                    if (typeMap.get(map.get("T_ID")) != null) {
                        typeMap.get(map.get("T_ID")).add(map);
                    } else {
                        List<Map<String, Object>> typeList = new LinkedList<Map<String, Object>>();
                        typeList.add(map);
                        typeMap.put(map.get("T_ID").toString(), typeList);
                    }
                }
            }
            for (Map.Entry<String, List<Map<String, Object>>> typeEntry : typeMap.entrySet()) {
                Map<String, Object> dimensionMap = new LinkedHashMap<String, Object>();
                dimensionMap.put("tid", typeEntry.getKey());
                List<Map<String, Object>> dataList = new LinkedList<Map<String, Object>>();
                for (Map<String, Object> typeValue : typeEntry.getValue()) {
                    dimensionMap.put("tname", typeValue.get("T_NAME"));
                    dataList.add(typeValue);
                }
                List<Map<String, Object>> data = data(dataList);
                dimensionMap.put("data", data);
                resultList.add(dimensionMap);
            }
        }
        return resultList;
    }


    /**
     * 筛选条件接口：data部分处理
     *
     * @param dataList
     * @Author gp
     * @Date 2017/3/2
     */
    private List<Map<String, Object>> data(List<Map<String, Object>> dataList) {
        List<Map<String, Object>> dataResultList = new ArrayList<>();
        if (dataList != null) {
            for (Map<String, Object> data : dataList) {
                Map<String, Object> dataMap = new HashMap<>();
                if (data.get("DATA_ID") != null) {
                    dataMap.put("id", data.get("DATA_ID"));
                    dataMap.put("text", data.get("DATA_TEXT"));
                    dataResultList.add(dataMap);
                } else {
                    dataMap.put("id", "");
                    dataMap.put("text", "");
                    dataResultList.add(dataMap);
                }
            }
        } else {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("id", "");
            dataMap.put("text", "");
            dataResultList.add(dataMap);
        }
        return dataResultList;
    }


    /**
     * 指标列表接口：kpiList整体数据处理
     *
     * @param kpiList 数据库查出的原始数据
     * @Author gp
     * @Date 2017/3/2
     */
    public List<Map<String, String>> kpi(List<Map<String, String>> kpiList) {
        List<Map<String, String>> resultList = new ArrayList<>();
        if (kpiList != null) {
            for (Map<String, String> map : kpiList) {
                Map<String, String> kpiMap = new HashMap<>();
                kpiMap.put("id", map.get("KPI_CODE"));
                kpiMap.put("name", map.get("KPI_NAME"));
                resultList.add(kpiMap);
            }
        } else {
            Map<String, String> kpiMap = new HashMap<>();
            kpiMap.put("id", "");
            kpiMap.put("name", "");
            resultList.add(kpiMap);
        }
        return resultList;
    }




    /**
     * 表格接口：数据处理
     *
     * @param dataList 数据库查出的原始数据
     * @Author gp
     * @Date 2017/3/2
     */
    /*public Map<String, List> table(List<Map<String, String>> dataList) {
        Map<String, List> result = new HashMap<>();
        for (Map<String, String> map : dataList) {
            if (map.get("ENTRY_MONTH") != null) {
                if (result.get(map.get("ENTRY_MONTH")) != null) {
                    result.get(map.get("ENTRY_MONTH")).add(map.get("NEWUSER_NUM"));
                } else {
                    List<String> data = new ArrayList();
                    data.add(map.get("NEWUSER_NUM"));
                    result.put(map.get("ENTRY_MONTH"), data);
                }
            }
        }
        return result;
    }*/


}