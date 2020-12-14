package com.yx.demo.utils;


import com.yx.demo.enums.DateStyle;
import com.yx.demo.enums.Week;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import javax.management.timer.Timer;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;


public class DateUtil {

    private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>();

    public static final FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.S");

    public static final FastDateFormat format2 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    public static final FastDateFormat formatYYYYMMdd = FastDateFormat.getInstance("yyyyMMddHHmmss");

    private static final Object object = new Object();

    public static final String DATE_FORMAT_SECOND = "yyyy-MM-dd HH:mm:ss";

    public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

    public static final String yyyyMMdd = "yyyyMMdd";

    public static final String HHmmss = "HHmmss";

    public static final String yyyy_MM_dd = "yyyy-MM-dd";

    public static final String yyyyMM = "yyyyMM";

    public static final String SIX_CLOCK = "060000";

    /**
     * 获取SimpleDateFormat
     *
     * @param pattern 日期格式
     * @return SimpleDateFormat对象
     * @throws RuntimeException 异常：非法日期格式
     */
    private static SimpleDateFormat getDateFormat(String pattern) throws RuntimeException {
        SimpleDateFormat dateFormat = threadLocal.get();
        if (dateFormat == null) {
            synchronized (object) {
                if (dateFormat == null) {
                    dateFormat = new SimpleDateFormat(pattern);
                    dateFormat.setLenient(false);
                    threadLocal.set(dateFormat);
                }
            }
        }
        dateFormat.applyPattern(pattern);
        return dateFormat;
    }

    /**
     * 获取日期中的某数值。如获取月份
     *
     * @param date     日期
     * @param dateType 日期格式
     * @return 数值
     */
    private static int getInteger(Date date, int dateType) {
        int num = 0;
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
            num = calendar.get(dateType);
        }
        return num;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期字符串
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期字符串
     */
    private static String addInteger(String date, int dateType, int amount) {
        String dateString = null;
        DateStyle dateStyle = getDateStyle(date);
        if (dateStyle != null) {
            Date myDate = StringToDate(date, dateStyle);
            myDate = addInteger(myDate, dateType, amount);
            dateString = DateToString(myDate, dateStyle);
        }
        return dateString;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     *
     * @param date     日期
     * @param dateType 类型
     * @param amount   数值
     * @return 计算后日期
     */
    private static Date addInteger(Date date, int dateType, int amount) {
        Date myDate = null;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(dateType, amount);
            myDate = calendar.getTime();
        }
        return myDate;
    }

    /**
     * 获取精确的日期
     *
     * @param timestamps 时间long集合
     * @return 日期
     */
    private static Date getAccurateDate(List<Long> timestamps) {
        Date date = null;
        long timestamp = 0;
        Map<Long, long[]> map = new HashMap<Long, long[]>();
        List<Long> absoluteValues = new ArrayList<Long>();

        if (timestamps != null && timestamps.size() > 0) {
            if (timestamps.size() > 1) {
                for (int i = 0; i < timestamps.size(); i++) {
                    for (int j = i + 1; j < timestamps.size(); j++) {
                        long absoluteValue = Math.abs(timestamps.get(i) - timestamps.get(j));
                        absoluteValues.add(absoluteValue);
                        long[] timestampTmp = {timestamps.get(i), timestamps.get(j)};
                        map.put(absoluteValue, timestampTmp);
                    }
                }

                // 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的。此时minAbsoluteValue为0
                // 因此不能将minAbsoluteValue取默认值0
                long minAbsoluteValue = -1;
                if (!absoluteValues.isEmpty()) {
                    minAbsoluteValue = absoluteValues.get(0);
                    for (int i = 1; i < absoluteValues.size(); i++) {
                        if (minAbsoluteValue > absoluteValues.get(i)) {
                            minAbsoluteValue = absoluteValues.get(i);
                        }
                    }
                }

                if (minAbsoluteValue != -1) {
                    long[] timestampsLastTmp = map.get(minAbsoluteValue);

                    long dateOne = timestampsLastTmp[0];
                    long dateTwo = timestampsLastTmp[1];
                    if (absoluteValues.size() > 1) {
                        timestamp = Math.abs(dateOne) > Math.abs(dateTwo) ? dateOne : dateTwo;
                    }
                }
            } else {
                timestamp = timestamps.get(0);
            }
        }

        if (timestamp != 0) {
            date = new Date(timestamp);
        }
        return date;
    }

