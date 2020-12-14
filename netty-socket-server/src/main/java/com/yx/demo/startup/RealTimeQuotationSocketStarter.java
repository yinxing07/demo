package com.yx.demo.startup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.yx.demo.beans.RealTimeQuotationBean;
import com.yx.demo.requestDTO.WebSocketLoginReqParam;
import com.yx.demo.service.impl.BaseService;
import com.yx.demo.utils.NumberUtil;
import com.yx.demo.utils.RedisUtil;
import com.yx.demo.utils.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;

/**
 * @author yinxing
 * @date 2020/11/2 11:06
 * @desc
 */

@Component
public class RealTimeQuotationSocketStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeQuotationSocketStarter.class);

    //    private static final String url = "ws://127.0.0.1:17801/renting-socketserver/ws/asset";
    private static final String url = "wss://k8stest.xinyusoft.com/api/websocket";

    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public WebSocketClient webSocketClient() {
        try {
            WebSocketClient client = new WebSocketClient(new URI(url), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    LOGGER.info("[websocket] 连接成功:{}", serverHandshake);
                    String requestMsg = wrapLoginParam();
                    LOGGER.info("发送webSocket登录请求参数:{}", requestMsg);
                    this.send(requestMsg);
                }

                @Override
                public void onMessage(String message) {
//                    LOGGER.info("[websocket] 收到消息：{}", message);
                    if (StringUtils.isNotEmpty(message)) {
                        JSONObject object = JSON.parseObject(message);
                        if ("REAL_MARKET".equals(object.getString("type"))) {
                            JSONObject quotes = object.getJSONObject("data");
                            Double last = quotes.getDouble("last");
                            Double close = quotes.getDouble("close");
                            Double upDownPoint = Math.floor((last - close) * 100) / 100;
                            String upDownPercent = NumberUtil.getPercent(close, upDownPoint);
                            updateRealTimeQuotationData(quotes, upDownPoint, upDownPercent);
                            pushData(quotes, upDownPoint, upDownPercent);
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LOGGER.info("[websocket] 退出连接：code={},reason={},remote={}", code, reason, remote);
                }

                @Override
                public void onError(Exception e) {
                    LOGGER.info("[websocket] 连接错误：{}", e.getMessage());
                }
            };
            client.connect();
            return client;
        } catch (Exception e) {
            LOGGER.info("[websocket] 连接异常：{}", e.getMessage());
        }
        return null;
    }

    private static String wrapLoginParam() {
        WebSocketLoginReqParam param = new WebSocketLoginReqParam();
        String sign = RequestUtil.getSign("/api/websocket", param);
        param.setSign(sign);
        return JSON.toJSONString(param);
    }

    public void pushData(JSONObject quotes, Double upDownPoint, String upDownPercent) {
        StringBuilder builder = new StringBuilder();
        builder.append(quotes.getString("contract")).append(",")
                .append(quotes.getLong("time")).append(",")
                .append(quotes.getDouble("last")).append(",")
                .append(upDownPoint).append(",")
                .append(upDownPercent);
        //推送实时行情
        BaseService.pushRealTimeQuotation(builder.toString());

        //实时行情放入redis，处理挂单
        RedisUtil.publish("REAL_QUOTATION_PENDING_ORDER_CHANNEL",quotes.getString("contract")+","+quotes.getDouble("last"));
    }

    public void updateRealTimeQuotationData(JSONObject quotes, Double upDownPoint, String upDownPercent) {
        String contract = quotes.getString("contract");
        String key = contract.concat("_real_time_data");
        String realQuotationStr = (String) redisTemplate.boundValueOps(key).get();
        RealTimeQuotationBean model = new RealTimeQuotationBean();
        if (StringUtils.isNotEmpty(realQuotationStr)) {
            model = JSON.parseObject(realQuotationStr, RealTimeQuotationBean.class);
        }
        String currTradingDay = "20201214";
        String contractName = quotes.getString("contName");
        Double open = quotes.getDouble("open");
        Double high = quotes.getDouble("high");
        Double low = quotes.getDouble("low");
        Double close = quotes.getDouble("close");
        Double last = quotes.getDouble("last");
        Long seq = quotes.getLong("seq");
        Long timeStamp = quotes.getLong("time");

        Double highest;
        Double lowest;
        if (model == null) {
            model = new RealTimeQuotationBean();
            highest = last;
            lowest = last;
        } else {
            highest = model.getHighestPrice();
            lowest = model.getLowestPrice();
            if (last > highest) {
                highest = last;
            }
            if (last < lowest || lowest == 0) {
                lowest = last;
            }
        }
        model.setPriceTick(1D);
        model.setContract(contract);
        model.setContName(contractName);
        model.setOpen(open);
        model.setHigh(high);
        model.setLow(low);
        model.setClose(close);
        model.setLast(last);
        model.setLowestPrice(lowest);
        model.setHighestPrice(highest);
        model.setTime(timeStamp);
        model.setSeq(seq);
        model.setUpDownPoint(upDownPoint);
        model.setUpDownPercent(upDownPercent);
        model.setTradingDay(currTradingDay);
        RedisUtil.set(key, JSON.toJSONString(model));
    }
}
