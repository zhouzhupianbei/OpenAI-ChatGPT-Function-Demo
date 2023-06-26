package com.lgf.warehouse.modules.ai.openai.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lgf.warehouse.core.chatgpt.OpenAiClient;
import com.lgf.warehouse.core.chatgpt.entity.chat.*;
import com.lgf.warehouse.core.vo.AIResult;
import com.lgf.warehouse.modules.ai.openai.functions.FunctionFactory;
import com.lgf.warehouse.modules.ai.openai.functions.FunctionResult;
import com.lgf.warehouse.modules.ai.openai.utils.ChatGPTResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.HttpException;

import java.util.*;

/**
 * ChatGPTService
 */
@Service
@Slf4j
public class ChatGPTService {

    @Autowired
    private OpenAiClient openAiClient;
    @Autowired
    private FunctionFactory functionFactory;

    public AIResult chatFun(String prompt) throws InterruptedException {

        StringBuilder result = new StringBuilder();

        List<Functions> functions = functionFactory.getFunctions();

        List<Message> messages = new ArrayList<>();
        Message message = Message.builder().role(Message.Role.USER).content(prompt).build();
        messages.add(message);

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(messages)
                .functions(functions)
                .functionCall("auto")
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        try {
            ChatCompletionResponse chatCompletionResponse = this.openAiClient.chatCompletion(chatCompletion);

            //应该遍历处理，暂时没有意义
            ChatChoice chatChoice = chatCompletionResponse.getChoices().get(0);
            if (chatChoice.getMessage().getFunctionCall() == null) {
                result.append(chatChoice.getMessage().getContent());
            } else {
                //存在满足条件的函数，调用函数返回值
                String functionName = chatChoice.getMessage().getFunctionCall().getName();
                String functionArgs = chatChoice.getMessage().getFunctionCall().getArguments();
                log.info("构造的方法名称：{}", functionName);
                log.info("构造的方法参数：{}", functionArgs);
                Map<String, Object> params = new HashMap<>();
                JSONObject argsJSON = JSONUtil.parseObj(functionArgs);
                for (String key : argsJSON.keySet()) {
                    params.put(key, argsJSON.get(key));
                }
                try {
                    FunctionResult functionResult = this.functionFactory.execute(functionName, params);
                    if (functionResult.isSuccess()) {
                        // 拼接消息，再次发送消息
                        List<Message> messageList = this.functionFactory.getMessages(message,chatChoice,functionResult);
                        ChatCompletion chatCompletionV2 = ChatCompletion
                                .builder()
                                .messages(messageList)
                                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                                .build();
                        ChatCompletionResponse chatCompletionResponseV2 = openAiClient.chatCompletion(chatCompletionV2);
                        ChatChoice chatChoiceV2 = chatCompletionResponseV2.getChoices().get(0);
                        result.append(chatChoiceV2.getMessage().getContent());
                    }
                } catch (Exception e) {
                    log.error("执行方法出错", e);
                    return this.chat(prompt,null);
                }
            }


            AIResult resultObj = ChatGPTResultUtil.process(result.toString());
            return resultObj;
        } catch (HttpException e1) {
            log.error("ChatGPTService.chat error", e1);
            try {
                byte[] bytes = e1.response().errorBody().bytes();
                String str = new String(bytes);
                System.out.println(str);
                log.error("Error Response：", e1.response().errorBody());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this.chat(prompt, null);
        }
    }


    public AIResult chat(String prompt, List<Message> messages) throws InterruptedException {

        StringBuilder result = new StringBuilder();
        if (messages == null) {
            messages = new ArrayList<>();
        }

        Message message = Message.builder().role(Message.Role.USER).content(prompt).build();
        messages.add(message);

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(messages)
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        try {
            ChatCompletionResponse chatCompletionResponse = this.openAiClient.chatCompletion(chatCompletion);

            //应该遍历处理，暂时没有意义
            ChatChoice chatChoice = chatCompletionResponse.getChoices().get(0);
            result.append(chatChoice.getMessage().getContent());
            AIResult resultObj = ChatGPTResultUtil.process(result.toString());
            return resultObj;
        } catch (HttpException e1) {
            log.error("ChatGPTService.chat error", e1);
            try {
                byte[] bytes = e1.response().errorBody().bytes();
                String str = new String(bytes);
                System.out.println(str);
                log.error("Error Response：", e1.response().errorBody());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return AIResult.fail("ChatGPTService.chat error" + e1.getMessage());
        }
    }


}
