package com.lgf.warehouse.core.chatgpt.entity.completions;

import com.lgf.warehouse.core.chatgpt.entity.common.Choice;
import com.lgf.warehouse.core.chatgpt.entity.common.OpenAiResponse;
import com.lgf.warehouse.core.chatgpt.entity.common.Usage;
import lombok.Data;

import java.io.Serializable;

/**
 * 描述： 答案类
 *
 * @author https:www.unfbx.com
 *  2023-02-11
 */
@Data
public class CompletionResponse extends OpenAiResponse implements Serializable {
    private String id;
    private String object;
    private long created;
    private String model;
    private Choice[] choices;
    private Usage usage;
}
