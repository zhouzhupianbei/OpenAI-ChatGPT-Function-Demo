package com.lgf.warehouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网络代理
 */

@Data
@Component
@ConfigurationProperties(prefix = "base.proxy")
public class NetProxyProperties {
    /**
     * 代理地址
     */
    private String url;
    /**
     * 代理端口
     */
    private Integer port;
}
