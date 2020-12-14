package com.yx.demo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.yx.demo.response.JSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestTemplateUtil {
    protected static Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

    @Value("${wp.api.url}")
    protected static final String REQUEST_BASE_URL = "https://www.fastcoinex.com/stfront";

    /**
     * 输入参数值参见枚举
     *
     * @param method 接口名
     * @return
     */
    public static String getUrl(String method) {
        String url = REQUEST_BASE_URL.concat(method);
        return url;
    }

    public static String getUrl(String host, String method) {
        String url = host.concat(method);
        return url;
    }

    /**
     * @param url
     * @param params
     * @Description: 发送form格式数据
     * @return: String
     */
    public static JSONResult postForm(String url, JSONObject params) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        // 设置header信息
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        requestHeaders.set("X-Requested-With", "XMLHttpRequest");
        if (params.get("sessionId") != null) {
            requestHeaders.set("Cookie", "PHPSESSID=" + params.get("sessionId").toString() + "; ");
        }
        HttpEntity<?> requestEntity = new HttpEntity<MultiValueMap>(createMultiValueMap(params), requestHeaders);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        JSONResult result = restTemplate.postForObject(url, requestEntity, JSONResult.class, params);
        return result;
    }

    /**
     * @param url
     * @param params
     * @param clz
     * @Description: 发送form格式数据
     * @return: String
     */
    public static <T> T postForm(String url, JSONObject params, Class<T> clz) {
        logger.info("请求接口地址：" + url);
        logger.info("请求参数：" + JSON.toJSONString(params));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        // 设置header信息
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        requestHeaders.set("X-Requested-With", "XMLHttpRequest");
        if (params.get("sessionId") != null) {
            requestHeaders.set("Cookie", "PHPSESSID=" + params.get("sessionId").toString() + "; ");
        }
        HttpEntity<?> requestEntity = new HttpEntity<MultiValueMap>(createMultiValueMap(params), requestHeaders);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        T result = restTemplate.postForObject(url, requestEntity, clz, params);
        logger.info("返回结果：" + JSON.toJSONString(result));
        return result;
    }

    /**
     * @param url
     * @param request
     * @param typeReference
     * @Description: 发送form格式数据
     * @return: String
     */
    public static <T> T postForm(String url, Object request, TypeReference<T> typeReference) {
        logger.info("请求接口地址：" + url);
        logger.info("请求参数：" + JSON.toJSONString(request));
        JSONObject params = null;
        if (request != null) {
            params = (JSONObject) JSONObject.toJSON(request);
        }
        if (params == null) {
            return null;
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> requestEntity = getHttpEntity(restTemplate,params, MediaType.APPLICATION_FORM_URLENCODED);
        String resultJson = restTemplate.postForObject(url, requestEntity, String.class, params);
        T result = JSON.parseObject(resultJson, typeReference);
        logger.info("返回结果：{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * @param url
     * @param request
     * @param responseBodyType
     * @Description: 发送form格式数据
     * @return: String
     */
    public static <T> T postForm(String url, Object request, ParameterizedTypeReference<T> responseBodyType) {
        return RestTemplateUtil.postForm(url, request, HttpMethod.POST, responseBodyType);
    }

    public static <T> T postForm(String url, Object request, HttpMethod method, ParameterizedTypeReference<T> responseBodyType) {
        logger.info("请求接口地址：" + url);
        logger.info("请求参数：" + JSON.toJSONString(request));
        JSONObject params = null;
        if (request != null) {
            params = (JSONObject) JSONObject.toJSON(request);
        }
        if (params == null) {
            return null;
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> requestEntity = getHttpEntity(restTemplate, params, MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<T> resultEntity = restTemplate.exchange(url, method, requestEntity, responseBodyType);
        logger.info("返回结果：" + JSON.toJSONString(resultEntity));
        return resultEntity.getBody();
    }

    /**
     * @param url
     * @param request
     * @param responseBodyType
     * @param <T>
     * @return
     */
    public static <T> T postJSON(String url, Object request, ParameterizedTypeReference<T> responseBodyType) {
        ResponseEntity<T> resultEntity = postJSONResponseEntity(url, request, responseBodyType);
        return resultEntity.getBody();
    }

    public static <T> ResponseEntity<T> postJSONResponseEntity(String url, Object request, ParameterizedTypeReference<T> responseBodyType) {
        logger.info("请求接口地址：" + url);
        logger.info("请求参数：" + JSON.toJSONString(request));
        JSONObject params = null;
        if (request != null) {
            params = (JSONObject) JSONObject.toJSON(request);
        }
        if (params == null) {
            return null;
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> requestEntity = getHttpEntity(restTemplate, params, MediaType.APPLICATION_JSON_UTF8);
        ResponseEntity<T> resultEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseBodyType);
        HttpHeaders httpHeaders = resultEntity.getHeaders();
        logger.info("返回结果：" + JSON.toJSONString(resultEntity));
        logger.info("返回结果 Headers ：" + JSON.toJSONString(httpHeaders));
        return resultEntity;
    }

    /**
     * @param url
     * @param request
     * @param mediaType
     * @param clz
     * @return
     * @Title: post
     * @Description: 发送json或者form格式数据
     * @return: String
     */
    public static <T> T post(String url, Object request, MediaType mediaType, Class<T> clz) {
        JSONObject params = null;
        if (request != null) {
            params = (JSONObject) JSONObject.toJSON(request);
        }
        if (params == null) {
            return null;
        }
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> httpMessageConverterList = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = httpMessageConverterList.iterator();
        if (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            //原有的String是ISO-8859-1编码 去掉
            if (converter instanceof StringHttpMessageConverter) {
                iterator.remove();
            }
            //由于系统中默认有jackson 在转换json时自动会启用  但是我们不想使用它 可以直接移除或者将fastjson放在首位
            /*if(converter instanceof GsonHttpMessageConverter || converter instanceof MappingJackson2HttpMessageConverter){
                iterator.remove();
            }*/
        }
        httpMessageConverterList.add(new StringHttpMessageConverter(Charset.forName("utf-8")));
        httpMessageConverterList.add(new FormHttpMessageConverter());
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.DisableCircularReferenceDetect);

        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        httpMessageConverterList.add(0, fastJsonHttpMessageConverter);
        // 设置header信息
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(mediaType);
        requestHeaders.set("X-Requested-With", "XMLHttpRequest");
        HttpEntity<?> requestEntity = (
                mediaType == MediaType.APPLICATION_JSON
                        || mediaType == MediaType.APPLICATION_JSON_UTF8)
                ? new HttpEntity<>(params, requestHeaders)
                : (mediaType == MediaType.APPLICATION_FORM_URLENCODED
                ? new HttpEntity<MultiValueMap>(createMultiValueMap(params), requestHeaders)
                : new HttpEntity<>(null, requestHeaders));

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        T result = (mediaType == MediaType.APPLICATION_JSON || mediaType == MediaType.APPLICATION_JSON_UTF8)
                ? restTemplate.postForObject(url, requestEntity, clz)
                : restTemplate.postForObject(mediaType == MediaType.APPLICATION_FORM_URLENCODED ? url : expandURL(url, params.keySet()), requestEntity, clz, params);

        return result;
    }

    protected static MultiValueMap<String, String> createMultiValueMap(JSONObject params) {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (String key : params.keySet()) {
            if (params.get(key) instanceof List) {
                for (Iterator<String> it = ((List<String>) params.get(key)).iterator(); it.hasNext(); ) {
                    String value = it.next();
                    map.add(key, value);
                }
            } else {
                map.add(key, params.getString(key));
            }
        }
        return map;
    }

    /**
     * @param url
     * @param keys
     * @return
     * @Title: expandURL
     * @Description:
     * @return: String
     */
    private static String expandURL(String url, Set<?> keys) {
        final Pattern QUERY_PARAM_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");
        Matcher mc = QUERY_PARAM_PATTERN.matcher(url);
        StringBuilder sb = new StringBuilder(url);
        if (mc.find()) {
            sb.append("&");
        } else {
            sb.append("?");
        }
        for (Object key : keys) {
            sb.append(key).append("=").append("{").append(key).append("}").append("&");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static HttpEntity<?> getHttpEntity(RestTemplate restTemplate, JSONObject params, MediaType mediaType) {
        // 设置header信息
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(mediaType);
        requestHeaders.set("X-Requested-With", "XMLHttpRequest");
        return getHttpEntity(restTemplate, params, mediaType, requestHeaders);
    }

    private static HttpEntity<?> getHttpEntity(RestTemplate restTemplate, JSONObject params, MediaType mediaType, HttpHeaders requestHeaders) {
        List<HttpMessageConverter<?>> httpMessageConverterList = restTemplate.getMessageConverters();
        httpMessageConverterList.add(new StringHttpMessageConverter(Charset.forName("utf-8")));
        httpMessageConverterList.add(new FormHttpMessageConverter());
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.DisableCircularReferenceDetect);

        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        httpMessageConverterList.add(0, fastJsonHttpMessageConverter);
        // 设置header信息
        if (requestHeaders == null) {
            requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(mediaType);
            requestHeaders.set("X-Requested-With", "XMLHttpRequest");
        }
        HttpEntity<?> requestEntity = (
                mediaType == MediaType.APPLICATION_JSON
                        || mediaType == MediaType.APPLICATION_JSON_UTF8)
                ? new HttpEntity<>(params, requestHeaders)
                : (mediaType == MediaType.APPLICATION_FORM_URLENCODED
                ? new HttpEntity<MultiValueMap>(createMultiValueMap(params), requestHeaders)
                : new HttpEntity<>(null, requestHeaders));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        return requestEntity;
    }


    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，那么取第一个ip为客户端ip
        if (ip != null && ip.indexOf(",") != -1) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
