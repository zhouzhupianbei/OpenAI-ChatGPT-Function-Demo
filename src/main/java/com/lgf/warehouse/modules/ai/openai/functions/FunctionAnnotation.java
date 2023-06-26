package com.lgf.warehouse.modules.ai.openai.functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * GPT函数的注释
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.PARAMETER})
public @interface FunctionAnnotation {

    /**
     * 描述
     * @return
     */
    public String describe() default "";

    /**
     * 枚举数据
     * @return
     */
    public String[] enums() default {};

    /**
     * 是否必须
     * @return
     */
    public boolean required() default false;
}
