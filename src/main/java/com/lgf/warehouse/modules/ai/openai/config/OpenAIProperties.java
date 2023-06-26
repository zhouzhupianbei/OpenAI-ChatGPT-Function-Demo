package com.lgf.warehouse.modules.ai.openai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "openai")
//@EnableConfigurationProperties
public class OpenAIProperties {
    private String proxyUrl;
    private Integer proxyPort;
    private String model;
    private List<String> tokens;
    private Map<String,String> prompt;

}
