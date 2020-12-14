package com.yx.demo.requestDTO;

import com.yx.demo.utils.RequestUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yinxing
 * @date 2020/11/2 14:53
 * @desc
 */

@Data
public class WebSocketLoginReqParam implements Serializable {

    private String requestId;

    private String memberChannel;

    private String timestamp;

    private String exchange = "xinyu";

    private String type = "SERVER_BIND";

    private String sign;

    public WebSocketLoginReqParam() {
        Long timestamp = System.currentTimeMillis();
        this.timestamp = timestamp.toString();
        this.requestId = RequestUtil.generateRequestId(timestamp);
//        this.memberChannel = RequestUtil.getMemberChannel();
        this.memberChannel = "168";
    }

}

