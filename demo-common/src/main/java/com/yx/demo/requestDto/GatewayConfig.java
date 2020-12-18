package com.yx.demo.requestDto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author yinxing
 * @date 2020/11/2 15:00
 * @desc
 */

@Configuration
@PropertySource(value = {"classpath:gateway-prod.properties"})
@ConfigurationProperties(prefix = "gateway")
@Data
public class GatewayConfig {
    private String url;
    private String exchange;
    private String platform;
    private String memberChannel;
    private String invitationCode;
    private String appkey;
    private String appsecret;
}
