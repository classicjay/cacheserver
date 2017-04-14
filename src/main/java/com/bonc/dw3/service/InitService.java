package com.bonc.dw3.service;

import com.bonc.dw3.mapper.InitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.*;

/**
 * Created by Candy on 2017/3/23.
 */
@Service
@CrossOrigin(origins = "*")
public class InitService {

    @Autowired
    InitMapper initMapper;

    /**
     * 1.查询入网月观察月标识
     *
     * @Author gp
     * @Date 2017/3/23
     */
    public List<Map<String, String>> getEntryViewMonthIdsList(){
        List<Map<String, String>> entryViewMonthIdsList = new ArrayList<>();
        entryViewMonthIdsList = initMapper.selectEntryViewMonthIds();
        return entryViewMonthIdsList;
    }


    /**
     * 2.查询筛选条件的默认值
     *
     * @Author gp
     * @Date 2017/3/23
     */
    public List<Map<String, String>> getConditionDefaultValuesList(){
        List<Map<String, String>> conditionDefaultValuesList = new ArrayList<>();
        conditionDefaultValuesList = initMapper.selectConditionDefaultValues();
        return conditionDefaultValuesList;
    }
}
