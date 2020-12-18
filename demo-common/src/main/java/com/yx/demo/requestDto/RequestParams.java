package com.yx.demo.requestDto;

import com.yx.demo.utils.RequestUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author yinxing
 * @date 2020/11/2 14:57
 * @desc
 */

@Data
public class RequestParams<T> implements Serializable {
    private String requestId;//请求编号，商户编号-时间戳-随机6位数字字母
    private String platform = "server";//平台
    private String deviceId;//设备编号
    private String timestamp;//时间戳
    private String version = "1.0.0.0";//请求编号
    private String memberChannel;//商户编号
    private String appKey;//设备令牌
    private String exchange = "xinyu";//交易所标记
    private String accessToken;//访问令牌
    private String sign;//签名
    private T data;
    private RequestPage page;

    public RequestParams() {
        Long timestamp = System.currentTimeMillis();
        this.timestamp = timestamp.toString();
        this.requestId = RequestUtil.generateRequestId(timestamp);
        this.deviceId = RequestUtil.getDeviceId();
        this.memberChannel = RequestUtil.getMemberChannel();
        this.appKey = RequestUtil.getAppKey();
    }

    public RequestParams(T data) {
        Long timestamp = System.currentTimeMillis();
        this.timestamp = timestamp.toString();
        this.requestId = RequestUtil.generateRequestId(timestamp);
        this.deviceId = RequestUtil.getDeviceId();
        this.memberChannel = RequestUtil.getMemberChannel();
        this.appKey = RequestUtil.getAppKey();
        this.data = data;
    }

    public RequestParams(String accessToken) {
        Long timestamp = System.currentTimeMillis();
        this.timestamp = timestamp.toString();
        this.requestId = RequestUtil.generateRequestId(timestamp);
        this.deviceId = RequestUtil.getDeviceId();
        this.memberChannel = RequestUtil.getMemberChannel();
        this.appKey = RequestUtil.getAppKey();
        this.sign = sign;
        if (StringUtils.isNotBlank(accessToken)) {
            this.accessToken = accessToken;
        }
    }

    public RequestParams(T data, String accessToken) {
        Long timestamp = System.currentTimeMillis();
        this.timestamp = timestamp.toString();
        this.requestId = RequestUtil.generateRequestId(timestamp);
        this.deviceId = RequestUtil.getDeviceId();
        this.memberChannel = RequestUtil.getMemberChannel();
        this.appKey = RequestUtil.getAppKey();
        this.sign = sign;
        this.data = data;
        if (StringUtils.isNotBlank(accessToken)) {
            this.accessToken = accessToken;
        }
    }
}

