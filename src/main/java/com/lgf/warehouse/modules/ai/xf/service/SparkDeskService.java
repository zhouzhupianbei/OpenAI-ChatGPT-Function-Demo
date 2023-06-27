package com.lgf.warehouse.modules.ai.xf.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.lang.System.out;

/**
 * 讯飞星火大模型服务
 */
@Slf4j
@Service
public class SparkDeskService extends WebSocketListener {
    @Value("${xf.host-url}")
    private String hostUrl;
    @Value("${xf.app-id}")
    private String appId = "";//从开放平台控制台中获取
    @Value("${xf.api-key}")
    private String apiKey = "";//从开放平台控制台中获取
    @Value("${xf.api-secret}")
    private String apiSecret = "";//从开放平台控制台中获取

    private SparkDeskWebSocket webSocket;

    @PostConstruct
    public void init() {
        this.webSocket = new SparkDeskWebSocket(this.hostUrl, this.appId, this.apiKey, this.apiSecret);
    }

    /**
     * 发送消息
     *
     * @param userId
     * @param message
     * @return
     */
    public String send(String userId, String message) {
        String result = this.webSocket.send(userId, message);
        log.info(result);
        return result;
    }
}
