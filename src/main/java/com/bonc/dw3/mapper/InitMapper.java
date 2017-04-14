package com.bonc.dw3.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.*;

/**
 * Created by Candy on 2017/3/23.
 */
@Mapper
public interface InitMapper {

    /**
     * 1.查询入网月观察月标识
     *
     * @Author gp
     * @Date 2017/3/23
     */
    List<Map<String, String>> selectEntryViewMonthIds();


    /**
     * 2.查询筛选条件的默认值
     *
     * @Author gp
     * @Date 2017/3/23
     */
    List<Map<String, String>> selectConditionDefaultValues();
}
