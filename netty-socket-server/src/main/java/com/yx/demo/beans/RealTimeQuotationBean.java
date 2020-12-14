package com.yx.demo.beans;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yinxing
 * @date 2020/11/27 14:36
 * @desc 行情数据
 */

@Data
public class RealTimeQuotationBean implements Serializable {
    private static final long serialVersionUID = 3916583902452064031L;

    private String contName;

    private String contract;

    /**
     * 开盘价（今天）
     */
    private Double open;

    private Double high;

    private Double low;

    /**
     * 昨收价
     */
    private Double close;
    /**
     * 最新价
     */
    private Double last;

    /**
     * 涨跌点数
     */
    private Double upDownPoint;

    /**
     * 涨跌百分比
     */
    private String upDownPercent;

    /**
     * 最高价
     */
    private Double highestPrice;

    /**
     * 最低价
     */
    private Double lowestPrice;

    /**
     * 价格波动单位（1,0.1,0.01）
     */
    private Double priceTick;

    private Long seq;

    private Long time;

    private String tradingDay;

}
