package com.lgf.warehouse.modules.ai.xf.controller;

import com.lgf.warehouse.core.vo.R;
import com.lgf.warehouse.modules.ai.xf.service.SparkDeskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/xf")
public class XfController {
    @Autowired
    private SparkDeskService sparkDeskService;

    /**
     * 问答
     *
     * @param prompt
     * @return
     */
    @PostMapping
    @Operation(summary = "问答", description = "问答")
    public R start(@RequestParam("prompt") String prompt) {
        String result = this.sparkDeskService.send("user123", prompt);
        return R.ok(result);
    }
}
