package com.lgf.warehouse.modules.ai.openai.config;

import com.lgf.warehouse.config.NetProxyProperties;
import com.lgf.warehouse.core.chatgpt.OpenAiClient;
import com.lgf.warehouse.core.chatgpt.interceptor.OpenAILogger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Configuration
public class OpenAIConfig {
    @Autowired
    private NetProxyProperties netProxyProperties;

    @Autowired
    private OpenAIProperties openAIProperties;




//    private String modu

    /**
     * OpenAI服务
     * @return
     */
    @Bean
    public OpenAiClient getOpenAiClient(){
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.netProxyProperties.getUrl(), this.netProxyProperties.getPort()));
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());

        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .proxy(proxy)//自定义代理
                .addInterceptor(httpLoggingInterceptor)//自定义日志
                .connectTimeout(120, TimeUnit.SECONDS)//自定义超时时间
                .writeTimeout(120, TimeUnit.SECONDS)//自定义超时时间
                .readTimeout(120, TimeUnit.SECONDS)//自定义超时时间
                .build();
        OpenAiClient client=OpenAiClient.builder()
                .apiKey(openAIProperties.getTokens())
                .keyStrategy(new FirstKeyStrategy())
//                .apiHost(this.apiHost)
                .okHttpClient(okHttpClient)
                .build();

        return client;
    }
}
