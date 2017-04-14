package com.bonc.dw3.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewuserMapper {

    /**
     * 地域：全国所有省份数据显示
     *
     * @Author gp
     * @Date 2017/3/2
     */
    List<Map<String, Object>> getProv();


    /**
     * 筛选条件（所有维度）
     *
     * @Author gp
     * @Date 2017/3/2
     */
    List<Map<String, Object>> selectCondition();


    /**
     * 入网月最大最小账期
     *
     * @Author gp
     * @Date 2017/3/2
     */
    List<Map<String, String>> getMinMaxDateEntry();


    /**
     * 观察月最大最小账期
     *
     * @Author gp
     * @Date 2017/3/2
     */
    List<Map<String, String>> getMinMaxDateView();


    /**
     * 指标列表
     *
     * @Author gp
     * @Date 2017/3/2
     */
    List<Map<String, String>> getKpiList();


    /**
     * 表格数据
     *
     * @Author gp
     * @Date 2017/3/2
     */
    List<Map<String, String>> getDataTable(Map<String, Object> paramMap);


    /**
     * 新发展用户数据
     *
     * @Author gp
     * @Date 2017/3/8
     */
    String getNewUserNum(Map<String, Object> paramMap);


    /**
     * 入网月列表
     *
     * @Author gp
     * @Date 2017/3/7
     */
    List<Map<String, String>> getEntryDateList(Map<String, String> paramMap);


    /**
     * 观察月列表
     *
     * @Author gp
     * @Date 2017/3/7
     */
    List<Map<String, String>> getViewDateList(Map<String, String> paramMap);

    /**
     * 得到kpiName
     *
     * @Author gp
     * @Date 2017/3/9
     */
    String getKpiName(String kpiCode);



	/*考核总览接口*//*

	*//* 最大账期数据接口：最大账期显示
	 * @return 实体对象 
	 * *//*
	String getDate();
	
	*//* 地域接口：139城市所有数据显示
	 * 传入paramMap查询地域信息
	 * @param paramMap
	 * 一个key:
	 * id=1:139城市编码
	 * @return 实体对象 
	 * *//*
	//List<Map<String,Object>> getArea(String param);
	List<Map<String,Object>> getArea_139(Map<String,String> paramMap);

	*//*城市分项排名分数接口:1.title内容获取
	 * @return 实体对象 
	 * *//*
	List<Map<String,String>> getCityItemRank_title();

	*//*城市分项排名分数接口:2.省份城市和分项及总分排名数据获取
	 *传入paramMap查询地域信息
	 * @param paramMap
	 * 一个key:
	 * month:排名月份
	 * @return 实体对象 
	 * *//*
	List<Map<String,Object>> getCityItemRank_Full(Map<String,String> paramMap);

	*//*城市综合排名总分数接口:1.地势获取及对应得分获取
	 *传入paramMap查询地域信息
	 * @param paramMap
	 * 一个key:
	 * month:排名月份
	 * @return 实体对象 
	 * *//*
	List<Map<String,Object>> getCityCompRank_area(Map<String,String> paramMap);*/


}
