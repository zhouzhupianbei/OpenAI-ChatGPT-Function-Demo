package com.lgf.warehouse.modules.ai.openai.controller;

import com.lgf.warehouse.core.vo.AIResult;
import com.lgf.warehouse.core.vo.R;
import com.lgf.warehouse.modules.ai.openai.service.ChatGPTService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/openai")
public class OpenAIController {
    @Autowired
    private ChatGPTService chatGPTService;

    /**
     * 问答
     *
     * @param prompt
     * @return
     */
    @PostMapping
    @Operation(summary = "问答", description = "问答")
    public R start(@RequestParam("prompt") String prompt) {
        try {
            AIResult aiResult = this.chatGPTService.chatFun(prompt);
            return R.ok(aiResult);
        } catch (InterruptedException e) {
            return R.error(e.getMessage());
        }
    }
}
