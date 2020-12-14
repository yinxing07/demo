package com.yx.demo.utils;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;

/**
 * @author yinxing
 * @date 2020/11/27 15:42
 * @desc
 */

@Component
public class NumberUtil {

    /**
     * 结算百分比
     *
     * @param d1
     * @param d2
     * @return String类型百分数
     */
    public static String getPercent(double d1, double d2) {
        if (d1 == 0.0 || d2 == 0.0) {
            return "0.00%";
        }
        double percent = d2 / d1;
        NumberFormat nt = NumberFormat.getPercentInstance();
        //设置百分数精确度2即保留两位小数
        nt.setMinimumFractionDigits(2);
        return nt.format(percent);
    }
}
