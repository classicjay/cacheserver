package com.bonc.dw3.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * <p>Title: BONC -  TimeUtil</p>
 * <p>Description:  </p>
 * <p>Copyright: Copyright BONC(c) 2013 - 2025 </p>
 * <p>Company: 北京东方国信科技股份有限公司 </p>
 *
 * @author zhaojie
 * @version 1.0.0
 */
public class TimeUtil {
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
}
