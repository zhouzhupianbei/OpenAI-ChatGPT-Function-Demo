package com.lgf.warehouse.modules.ai.xf.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.System.out;

/**
 * 讯飞星火大模型服务的WEBSocket对接方式
 */
public class SparkDeskWebSocket extends WebSocketListener {

    private String appId;
    private String apiKey;
    private String apiSecret;

    private String hostUrl;
    private Gson json = new Gson();

    private StringBuilder result;
    /**
     * 消息开关，1为开启，2为关闭
     */
    private static int flag;


    public SparkDeskWebSocket(String hostUrl, String appId, String apikey, String apiSecret) {
        super();
        this.appId = appId;
        this.apiKey = apikey;
        this.apiSecret = apiSecret;
        this.hostUrl = hostUrl;
    }

    private WebSocket createWebsocket(){
        try {
            //构建鉴权httpurl
            String authUrl = getAuthorizationUrl(this.hostUrl, this.apiKey, this.apiSecret);
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            String url = authUrl.replace("https://", "wss://").replace("http://", "ws://");
            Request request = new Request.Builder().url(url).build();
            WebSocket webSocket = okHttpClient.newWebSocket(request, this);
            return webSocket;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //鉴权url
    public static String getAuthorizationUrl(String hostUrl, String apikey, String apisecret) throws Exception {
        //获取host
        URL url = new URL(hostUrl);
        //获取鉴权时间 date
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        out.println("format:\n" + format);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        //获取signature_origin字段
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").
                append("date: ").append(date).append("\n").
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        out.println("signature_origin:\n" + builder);
        //获得signatue
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec sp = new SecretKeySpec(apisecret.getBytes(charset), "hmacsha256");
        mac.init(sp);
        byte[] basebefore = mac.doFinal(builder.toString().getBytes(charset));
        String signature = Base64.getEncoder().encodeToString(basebefore);
        //获得 authorization_origin
        String authorization_origin = String.format("api_key=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", apikey, "hmac-sha256", "host date request-line", signature);
        //获得authorization
        String authorization = Base64.getEncoder().encodeToString(authorization_origin.getBytes(charset));
        //获取httpurl
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
                addQueryParameter("authorization", authorization).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        return httpUrl.toString();
    }

    /**
     * 发送问题
     *
     * @param question
     * @return
     */
    public String send(String userId, String question) {
        WebSocket webSocket = createWebsocket();
        if(webSocket == null){
            return "websocket创建失败";
        }
        //TODO 回复机制有问题，在回复完成前不建议发送第二个问题，缺少一个会话队列功能，将问题放入队列，直到回复结束后继续下一段会话
        this.result = new StringBuilder();
        String frameStr = packFrame(userId, question);
        flag = 1;
        webSocket.send(frameStr);
        //发送消息，发送后监听回复信息，直至flag==2后返回结果数据
        while (flag == 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    private String packFrame(String userId, String question) {
        JsonObject frame = new JsonObject();
        JsonObject header = new JsonObject();
        JsonObject chat = new JsonObject();
        JsonObject parameter = new JsonObject();
        JsonObject payload = new JsonObject();
        JsonObject message = new JsonObject();
        JsonObject text = new JsonObject();
        JsonArray ja = new JsonArray();

        //填充header
        header.addProperty("app_id", this.appId);
        header.addProperty("uid", userId);
        //填充parameter
        chat.addProperty("domain", "general");
        chat.addProperty("random_threshold", 0);
        chat.addProperty("max_tokens", 1024);
        chat.addProperty("auditing", "default");
        parameter.add("chat", chat);
        //填充payload
        text.addProperty("role", "user");
        text.addProperty("content", question);
        ja.add(text);
//            message.addProperty("text",ja.getAsString());
        message.add("text", ja);
        payload.add("message", message);
        frame.add("header", header);
        frame.add("parameter", parameter);
        frame.add("payload", payload);
        String frameStr = frame.toString();
        out.println(frameStr);
        return frameStr;
    }

    //重写onopen
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);

    }

    //重写onmessage

    @Override
    public void onMessage(WebSocket webSocket, String text) {
//        super.onMessage(webSocket, text);
        out.println("text:\n" + text);
        ResponseData responseData = json.fromJson(text, ResponseData.class);
//        System.out.println("code:\n" + responseData.getHeader().get("code"));
        if (0 == responseData.getHeader().get("code").getAsInt()) {

            if (2 != responseData.getHeader().get("status").getAsInt()) {
                Payload pl = json.fromJson(responseData.getPayload(), Payload.class);
                JsonArray temp = (JsonArray) pl.getChoices().get("text");
                JsonObject jo = (JsonObject) temp.get(0);
                result.append(jo.get("content").getAsString());
                flag=1;
            } else {
                Payload pl1 = json.fromJson(responseData.getPayload(), Payload.class);
                JsonObject jsonObject = (JsonObject) pl1.getUsage().get("text");
                int prompt_tokens = jsonObject.get("prompt_tokens").getAsInt();
                JsonArray temp1 = (JsonArray) pl1.getChoices().get("text");
                JsonObject jo = (JsonObject) temp1.get(0);
                result.append(jo.get("content").getAsString());
                flag = 2;
                webSocket.close(1000, "close");
            }
        } else {
            out.println("返回结果错误：\n" + responseData.getHeader().get("code") + responseData.getHeader().get("message"));
            result.append("返回结果错误：\n" + responseData.getHeader().get("code") + responseData.getHeader().get("message"));
            flag = 2;
            webSocket.close(1000, "close");
        }
    }

    //重写onFailure

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        out.println(response);
    }


    class ResponseData {
        private JsonObject header;
        private JsonObject payload;

        public JsonObject getHeader() {
            return header;
        }

        public JsonObject getPayload() {
            return payload;
        }
    }

    class Header {
        private int code;
        private String message;
        private String sid;
        private String status;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String getSid() {
            return sid;
        }

        public String getStatus() {
            return status;
        }
    }

    class Payload {
        private JsonObject choices;
        private JsonObject usage;

        public JsonObject getChoices() {
            return choices;
        }

        public JsonObject getUsage() {
            return usage;
        }
    }

    class Choices {
        private int status;
        private int seq;
        private JsonArray text;

        public int getStatus() {
            return status;
        }

        public int getSeq() {
            return seq;
        }

        public JsonArray getText() {
            return text;
        }
    }

}
