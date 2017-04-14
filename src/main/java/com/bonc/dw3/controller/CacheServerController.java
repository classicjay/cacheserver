package com.bonc.dw3.controller;

import java.util.*;

import com.bonc.dw3.scheduled.ScheduledInit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bonc.dw3.service.NewuserService;

@Api(value = "新发展用户拍照分析", description = "测试库")
@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/CacheServer")
public class CacheServerController {

    @Autowired
    private NewuserService newuserService;

    //默认值:入网月起始时间
    public String entryStartDefault;
    //默认值:入网月终止时间
    public String entryEndDefault;
    //默认值:观察月起始时间
    public String viewStartDefault;
    //默认值:观察月终止时间
    public String viewEndDefault;

    private static Logger logger = Logger.getLogger(CacheServerController.class);

    /**
     * 地域接口-全国所有省份
     *
     * @Author gp
     * @Date 2017/3/2
     */
    @ApiOperation("地域接口")
    @PostMapping("/area")
    public String getArea(Model model) {
        //List<Map<String, Object>> areaList = newuserService.getProv();
        //定时任务读取地域接口数据，而不是每次请求都查询数据库
        List<Map<String, Object>> areaList = ScheduledInit.areaList;

        model.addAttribute("areaList", areaList);
        return "area";
    }


    /**
     * 筛选条件接口
     *
     * @Author gp
     * @Date 2017/3/2
     */
    @ApiOperation("筛选条件接口")
    @PostMapping("/select")
    public String selectCondition(Model model) {
        //定时任务读取筛选条件接口数据，而不是每次请求都查询数据库
        List<Map<String, Object>> conditionList = ScheduledInit.conditionList;

        model.addAttribute("conditionList", conditionList);
        return "selectCondition";
    }


    /**
     * 最大最小账期接口
     *
     * @param id 入网月观察月标识（1-入网月，2-观察月）
     * @Author gp
     * @Date 2017/3/2
     */
    @ApiOperation("最大账期")
    @PostMapping("/Date")
    public String getMaxDate(@ApiParam("入网月观察月标识")
                                 @RequestParam String id,
                             Model model) {
        //id = 1：入网月
        if (id.equals(ScheduledInit.entryMonthId)) {
            //定时任务读取入网月最大最小账期接口数据，而不是每次请求都查询数据库
            List<Map<String, String>> minMaxDateList = ScheduledInit.minMaxDateListEntry;
            model.addAttribute("minMaxDateList", minMaxDateList);
        } else if (id.equals(ScheduledInit.viewMonthId)) {
            //id = 2：观察月
            //定时任务读取观察月最大最小账期接口数据，而不是每次请求都查询数据库
            List<Map<String, String>> minMaxDateList = ScheduledInit.minMaxDateListView;
            model.addAttribute("minMaxDateList", minMaxDateList);
        } else {
            List<Map<String, String>> minMaxDateList = new LinkedList<>();
            model.addAttribute("minMaxDateList", minMaxDateList);
        }
        return "date";
    }


    /**
     * 指标列表接口
     *
     * @Author gp
     * @Date 2017/3/2
     */
    @ApiOperation("指标列表接口")
    @PostMapping("/KpiList")
    public String getKpiList(Model model) {
        //定时任务读取指标列表接口数据，而不是每次请求都查询数据库
        List<Map<String, String>> kpiList = ScheduledInit.kpiList;

        model.addAttribute("kpiList", kpiList);
        return "kpi";
    }


    /**
     * 表格接口
     *
     * @param param json串，包含入网月和观察月起止时间，如下所示
     *              {"Entrydate":{"startDate":"2016-10","endDate":"2016-11"},
     *              "Viewdate":{"startDate":"2016-11","endDate":"2017-01"}}
     * @Author gp
     * @Date 2017/3/2
     */
    @ApiOperation("表格接口")
    @PostMapping("/Datatable")
    public String getDataTable(@ApiParam("json串，包含入网月和观察月起止时间")
                                   @RequestBody Map<String, Object> param,
                               Model model) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap = dataTableParamDeal(param);
        /*System.out.println("paramMap+++++++++>" + paramMap);*/

