package com.lgf.warehouse.core.vo;

import com.lgf.warehouse.core.Constants;
import lombok.Data;

/**
 * AI工具返回的结果
 */
@Data
public class AIResult {
    private String instanceCode;
    private String businessCode;
    private Object result;
    /**
     * 返回值类型，Constants.AI.Result.Type
     */
    private String type;

    private Integer code;
    private String msg;

    public AIResult() {
        this.code = Constants.Result.Code.SUCCESS;
    }

    public AIResult(Integer code) {
        this.code = code;
    }


    public static AIResult fail(String message) {
        AIResult result = new AIResult();
        result.setCode(Constants.Result.Code.ERROR);
        result.setType(Constants.Result.Type.TEXT);
        result.setResult(message);
        return result;
    }

    /**
     * 判断是否成功
     *
     * @return
     */
    public boolean isSuccess() {
        if (this.code == null) {
            return false;
        }
        return Constants.Result.Code.SUCCESS.equals(this.code);
    }
}