    /**
     * 判断字符串是否为日期字符串
     *
     * @param date 日期字符串
     * @return true or false
     */
    public static boolean isDate(String date) {
        boolean isDate = false;
        if (date != null) {
            if (getDateStyle(date) != null) {
                isDate = true;
            }
        }
        return isDate;
    }

    /**
     * 获取日期字符串的日期风格。失敗返回null。
     *
     * @param date 日期字符串
     * @return 日期风格
     */
    public static DateStyle getDateStyle(String date) {
        DateStyle dateStyle = null;
        Map<Long, DateStyle> map = new HashMap<Long, DateStyle>();
        List<Long> timestamps = new ArrayList<Long>();
        for (DateStyle style : DateStyle.values()) {
            if (style.isShowOnly()) {
                continue;
            }
            Date dateTmp = null;
            if (date != null) {
                try {
                    ParsePosition pos = new ParsePosition(0);
                    dateTmp = getDateFormat(style.getValue()).parse(date, pos);
                    if (pos.getIndex() != date.length()) {
                        dateTmp = null;
                    }
                } catch (Exception e) {
                }
            }
            if (dateTmp != null) {
                timestamps.add(dateTmp.getTime());
                map.put(dateTmp.getTime(), style);
            }
        }
        Date accurateDate = getAccurateDate(timestamps);
        if (accurateDate != null) {
            dateStyle = map.get(accurateDate.getTime());
        }
        return dateStyle;
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static Date StringToDate(String date) {
        DateStyle dateStyle = getDateStyle(date);
        return StringToDate(date, dateStyle);
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date    日期字符串
     * @param pattern 日期格式
     * @return 日期
     */
    public static Date StringToDate(String date, String pattern) {
        Date myDate = null;
        if (date != null) {
            try {
                myDate = getDateFormat(pattern).parse(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return myDate;
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date      日期字符串
     * @param dateStyle 日期风格
     * @return 日期
     */
    public static Date StringToDate(String date, DateStyle dateStyle) {
        Date myDate = null;
        if (dateStyle != null) {
            myDate = StringToDate(date, dateStyle.getValue());
        }
        return myDate;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date    日期
     * @param pattern 日期格式
     * @return 日期字符串
     */
    public static String DateToString(Date date, String pattern) {
        String dateString = null;
        if (date != null) {
            try {
                dateString = getDateFormat(pattern).format(date);
            } catch (Exception e) {

            }
        }
        return dateString;
    }

    /**
     * Date 转 String(yyyy-MM-dd HH:mm:ss)
     *
     * @param date
     * @return
     */
    public static String getStringDate(Date date) {
        String dateString = null;
        if (date != null) {
            try {
                dateString = getDateFormat(DATE_FORMAT_SECOND).format(date);
            } catch (Exception e) {

            }
        }
        return dateString;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date      日期
     * @param dateStyle 日期风格
     * @return 日期字符串
     */
    public static String DateToString(Date date, DateStyle dateStyle) {
        String dateString = null;
        if (dateStyle != null) {
            dateString = DateToString(date, dateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date       旧日期字符串
     * @param newPattern 新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, String newPattern) {
        DateStyle oldDateStyle = getDateStyle(date);
        return StringToString(date, oldDateStyle, newPattern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param newDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, DateStyle newDateStyle) {
        DateStyle oldDateStyle = getDateStyle(date);
        return StringToString(date, oldDateStyle, newDateStyle);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date        旧日期字符串
     * @param olddPattern 旧日期格式
     * @param newPattern  新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, String olddPattern, String newPattern) {
        return DateToString(StringToDate(date, olddPattern), newPattern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param olddDteStyle 旧日期风格
     * @param newParttern  新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, DateStyle olddDteStyle, String newParttern) {
        String dateString = null;
        if (olddDteStyle != null) {
            dateString = StringToString(date, olddDteStyle.getValue(), newParttern);
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param olddPattern  旧日期格式
     * @param newDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, String olddPattern, DateStyle newDateStyle) {
        String dateString = null;
        if (newDateStyle != null) {
            dateString = StringToString(date, olddPattern, newDateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     *
     * @param date         旧日期字符串
     * @param olddDteStyle 旧日期风格
     * @param newDateStyle 新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, DateStyle olddDteStyle, DateStyle newDateStyle) {
        String dateString = null;
        if (olddDteStyle != null && newDateStyle != null) {
            dateString = StringToString(date, olddDteStyle.getValue(), newDateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期字符串
     */
    public static String addYear(String date, int yearAmount) {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的年份。失败返回null。
     *
     * @param date       日期
     * @param yearAmount 增加数量。可为负数
     * @return 增加年份后的日期
     */
    public static Date addYear(Date date, int yearAmount) {
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date        日期
     * @param monthAmount 增加数量。可为负数
     * @return 增加月份后的日期字符串
     */
    public static String addMonth(String date, int monthAmount) {
        return addInteger(date, Calendar.MONTH, monthAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     *
     * @param date        日期
     * @param monthAmount 增加数量。可为负数
     * @return 增加月份后的日期
     */
    public static Date addMonth(Date date, int monthAmount) {
        return addInteger(date, Calendar.MONTH, monthAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期字符串
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期字符串
     */
    public static String addDay(String date, int dayAmount) {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     *
     * @param date      日期
     * @param dayAmount 增加数量。可为负数
     * @return 增加天数后的日期
     */
    public static Date addDay(Date date, int dayAmount) {
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期字符串
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期字符串
     */
    public static String addHour(String date, int hourAmount) {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     *
     * @param date       日期
     * @param hourAmount 增加数量。可为负数
     * @return 增加小时后的日期
     */
    public static Date addHour(Date date, int hourAmount) {
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date         日期字符串
     * @param minuteAmount 增加数量。可为负数
     * @return 增加分钟后的日期字符串
     */
    public static String addMinute(String date, int minuteAmount) {
        return addInteger(date, Calendar.MINUTE, minuteAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     *
     * @param date         日期
     * @param minuteAmount 增加数量。可为负数
     * @return 增加分钟后的日期
     */
    public static Date addMinute(Date date, int minuteAmount) {
        return addInteger(date, Calendar.MINUTE, minuteAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date         日期字符串
     * @param secondAmount 增加数量。可为负数
     * @return 增加秒钟后的日期字符串
     */
    public static String addSecond(String date, int secondAmount) {
        return addInteger(date, Calendar.SECOND, secondAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     *
     * @param date         日期
     * @param secondAmount 增加数量。可为负数
     * @return 增加秒钟后的日期
     */
    public static Date addSecond(Date date, int secondAmount) {
        return addInteger(date, Calendar.SECOND, secondAmount);
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期字符串
     * @return 年份
     */
    public static int getYear(String date) {
        return getYear(StringToDate(date));
    }

    /**
     * 获取日期的年份。失败返回0。
     *
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        return getInteger(date, Calendar.YEAR);
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期字符串
     * @return 月份
     */
    public static int getMonth(String date) {
        return getMonth(StringToDate(date));
    }

    /**
     * 获取日期的月份。失败返回0。
     *
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        return getInteger(date, Calendar.MONTH) + 1;
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期字符串
     * @return 天
     */
    public static int getDay(String date) {
        return getDay(StringToDate(date));
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期
     * @return 天
     */
    public static int getDay(Date date) {
        return getInteger(date, Calendar.DATE);
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期字符串
     * @return 小时
     */
    public static int getHour(String date) {
        return getHour(StringToDate(date));
    }

    /**
     * 获取日期的小时。失败返回0。
     *
     * @param date 日期
     * @return 小时
     */
    public static int getHour(Date date) {
        return getInteger(date, Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 分钟
     */
    public static int getMinute(String date) {
        return getMinute(StringToDate(date));
    }

    /**
     * 获取日期的分钟。失败返回0。
     *
     * @param date 日期
     * @return 分钟
     */
    public static int getMinute(Date date) {
        return getInteger(date, Calendar.MINUTE);
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期字符串
     * @return 秒钟
     */
    public static int getSecond(String date) {
        return getSecond(StringToDate(date));
    }

    /**
     * 获取日期的秒钟。失败返回0。
     *
     * @param date 日期
     * @return 秒钟
     */
    public static int getSecond(Date date) {
        return getInteger(date, Calendar.SECOND);
    }

    /**
     * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 日期
     */
    public static String getDate(String date) {
        return StringToString(date, DateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期。默认yyyy-MM-dd格式。失败返回null。
     *
     * @param date 日期
     * @return 日期
     */
    public static String getDate(Date date) {
        return DateToString(date, DateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期字符串
     * @return 时间
     */
    public static String getTime(String date) {
        return StringToString(date, DateStyle.HH_MM_SS);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     *
     * @param date 日期
     * @return 时间
     */
    public static String getTime(Date date) {
        return DateToString(date, DateStyle.HH_MM_SS);
    }

    /**
     * 获取日期的星期。失败返回null。
     *
     * @param date 日期字符串
     * @return 星期
     */
    public static Week getWeek(String date) {
        Week week = null;
        DateStyle dateStyle = getDateStyle(date);
        if (dateStyle != null) {
            Date myDate = StringToDate(date, dateStyle);
            week = getWeek(myDate);
        }
        return week;
    }

    /**
     * 获取日期的星期。失败返回null。
     *
     * @param date 日期
     * @return 星期
     */
    public static Week getWeek(Date date) {
        Week week = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekNumber = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        switch (weekNumber) {
            case 0:
                week = Week.SUNDAY;
                break;
            case 1:
                week = Week.MONDAY;
                break;
            case 2:
                week = Week.TUESDAY;
                break;
            case 3:
                week = Week.WEDNESDAY;
                break;
            case 4:
                week = Week.THURSDAY;
                break;
            case 5:
                week = Week.FRIDAY;
                break;
            case 6:
                week = Week.SATURDAY;
                break;
        }
        return week;
    }

    /**
     * 获取两个日期相差的天数
     *
     * @param date      日期字符串
     * @param otherDate 另一个日期字符串
     * @return 相差天数。如果失败则返回-1
     */
    public static int getIntervalDays(String date, String otherDate) {
        return getIntervalDays(StringToDate(date), StringToDate(otherDate));
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数。如果失败则返回-1
     */
    public static int getIntervalDays(Date date, Date otherDate) {
        int num = -1;
        long time = Math.abs(getIntervalTime(date, otherDate));
        num = (int) (time / (24 * 60 * 60 * 1000));
        return num;
    }

    /**
     * @param date      日期 有正负符号
     * @param otherDate
     * @return
     */
    public static int getIntervalDaysWithSymbol(Date date, Date otherDate) {
        int num = -1;
        long time = getIntervalTime(date, otherDate);
        num = (int) (time / (24 * 60 * 60 * 1000));
        return num;
    }

    /**
     * 计算两个日期间隔
     *
     * @param date
     * @param otherDate
     * @return
     */
    private static long getIntervalTime(Date date, Date otherDate) {
        long time = 0;
        Date dateTmp = StringToDate(DateUtil.getDate(date), DateStyle.YYYY_MM_DD);
        Date otherDateTmp = StringToDate(DateUtil.getDate(otherDate), DateStyle.YYYY_MM_DD);
        if (dateTmp != null && otherDateTmp != null) {
            time = dateTmp.getTime() - otherDateTmp.getTime();
        }
        return Math.abs(time);
    }

    /**
     * 计算两个日期大小
     *
     * @param date
     * @param otherDate
     * @return
     */
    public static int compareTime(Date date, Date otherDate) {
        long time = 0;
        if (date != null && otherDate != null) {
            time = date.getTime() - otherDate.getTime();
        }
        if (time > 0) {
            return 1;
        } else if (time < 0) {
            return -1;
        }
        return 0;
    }

    /**
     * 获取指定日期 指定小时的日期
     *
     * @param date
     * @param hour
     * @return
     */
    public static Date getDate(Date date, int hour) {
        if (hour > 23 || hour < 0) {
            return date;
        }
        String datetime = DateToString(date, DateStyle.YYYY_MM_DD);
        StringBuilder builder = new StringBuilder(datetime);
        builder.append(" ").append(hour).append(":00:00");
        return DateUtil.StringToDate(builder.toString());
    }

    public static int getDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return dayOfMonth;
    }

    /**
     * 获取当前周几
     *
     * @param date
     * @return
     */
    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekNumber = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return weekNumber;
    }

    public static int getLocalDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayofmonth = calendar.get(Calendar.DAY_OF_MONTH);
        LocalDate localDate = LocalDate.of(year, month, dayofmonth);
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return dayOfWeek.getValue();
    }

    /**
     * @param year
     * @param month
     * @return
     */
    public static int getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month + 1);
        // 某年某月的最后一天
        return cal.getActualMaximum(Calendar.DATE);
    }

    public static Date getFirstDayOfMonth(Date end) {
        int year = getYear(end);
        int month = getMonth(end);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month + 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 根据指定年月的第一天
     * @param year
     * @param month
     * @return
     */
    public static String getFirstDateByYearMonth(String year, String month) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(year);
        stringBuilder.append("-");
        stringBuilder.append(month);
        stringBuilder.append("-01");
        return stringBuilder.toString();
    }

    /**
     * 根据指定年月的最后一天
     * @param year
     * @param month
     * @return
     */
    public static String getLastDateByYearMonth(String year, String month) {
        int lastDay = DateUtil.getLastDayOfMonth(Integer.valueOf(year), Integer.valueOf(month));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(year);
        stringBuilder.append("-");
        stringBuilder.append(month);
        stringBuilder.append("-");
        stringBuilder.append(lastDay);
        return stringBuilder.toString();
    }


    public static Date setHour(Date date, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, hour);
        return cal.getTime();
    }

    public static Date setMinute(Date date, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTime();
    }

    public static Date setHourMinute(Date date, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTime();
    }

    public static Date setDay(Date date, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, day);
        return cal.getTime();
    }

    /**
     * 两个时间相差小时(8点-8点=0小时,8点-9点=1小时)
     *
     * @param small
     * @param big
     * @return
     */
    public static int differHour(Date small, Date big) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        format(small, big, sdf);
        long diff = big.getTime() - small.getTime();
        if (diff <= 0) {
            return 0;
        }
        return (int) (diff / Timer.ONE_HOUR);
    }

    public static int differHour2(String startDate, String endDate) {
        long endTime = getLongTime(endDate);
        long startTime = getLongTime(startDate);
        long diff = endTime - startTime;
        if (diff <= 0) {
            return 0;
        }
        return (int) (diff / Timer.ONE_HOUR);
    }

    public static int differHour3(long startTime, long endTime) {
        long diff = endTime - startTime;
        if (diff <= 0){
            return 0;
        }
        return (int) (diff / Timer.ONE_HOUR);
    }

    /**
     * 两个时间相差天数(1号-1号=1天,1号-2号=2天)
     *
     * @param small
     * @param big
     * @return
     */
    public static int differDay(Date small, Date big) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        format(small, big, sdf);
        long diff = big.getTime() - small.getTime();
        if (diff == 0) {
            return 1;
        }
        if (diff < 0) {
            return 0;
        }
        return (int) (diff / Timer.ONE_DAY) + 1;
    }

    /**
     * 两个时间相差天数(1号-1号=1天,1号-2号=2天)
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static int differDay2(long startTime, long endTime) {
        long diff = endTime - startTime;
        if (diff == 0) {
            return 1;
        }
        if (diff < 0) {
            return 0;
        }
        return (int) (diff / Timer.ONE_DAY) + 1;
    }

    private static void format(Date small, Date big, SimpleDateFormat sdf) {
        try {
            sdf.parse(sdf.format(small));
            sdf.parse(sdf.format(big));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("时间格式化错误:%s", e.getMessage()));
        }
    }

    /**
     * 获取当前日前的前n天
     *
     * @param d
     * @param day
     * @return
     */
    public static Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    /**
     * 获取当前日前的后n天
     *
     * @param d
     * @param day
     * @return
     */
    public static Date getDateAfter(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTime();
    }

    public static Date format(Date date, DateStyle dateStyle) {
        Date formatDate = date;
        if (date != null) {
            String strDate = DateUtil.DateToString(date, dateStyle);
            formatDate = DateUtil.StringToDate(strDate, dateStyle);
        }
        return formatDate;
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        return timeStamp2Date(Long.valueOf(seconds + "000"), format);
    }

    public static String timeStamp2Date(Long seconds, String format) {
        if (format == null || format.isEmpty()){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    /**
     * 时间戳转字符串
     * @param milliseconds 精确到毫秒
     * @param format 不传默认yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String milliTimeStamp2Date(Long milliseconds, String format) {
        if(milliseconds == null || milliseconds < 0){
            return "";
        }
        if (format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(milliseconds));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return
     */
    public static String timeStamp() {
        long time = System.currentTimeMillis();
        String t = String.valueOf(time / 1000);
        return t;
    }

    /**
     * @param date 格式：20171108090200
     * @return 返回long date
     */
    public static long getLongTime(String date) {
        return getLongDate(date, yyyyMMddHHmmss);
    }

    /**
     * @param date 格式：2019-10-08 09:02:13
     * @return 返回long date
     */
    public static long getLongTime2(String date) {
        return getLongDate(date, DATE_FORMAT_SECOND);
    }

    /**
     * @param dateStr 字符串
     * @param pattern 格式
     * @return 返回 long 时间戳
     */
    public static long getLongDate(String dateStr, String pattern) {
        Date date = null;
        try {
            date = getDateFormat(pattern).parse(dateStr);
        } catch (ParseException e) {
            System.out.println("dateStr format is error!");
        }
        if (date != null) {
            return date.getTime();
        } else {
            return 0;
        }
    }

    /**
     *
     * @param pattern 默认返回yyyy-MM-dd HH:mm:ss格式
     * @return
     */
    public static String getCurrentDate(String pattern) {
        pattern = StringUtils.isEmpty(pattern)?DATE_FORMAT_SECOND:pattern;
        return getDateFormat(pattern).format(System.currentTimeMillis());
    }

    /**
     * 获取当前月份
     *
     * @return yyyyMM
     */
    public static String getCurrentMonth() {
        return getCurrentDate(yyyyMMdd).substring(0, 6);
    }

    public static String getCurrentYear() {
        return getCurrentDate(yyyyMMdd).substring(0, 4);
    }

    /**
     * 获取当前日期
     *
     * @return 返回 yyyy-MM-dd 格式的当前日期
     */
    public static String getCurrentDate2() {
        return getDateFormat(yyyy_MM_dd).format(System.currentTimeMillis());
    }

    /**
     * 获取当前时间
     *
     * @return 返回HHmmss格式的当前时间
     */
    public static String getCurrentTime() {
        return getDateFormat(HHmmss).format(System.currentTimeMillis());
    }

    public static int checkWeekend() {
        int flag = 0;
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            flag = -1;
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            flag = -2;
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            flag = 1;
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            flag = 2;
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            flag = 3;
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            flag = 4;
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            flag = 5;
        }
        return flag;
    }

    public static String getDayofWeek(String timeStr) {
        int dayofweek = DateUtil.getDayOfWeek(DateUtil.StringToDate(timeStr));
        return getDayofWeek(dayofweek);
    }

    public static String getDayofWeek(int dayofweek) {
        String dayofweekCN = null;
        switch (dayofweek) {
            case 1:
                dayofweekCN = "周一";
                break;
            case 2:
                dayofweekCN = "周二";
                break;
            case 3:
                dayofweekCN = "周三";
                break;
            case 4:
                dayofweekCN = "周四";
                break;
            case 5:
                dayofweekCN = "周五";
                break;
            case 6:
                dayofweekCN = "周六";
                break;
            case 7:
                dayofweekCN = "周日";
                break;
            default:
                dayofweekCN = "周一";
                break;
        }
        return dayofweekCN;
    }


    /**
     * 当前日志往前或后推N天
     *
     * @param day 日数
     * @return yyyyMMdd
     */
    public static String getPrevDate(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        return getDateFormat(yyyyMMdd).format(cal.getTime());
    }

    /**
     * 获取上个月
     *
     * @return yyyyMM
     */
    public static String getLastMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return getDateFormat(yyyyMM).format(cal.getTime());
    }

    /**
     * 获取当前周的第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(7);
        try {
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cal.getTime();
    }

    /**
     * /**
     * 获取当前周最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(7);
        try {
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 6);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cal.getTime();
    }

    /**
     * 获取当前月第一天
     *
     * @return
     */
    public static String getCurrentMonthFirstDay() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        return format.format(cale.getTime());
    }

    /**
     * 获取当前月第一天
     *
     * @return
     */
    public static String getCurrentMonthFirstDay2() {
        SimpleDateFormat format = new SimpleDateFormat(yyyyMMdd);
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        return format.format(cale.getTime());
    }

    public static String getFirstDayOfCurrMonth() {
        SimpleDateFormat format = getDateFormat(yyyyMMdd);
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        return format.format(cale.getTime());
    }

    /**
     * 获取当前月最后一天
     *
     * @return
     */
    public static String getCurrentMonthLastDay() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        return format.format(cale.getTime());
    }

    /**
     * @param ms
     * @return 返回yyyy-MM-dd HH:mm:ss.S格式的时间
     */
    public static String transForDate(long ms) {
        return format.format(ms);
    }

    /**
     * @param ms
     * @return 返回yyyy-MM-dd HH:mm:ss格式的时间
     */
    public static String transForDate2(long ms) {
        return format2.format(ms);
    }

    /**
     * 获取
     *
     * @param ms
     * @return yyyyMMddHHmmss 格式日期
     */
    public static String getYyyyMMddHHmmss(long ms) {
        return formatYYYYMMdd.format(ms);
    }

    /**
     * 某一天往前推n天
     *
     * @param dateStr
     * @param day
     * @return yyyyMMdd
     */
    public static String getPreDate2(String dateStr, int day) {
        Calendar cal = Calendar.getInstance();
        Date date = null;
        try {
            date = getDateFormat(yyyyMMdd).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.setTime(date);
        cal.add(Calendar.DATE, day);
        String prevDate = getDateFormat(yyyyMMdd).format(cal.getTime());
        return prevDate;
    }

    /**
     * 计算5分钟的整数倍时间点(往后推取整)
     *
     * @param n 1-5分钟,2-10分钟，3-15分钟，6-30分钟，12-1小时
     * @return
     */
    public static long getFiveTimesMinPoint(int n) {
        Calendar rightNow = Calendar.getInstance();
        int minute = rightNow.get(Calendar.MINUTE);
        minute = Math.round(minute / (n * 5) * n * 5) + n * 5;
        rightNow.set(Calendar.MINUTE, minute);
        rightNow.set(Calendar.SECOND, 0);
        return rightNow.getTimeInMillis();
    }

    public static String getFiveTimesMinPointStr(int n) {
        return getYyyyMMddHHmmss(getFiveTimesMinPoint(n));
    }

    public static long getOneTimesMinPoint(int n) {
        Calendar rightNow = Calendar.getInstance();
        int minute = rightNow.get(Calendar.MINUTE);
        minute = Math.round(minute) + n;
        rightNow.set(Calendar.MINUTE, minute);
        rightNow.set(Calendar.SECOND, 0);
        return rightNow.getTimeInMillis();
    }

    public static String getOneTimesMinPointStr(int n) {
        long times = getOneTimesMinPoint(n);
        return getYyyyMMddHHmmss(times);
    }

    /**
     * 往前推N分钟
     *
     * @param date
     * @param min
     * @return yyyyMMddHHmmss
     */
    public static long getLongPreMinTime(String date, int min) {
        if (StringUtils.isEmpty(date)) {
            return 0;
        }
        Date da = StringToDate(date, yyyyMMddHHmmss);
        if (da != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(da);
            calendar.add(Calendar.MINUTE, min);
            return calendar.getTimeInMillis();
        }
        return 0;
    }

    public static String getTradingDay(long createTime) {
        String day = getYyyyMMddHHmmss(createTime).substring(0, 8);
        long openTime = getLongTime(day.concat(SIX_CLOCK));
        if (createTime >= openTime) {
            return day;
        } else {
            return getPreDate2(day, -1);
        }
    }

    /**
     * 获取当前的时间
     *
     * @return yyyyMMddHHmmss
     */
    public static String getCurrentyyyyMMddHHmmss() {
        return formatYYYYMMdd.format(System.currentTimeMillis());
    }


    /**
     * 获取当前的时间
     *
     * @return yyyyMMddHHmmss
     */
    public static String getCurrentyyyyMMddHHmmss2(int min) {
        return formatYYYYMMdd.format(System.currentTimeMillis() + min * 60 * 1000);
    }


    public static long getFifteenTimesMinPoint(int n) {
        Calendar rightNow = Calendar.getInstance();
        int minute = rightNow.get(Calendar.MINUTE);
        minute = Math.round(minute / (n * 15) * n * 15) + n * 15;
        rightNow.set(Calendar.MINUTE, minute);
        rightNow.set(Calendar.SECOND, 0);
        return rightNow.getTimeInMillis();
    }

    public static String getFifteenTimesMinPointStr(int n) {
        return getYyyyMMddHHmmss(getFifteenTimesMinPoint(n));
    }

    public static long getThirtyTimesMinPoint(int n) {
        Calendar rightNow = Calendar.getInstance();
        int minute = rightNow.get(Calendar.MINUTE);
        minute = Math.round(minute / (n * 30) * n * 30) + n * 30;
        rightNow.set(Calendar.MINUTE, minute);
        rightNow.set(Calendar.SECOND, 0);
        return rightNow.getTimeInMillis();
    }

    public static String getThirtyTimesMinPointStr(int n) {
        return getYyyyMMddHHmmss(getThirtyTimesMinPoint(n));
    }

    public static long getHourTimesMinPoint(int n) {
        Calendar rightNow = Calendar.getInstance();
        int minute = rightNow.get(Calendar.MINUTE);
        minute = Math.round(minute / (n * 60) * n * 60) + n * 60;
        rightNow.set(Calendar.MINUTE, minute);
        rightNow.set(Calendar.SECOND, 0);
        return rightNow.getTimeInMillis();
    }

    public static String getHourTimesMinPointStr(int n) {
        return getYyyyMMddHHmmss(getHourTimesMinPoint(n));
    }

    public static void main(String[] args) {
        System.out.println(transForDate(1605885010621L));

        System.out.println(transForDate(1605893033763L));
//
//        System.out.println(transForDate(1605574800000L));

//        System.out.println(transForDate2(System.currentTimeMillis()));

//        String time=getCurrentyyyyMMddHHmmss2(1).substring(0,12);
//        System.out.println(time);
//
//        long time1=getLongTime("20201120060000");
//        System.out.println(time1);
//        long time2=getLongTime("20201003021958");
//        System.out.println(time2);

//        System.out.println(getFiveTimesMinPointStr(1));
//        System.out.println(getFifteenTimesMinPointStr(1));
//        System.out.println(getThirtyTimesMinPointStr(1));
//        System.out.println(getHourTimesMinPointStr(1));
//        long tim=System.currentTimeMillis();
//        System.out.println(getYyyyMMddHHmmss(tim));
//        System.out.println(getCurrentMonthFirstDay2());
//
//        long sysEndTime=DateUtil.getFiveTimesMinPoint(1);
//        System.out.println(transForDate2(sysEndTime));
//
//        String sysEndTimeStr=DateUtil.getFiveTimesMinPointStr(3);
//        System.out.println(sysEndTimeStr);
//
//        long sysEndTime=DateUtil.getFiveTimesMinPoint(1);
//        System.out.println(getYyyyMMddHHmmss(sysEndTime));
//
//        System.out.println(getCurrentyyyyMMddHHmmss());
//        System.out.println(getCurrentyyyyMMddHHmmss2(1));
//        String time=DateUtil.getCurrentyyyyMMddHHmmss2(1).substring(0,12);
//        String periodWithDate=time.concat("00");
//        System.out.println(periodWithDate);
//        System.out.println(getYyyyMMddHHmmss(1578896700682L));
//        System.out.println(getLastMonth());
//        System.out.println(getStringDate(new Date()));
//
//        long tt=getOneTimesMinPoint(0);
//        System.out.println(tt);
//        System.out.println(transForDate2(tt));
//        String os2=getOneTimesMinPointStr(0);
//        System.out.println(os2);
//        System.out.println(getLongTime(os2));
//
//        long openTime=DateUtil.getLongTime(day.concat("060000"));
//        System.out.println(openTime);
//        System.out.println(transForDate2(openTime));
//        System.out.println(UUID.randomUUID());
//        String day="20200301";
//        System.out.println(getPreDate2(day,30));
//
//        String date="2020-04-15 17:45:48";
//        System.out.println(getLongTime2(date));
//
//        System.out.println(differHour2("20200826142339","20200827060000"));
//        System.out.println(getCurrentYear());
//        System.out.println(getCurrentyyyyMMddHHmmss().substring(0,12));
    }
}