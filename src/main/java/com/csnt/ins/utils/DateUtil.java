package com.csnt.ins.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author cloud
 * @date 2017/12/12
 */
public class DateUtil {
    //精确地3位毫秒
    public static final String FORMAT_YYYYM_MDDH_HMMSSSSS = "yyyyMMddHHmmssSSS";
    //精确地0位毫秒
    public static final String FORMAT_YYYYM_MDDH_HMMSS = "yyyyMMddHHmmss";
    //精确地3位毫秒
    public static final String FORMAT_YYYY_MM_DD_H_HMMSSSSS = "yyyy-MM-dd HH:mm:ss.SSS";
    //精确地0位毫秒
    public static final String FORMAT_YYYY_MM_DD_H_HMMSS = "yyyy-MM-dd HH:mm:ss";
    //精确地0位毫秒
    public static final String FORMAT_YYYY_MM_DDTH_HMMSS = "yyyy-MM-dd'T'HH:mm:ss";
    /**
     * 精确到天
     */
    public static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    /**
     * 精确到天
     */
    public static final String FORMAT_YYYYM_MDD = "yyyyMMdd";
    /**
     * 精确到天
     */
    public static final String FORMAT_YYM_MDD = "yyMMdd";
    /**
     * yyyy/MM/dd的时间路径
     */
    public static final String FORMAT_YYYY_MM_DD_PATH = "yyyy/MM/dd";

    /**
     * yyyy/MM/dd的时间路径
     */
    public static final String FORMAT_HHMMSS = "HH:mm:ss";
    /**
     * GMT+8的时区
     */
    public static final String FORMAT_GMT8 = "GMT+8";

    /**
     * 格式化日期对象
     *
     * @param date   日期对象
     * @param format 格式化字符串
     * @return
     */
    public static String formatDate(Date date, String format) {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (date == null) {
            result = sdf.format(new Date());
        } else {
            result = sdf.format(date);
        }
        return result;
    }
    /**
     * 格式化日期对象
     *
     * @param date   日期对象
     * @param format 格式化字符串
     * @return
     */
    public static Date formatDateTime(Date date, String format) {
        Date result=null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (date == null) {
            try {
                result = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                result = sdf.parse(sdf.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取当前的时间yyyyMMddHHmmssSSS
     * @update by duwanjiang 停留1毫秒,保证每次调用都不会获取相同的时间
     * @return
     */
    public synchronized static String getCurrentTime_yyyyMMddHHmmssSSS(){
        try{
            Thread.sleep(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return formatDate(new Date(), FORMAT_YYYYM_MDDH_HMMSSSSS);
    }

    /**
     * 时间格式化yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentTime_yyyy_MM_dd_HHmmss(){
        return formatDate(new Date(), FORMAT_YYYY_MM_DD_H_HMMSS);
    }
    /**
     * 时间格式化yyyy-MM-dd'T'HH:mm:ss
     * @return
     */
    public static String getCurrentTime_yyyy_MM_ddTHHmmss(){
        return formatDate(new Date(), FORMAT_YYYY_MM_DDTH_HMMSS);
    }
    /**
     * 时间格式化yyyyMMddHHmmss
     * @return
     */
    public static String getCurrentTime_yyyyMMddHHmmss(Date date){
        return formatDate(date, FORMAT_YYYYM_MDDH_HMMSS);
    }
    /**
     * 时间格式化yyyyMMddHHmmssSSS
     * @return
     */
    public static String getCurrentTime_yyyyMMddHHmmssSSS(Date date){
        return formatDate(date, FORMAT_YYYYM_MDDH_HMMSSSSS);
    }

    /**
     * 格式化日期对象
     *
     * @param date   日期对象
     * @param format 格式化字符串
     * @return
     */
    public static Date parseDate(String date, String format) {
        Date result = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (date != null && !("".equals(date))) {
            try {
                result = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    /**
     * 获取时间毫秒差
     * @param startTime
     * @param endTime
     * @return
     */
    public static Long getTimes(Date startTime, Date endTime) {
        return endTime.getTime()-startTime.getTime();
    }

    /**
     * 获取时间差
     *
     * @param startTime 开始时间（date）
     * @param endTime   结束时间（date）
     * @return
     */
    public static String diffTime(Date startTime, Date endTime) {
        return diffTime(endTime.getTime(), startTime.getTime());
    }

    /**
     * 获取时间差
     *
     * @param startTimeStamp 开始时间戳
     * @param endTimeStamp   结束时间戳
     * @return
     */
    public static String diffTime(long startTimeStamp, long endTimeStamp) {
        String result = "";
        long diff = endTimeStamp - startTimeStamp;
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = diff / (1000 * 60 * 60) - days * 24;
        long mins = diff / (1000 * 60) - hours * 60;
        long seconds = diff / (1000) - mins * 60;
        if (days > 0) {
            result += days + "天";
        }
        if (hours > 0) {
            result += hours + "小时";
        }
        if (mins > 0) {
            result += mins + "分";
        }
        if (seconds > 0) {
            result += seconds + "秒";
        }
        if (StringUtil.isEmpty(result)) {
            result = diff + "毫秒";
        }
        return result;
    }

    /**
     * 获取两个时间之间的天数
     * @param startTime
     * @param endTime
     * @return
     */
    public static long diffDays(Date startTime, Date endTime){
        long result = 0;
        long diff = endTime.getTime() - startTime.getTime();
        result = diff / (1000 * 60 * 60 * 24);
        return result;
    }

    /**
     * 日期月份计算
     * @param startDate 基础日期
     * @param monthAmount    增加或减去的月份数
     * @return 返回增加或减去指定月份数后的日期
     */
    public static Date addMonth(Date startDate, int monthAmount){
        return addTime(startDate,monthAmount, Calendar.MONTH);
    }

    /**
     * 日期月份计算
     * @param startDate 基础日期
     * @param monthAmount    增加或减去的月份数
     * @return 返回增加或减去指定月份数后的日期
     */
    public static Date addDays(Date startDate, int monthAmount){
        return addTime(startDate,monthAmount, Calendar.DATE);
    }

    /**
     * 日期计算
     * @param date      基础日期
     * @param amount    天数或月数或年数...
     * @param field     需要计算的值:Calendar.MONTH,Calendar.YEAR,Calendar.DATE,Calendar.WEDNESDAY
     * @return  返回计算后的日期
     */
    public static Date addTime(Date date, int amount, int field){
        Calendar cal= Calendar.getInstance();
        cal.setTime(date);
        cal.add(field,amount);
        return cal.getTime();
    }

    /**
     * 日期月份计算
     * @param str 转换字符串
     * @param formt    转换格式
     * @return 返回增加或减去指定月份数后的日期
     */
    public static Date stringTODate(String str, SimpleDateFormat formt)  {
        try {
            return formt.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized static String format_yyyyMMddHHmmssSSS_now() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

}
