package com.lgf.warehouse.modules.ai.openai.functions;

import cn.hutool.json.JSONUtil;
import com.lgf.warehouse.core.Constants;
import lombok.Data;

import java.util.Objects;

/**
 * 函数执行结果
 */
@Data
public class FunctionResult {

    private Integer code;

    private Object data;

    public FunctionResult() {
        this.code = Constants.Result.Code.SUCCESS;
    }

    public FunctionResult(Object data) {
        this.code = Constants.Result.Code.SUCCESS;
        this.data = data;
    }

    /**
     * 失败
     *
     * @return
     */
    public static FunctionResult failure() {
        FunctionResult result = new FunctionResult();
        result.setCode(Constants.Result.Code.ERROR);
        return result;
    }

    /**
     * 成功
     *
     * @param data
     * @return
     */
    public static FunctionResult ok(Object data) {
        return new FunctionResult(data);
    }

    public boolean isSuccess() {
        return Objects.equals(this.code, Constants.Result.Code.SUCCESS);
    }

    public String toString() {
        if(this.getData()==null){
            return "";
        }else {
            return JSONUtil.toJsonStr(this.getData());
        }
    }
}
