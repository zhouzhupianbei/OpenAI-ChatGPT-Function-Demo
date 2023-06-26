package com.lgf.warehouse.modules.ai.openai.functions;

import com.lgf.warehouse.core.chatgpt.entity.chat.ChatChoice;
import com.lgf.warehouse.core.chatgpt.entity.chat.FunctionCall;
import com.lgf.warehouse.core.chatgpt.entity.chat.Functions;
import com.lgf.warehouse.core.chatgpt.entity.chat.Message;
import com.lgf.warehouse.modules.ai.openai.functions.lucene.service.LuceneSearchService;
import com.lgf.warehouse.modules.ai.openai.functions.weather.service.WeatherApiService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 方法生产工厂
 */
@Slf4j
@Service
public class FunctionFactory {
    @Autowired
    private WeatherApiService weatherApiService;
    @Autowired
    private LuceneSearchService luceneSearchService;

    //    函数定义集合
    private Map<String, AbsFunctionService> functionBeanMap;

    private List<Functions> functions;

    /**
     * 注册服务到FunctionFactory
     */
    @PostConstruct()
    public void register() throws NoSuchMethodException {
        this.functionBeanMap = new HashMap<>();
        this.functions = new ArrayList<>();
        //注册服务
        this.registerFunction(this.weatherApiService);
        this.registerFunction(this.luceneSearchService);
        log.info(String.format("注册了%d个服务", this.functionBeanMap.size()));
    }

    private void registerFunction(AbsFunctionService functionService) throws NoSuchMethodException {
        this.functionBeanMap.put(functionService.getFunctionName(), functionService);
        this.functions.addAll(functionService.getFunctions());
    }

    // 定义公共的返回值
    public FunctionResult execute(String functionName, Map<String, Object> params) throws NoSuchMethodException {
        String[] sp = functionName.split("_");
        if (sp.length != 2) {
            throw new RuntimeException("方法名称不正确");
        }
        String beanName = sp[0];
        AbsFunctionService functionService = this.functionBeanMap.get(beanName);
        if (functionService == null) {
            throw new RuntimeException("没有找到对应的Bean");
        }
        //通过反射执行functionService中的functionName名称的方法
        Object result = functionService.execute(sp[1], params);
        if (result == null) {
            return FunctionResult.failure();
        } else {
            return FunctionResult.ok(result);
        }
    }

    /**
     * 包装Messages，在第二次请求的时候，将上下文信息都传入到OpenAI中，方便OpenAI理解，然后回复我
     * @param message
     * @param chatChoice
     * @param functionResult
     * @return
     */
    public List<Message> getMessages(Message message, ChatChoice chatChoice,FunctionResult functionResult) {
        FunctionCall fc = chatChoice.getMessage().getFunctionCall();
        FunctionCall functionCall = FunctionCall.builder()
                .arguments(fc.getArguments())
                .name(fc.getName())
                .build();
        //辅助消息，说明方法的参数信息
        Message message2 = Message.builder().role(Message.Role.ASSISTANT).content("方法参数").functionCall(functionCall).build();
        String content = functionResult.toString();
        //方法说明的消息，将方法的返回值告诉OpenAI
        Message message3 = Message.builder().role(Message.Role.FUNCTION).name(fc.getName()).content(content).build();
        //把问题串起来给OpenAI
        List<Message> messageList = Arrays.asList(message, message2, message3);
        return messageList;
    }


    /**
     * 获取方法集合
     *
     * @return
     */
    public List<Functions> getFunctions() {
        return this.functions;
    }

}
