package com.bonc.dw3.service;

import com.bonc.dw3.mapper.InitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.*;

@Service
@CrossOrigin(origins = "*")
public class InitService {

    @Autowired
    InitMapper initMapper;

    /**
     * 查询入网月观察月标识
     *
     */
    public List<Map<String, String>> getEntryViewMonthIdsList(){
        List<Map<String, String>> entryViewMonthIdsList = new ArrayList<>();
        entryViewMonthIdsList = initMapper.selectEntryViewMonthIds();
        return entryViewMonthIdsList;
    }


    /**
     * 查询筛选条件的默认值
     *
     */
    public List<Map<String, String>> getConditionDefaultValuesList(){
        List<Map<String, String>> conditionDefaultValuesList = new ArrayList<>();
        conditionDefaultValuesList = initMapper.selectConditionDefaultValues();
        return conditionDefaultValuesList;
    }

    public List<HashMap<String,String>> getAllInitParam(){
        List<HashMap<String,String>> allInitParam = new ArrayList<>();
        allInitParam = initMapper.getAllInitParam();
        return allInitParam;
    }

    public List<HashMap<String, String>> getMinMaxDateEntry() {
        List<HashMap<String, String>> minMaxDateList = initMapper.getMinMaxDateEntry();
        return minMaxDateList;
    }

    public List<HashMap<String, String>> getMinMaxDateView() {
        List<HashMap<String, String>> minMaxDateList = initMapper.getMinMaxDateView();
        return minMaxDateList;
    }

    public List<HashMap<String,String>> getServerList(){
        List<HashMap<String,String>> serverList = initMapper.getServerList();
        return serverList;
    }

    public void setFailTime(String failTime){
        initMapper.setFailTime(failTime);
    }

    public void setServerNotAvailable(HashMap<String,String> paramMap){
        initMapper.setServerNotAvailable(paramMap);
    }
}
