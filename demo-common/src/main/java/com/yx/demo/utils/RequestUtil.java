package com.yx.demo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.yx.demo.requestDto.GatewayConfig;
import com.yx.demo.requestDto.RequestParams;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author yinxing
 * @date 2020/11/2 14:56
 * @desc
 */

@Data
@Component
public class RequestUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtil.class);
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final String CHAR_SEED = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MID_SPLIT_CHAR = "-";//访问令牌
    private static final String EXCLUSION_KEY = "data";

    private final static AtomicInteger ATOMIC_COUNT = new AtomicInteger(0);

    @Value("${spring.application.name}")
    private String appName;
    private static String applicationName;
    @Autowired
    private GatewayConfig gatewayConfig;
    private static GatewayConfig config;

    @PostConstruct
    public void init() {
        config = this.gatewayConfig;
        applicationName = this.appName;
    }

    public static <T> T postJSON(String apiName, RequestParams request, Class<T> clz) throws RuntimeException {
        String sign = RequestUtil.getSign(apiName, request);
        if (StringUtils.isBlank(sign)) {
            throw new RuntimeException();
        }
        request.setSign(sign);
        String url = RequestUtil.getURL(apiName);
        return RestTemplateUtil.post(url, request, MediaType.APPLICATION_JSON_UTF8, clz);
    }

    public static <T> T postJSON(String apiName, RequestParams request, TypeReference<T> typeReference) throws RuntimeException {
        String sign = RequestUtil.getSign(apiName, request);
        if (StringUtils.isBlank(sign)) {
            throw new RuntimeException();
        }
        request.setSign(sign);
        String url = RequestUtil.getURL(apiName);
        LOGGER.info("请求接口地址：" + url);
        LOGGER.info("请求参数：" + JSON.toJSONString(request));
        String resultJson = RestTemplateUtil.post(url, request, MediaType.APPLICATION_JSON_UTF8, String.class);
        LOGGER.info("返回结果：{}", resultJson);
        T result = JSON.parseObject(resultJson, typeReference);
        return result;
    }

    public static <T> T postJSON(String apiName, RequestParams request, ParameterizedTypeReference<T> responseBodyType) throws RuntimeException {
        String sign = RequestUtil.getSign(apiName, request);
        if (StringUtils.isBlank(sign)) {
            throw new RuntimeException();
        }
        request.setSign(sign);
        String url = RequestUtil.getURL(apiName);
        T resultJson = RestTemplateUtil.postJSON(url, request, responseBodyType);
        return resultJson;
    }

    public static <T> ResponseEntity<T> postJSONResponseEntity(String apiName, RequestParams request, ParameterizedTypeReference<T> responseBodyType) throws RuntimeException {
        String sign = RequestUtil.getSign(apiName, request);
        if (StringUtils.isBlank(sign)) {
            throw new RuntimeException();
        }
        request.setSign(sign);
        String url = RequestUtil.getURL(apiName);
        ResponseEntity<T> responseEntity = RestTemplateUtil.postJSONResponseEntity(url, request, responseBodyType);
        return responseEntity;
    }

    public static String getSign(Object data) {
        String dataStr = JSONObject.toJSONString(data);
        if (StringUtils.isBlank(dataStr)) {
            return "";
        }
        Map<String, String> parameterMap = JSONObject.parseObject(dataStr, Map.class);
        List<String> keys = new ArrayList(parameterMap.keySet());
        keys = keys.stream().filter(key -> StringUtils.isNotBlank(key)).collect(Collectors.toList());
        Collections.sort(keys);//排序
        return getSign(null, keys, parameterMap);
    }

    /**
     * 获取签名
     *
     * @param apiName
     * @param data
     * @return
     */
    public static String getSign(String apiName, Object data) {
        String dataStr = JSONObject.toJSONString(data);
        if (StringUtils.isBlank(dataStr)) {
            return "";
        }
        Map<String, String> parameterMap = JSONObject.parseObject(dataStr, Map.class);
        Map<String, String> dataContentMap = null;
        if (parameterMap.get(EXCLUSION_KEY) != null) {
            String dataContentStr = JSONObject.toJSONString(parameterMap.get(EXCLUSION_KEY));
            dataContentMap = JSONObject.parseObject(dataContentStr, Map.class);
        }
        if (!CollectionUtils.isEmpty(dataContentMap)) {
            parameterMap.putAll(dataContentMap);
        }
        List<String> keys = new ArrayList(parameterMap.keySet());
        keys = keys.stream().filter(key -> StringUtils.isNotBlank(key) && !EXCLUSION_KEY.equals(key)).collect(Collectors.toList());
        Collections.sort(keys);//排序
        return getSign(apiName, keys, parameterMap);
    }

    /**
     * 中进商场签名规则
     *
     * @param apiName
     * @param keys
     * @param parameterMap
     * @return
     */
    private static String getSign(String apiName, List<String> keys, Map<String, String> parameterMap) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(apiName)) {
            stringBuilder.append(apiName);
        }
        keys.stream().forEach(key -> {
            Object value = parameterMap.get(key);
            stringBuilder.append(key).append(String.valueOf(value));//拼接字符串
        });
        String appsecret = config.getAppsecret();
        stringBuilder.append(appsecret);
        String content = stringBuilder.toString();
        String sign = null;
        try {
            sign = AESUtil.encrypt(content, appsecret);
        } catch (Throwable throwable) {
            LOGGER.error(" encrypting has an error: ", throwable.getMessage());
            throwable.printStackTrace();
        }
        return sign;
    }

    /**
     * 获取商户号
     *
     * @return
     */
    public static String getMemberChannel() {
        return config.getMemberChannel();
    }

    /**
     * 获取交易柜台配置的appkey
     *
     * @return
     */
    public static String getAppKey() {
        return config.getAppkey();
    }

    /**
     * 获取交易柜台配置的Appsecret
     *
     * @return
     */
    public static String getAppsecret() {
        return config.getAppsecret();
    }

    public static String getURL(String apiName) {
        return config.getUrl().concat(apiName);
    }

    /**
     * 获取请求Id
     *
     * @return
     */
    public synchronized static String generateRequestId(long timestamps) {
        String memberChannel = config.getMemberChannel();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(memberChannel).append(MID_SPLIT_CHAR);
        stringBuilder.append(timestamps).append(MID_SPLIT_CHAR);
        stringBuilder.append(getRandomSix());
        return stringBuilder.toString();
    }


    /**
     * 获取设备Id
     *
     * @return
     */
    public static String getDeviceId() {
        String hostName = getHost();
//        return applicationName.concat(hostName);
//        return "gz-server".concat(hostName);
        return "gz";
    }

    /**
     * 获取主机名
     *
     * @return
     */
    private static String getHost() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String hostname = addr == null ? "" : addr.getHostAddress();
        return hostname;
    }

    public static String getRandomSix() {
        return String.valueOf(Math.abs(ATOMIC_COUNT.incrementAndGet() % 99999) + 100000);
    }
}
