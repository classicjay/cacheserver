package com.bonc.dw3.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.*;

/**
 * Created by Candy on 2017/3/23.
 */
@Mapper
public interface InitMapper {

    /**
     * 查询入网月观察月标识
     *
     */
    List<Map<String, String>> selectEntryViewMonthIds();


    /**
     * 查询筛选条件的默认值
     *
     */
    List<Map<String, String>> selectConditionDefaultValues();

    public List<HashMap<String,String>> getAllInitParam();

    public List<HashMap<String, String>> getMinMaxDateEntry();

    public List<HashMap<String, String>> getMinMaxDateView();

    public List<HashMap<String,String>> getServerList();

    public void setFailTime(String failTime);

    public void setServerNotAvailable(HashMap<String,String> paramMap);
}
