package com.bonc.dw3.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isGreaterThanOneHour(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1;
        Date date2;
        long hours = 0;
        try {
            date1 = sdf.parse(time1);
            date2 = sdf.parse(time2);
            long timeVal1 = date1.getTime();
            long timeVal2 = date2.getTime();
            long diff;
            if (timeVal1<timeVal2){
                diff = timeVal2 - timeVal1;
            }else {
                diff = timeVal1 - timeVal2;
            }
            hours = (diff / (60 * 60 * 1000));
        }catch (ParseException e){
            e.printStackTrace();
        }
        if (hours >= 1){
            return true;
        }else {
            return false ;
        }
    }

    /**
     * 计算时间间隔
     * @param time1
     * @param time2
     * @return
     */
    public static String calTimeInterval(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1;
        Date date2;
        long days = 0;
        long hours = 0;
        long mins = 0;
        long secs = 0;
        try {
            date1 = sdf.parse(time1);
            date2 = sdf.parse(time2);
            long timeVal1 = date1.getTime();
            long timeVal2 = date2.getTime();
            long diff;
            if (timeVal1<timeVal2){
                diff = timeVal2 - timeVal1;
            }else {
                diff = timeVal1 - timeVal2;
            }
            days = diff/(24 * 60 * 60 * 1000);
            hours = (diff / (60 * 60 * 1000) - days * 24);
            mins = ((diff / (60 * 1000)) - days * 24 * 60 - hours * 60);
            secs = (diff / 1000 - days * 24 * 60 * 60 - hours * 60 * 60 - mins * 60);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return "时间间隔为"+days+"天"+hours+"小时"+mins+"分"+secs+"秒";
    }
}