        //判断这些参数是否都是默认参数，都是默认参数的话就从定时任务缓存的数据里去取
        if (paramMap.get("flag").toString().equals("11")){
            Map<String, Object> dataMap = ScheduledInit.dataMap;
            model.addAttribute("dataMap", dataMap);
        }else{
            //如果不是默认参数，就从数据库中查询
            //得到接口想要的数据
            Map<String, Object> dataMap = newuserService.getDataTable(paramMap);
            model.addAttribute("dataMap", dataMap);
        }
        return "dataTable";
    }

    /**
     * 表格接口参数处理
     *
     * @Author gp
     * @Date 2017/3/24
     */
    private Map<String,Object> dataTableParamDeal(Map<String, Object> param) {
        Map<String, Object> paramMap = new HashMap<>();
        //取参数
        String provId = param.get("provId").toString();
        String clientId = param.get("clientId").toString();
        List<String> provIdList = new ArrayList<>();
        List<String> clientIdList = new ArrayList<>();
        List<String> channelIdList = (List<String>) param.get("channelId");
        List<String> contractIdList = (List<String>) param.get("contractId");
        List<String> networkIdList = (List<String>) param.get("networkId");
        List<String> terminalIdList = (List<String>) param.get("terminalId");
        List<String> incomeIdList = (List<String>) param.get("incomeId");
        List<String> productIdList = (List<String>) param.get("productId");
        Map<String, String> entryDateMap = (Map<String, String>) param.get("entryDate");
        //2016-10转换成201610
        String entryStartDate = entryDateMap.get("startDate").replace("-", "");
        String entryEndDate = entryDateMap.get("endDate").replace("-", "");
        Map<String, String> viewDateMap = (Map<String, String>) param.get("viewDate");
        String viewStartDate = viewDateMap.get("startDate").replace("-", "");
        String viewEndDate = viewDateMap.get("endDate").replace("-", "");
        String kpiCode = param.get("kpi").toString();

        //标记，用来给查询条件是默认值的条件计数，最终用来判断是否所有的查询条件都是默认值
        //count = 11
        int count = 0;
        //判断省份类型是否为“-1”全部，全部时传null,并设置默认标记
        if (!provId.equals(ScheduledInit.allDefault)){
            provIdList.add(provId);
            paramMap.put("provId", provIdList);
        }else{
            paramMap.put("provId", null);
            //设置标记
            count += 1;
            paramMap.put("flag", count);//1
        }
        //判断客户类型是否为“-1”全部，全部时传null,并设置默认标记
        if (!clientId.equals(ScheduledInit.allDefault)) {
            clientIdList.add(clientId);
            paramMap.put("clientId", clientIdList);
        } else {
            paramMap.put("clientId", null);
            count += 1;
            paramMap.put("flag", count);//2
        }
        //判断渠道编码是否为“-1”全部，全部时传null
        if (channelIdList.size() == 1 && ScheduledInit.allDefault.equals(channelIdList.get(0))) {
            paramMap.put("channel", null);
            count += 1;
            paramMap.put("flag", count);//3
        } else {
            paramMap.put("channel", channelIdList);
        }
        //判断合约类型是否为“-1”全部，全部时传null
        if (contractIdList.size() == 1 && ScheduledInit.allDefault.equals(contractIdList.get(0))) {
            paramMap.put("contract", null);
            count += 1;
            paramMap.put("flag", count);//4
        } else {
            paramMap.put("contract", contractIdList);
        }
        //判断使用网络是否为“-1”全部，全部时转null
        if (networkIdList.size() == 1 && ScheduledInit.allDefault.equals(networkIdList.get(0))) {
            paramMap.put("network", null);
            count += 1;
            paramMap.put("flag", count);//5
        } else {
            paramMap.put("network", networkIdList);
        }
        //判断终端是否为“-1”全部，全部时转null
        if (terminalIdList.size() == 1 && ScheduledInit.allDefault.equals(terminalIdList.get(0))) {
            paramMap.put("terminal", null);
            count += 1;
            paramMap.put("flag", count);//6
        } else {
            paramMap.put("terminal", terminalIdList);
        }

        //判断收入类型是否是默认值，如果是，设置默认标记count+1
        if (incomeIdList.size() == 1 && incomeIdList.get(0).toString().equals(ScheduledInit.incomeDefault)){
            paramMap.put("income", null);
            count += 1;
            paramMap.put("flag", count);//7
        }else {
            paramMap.put("income", incomeIdList);
        }
        //判断产品类型是否是默认值，如果是，设置默认标记count+1
        if (productIdList.size() == 1 && productIdList.get(0).toString().equals(ScheduledInit.productDefault)){
            paramMap.put("product", null);
            count += 1;
            paramMap.put("flag", count);//8
        }else{
            paramMap.put("product", productIdList);
        }
        //判断入网月起始时间和终止时间是否为默认值
        if (entryStartDate.equals(ScheduledInit.entryStartDefault) && entryEndDate.equals(ScheduledInit.entryEndDefault)){
            count += 1;
            paramMap.put("flag", count);//9
        }
        //判断观察月起始时间和终止时间是否为默认值
        if (viewStartDate.equals(ScheduledInit.viewStartDefault) && viewEndDate.equals(ScheduledInit.viewEndDefault)){
            count += 1;
            paramMap.put("flag", count);//10
        }
        //判断kpicode指标是否是默认值
        if (kpiCode.equals(ScheduledInit.kpiDefault)){
            count += 1;
            paramMap.put("flag", count);//11
        }

        if (count == 0){
            paramMap.put("flag", count);
        }

        paramMap.put("entryStartDate", entryStartDate);
        paramMap.put("entryEndDate", entryEndDate);
        paramMap.put("viewStartDate", viewStartDate);
        paramMap.put("viewEndDate", viewEndDate);
        paramMap.put("kpiCode", kpiCode);
        //存放用于循环的入网月，每次都进行覆盖
        paramMap.put("entryMonth", null);
        return paramMap;
    }
}