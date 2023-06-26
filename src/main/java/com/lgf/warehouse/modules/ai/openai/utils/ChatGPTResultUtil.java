package com.lgf.warehouse.modules.ai.openai.utils;

import com.lgf.warehouse.core.vo.AIResult;

/**
 * ChatGPT的返回值处理
 */
public class ChatGPTResultUtil {

    /**
     * 处理返回值，如果返回值中包含有JSON则只返回JSON中内容，如果没有，则包装JSON格式内容返回
     *
     * @param result
     * @return
     */
    public static AIResult process(String result) {
        AIResult aiResult = new AIResult();
        aiResult.setResult(result);
        return aiResult;
    }


}
