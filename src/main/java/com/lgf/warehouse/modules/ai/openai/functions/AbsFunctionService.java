package com.lgf.warehouse.modules.ai.openai.functions;

import cn.hutool.json.JSONObject;
import com.lgf.warehouse.core.chatgpt.entity.chat.Functions;
import com.lgf.warehouse.core.chatgpt.entity.chat.Parameters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 方法服务抽象类
 */
public abstract class AbsFunctionService {

    public abstract String getFunctionName();

    public abstract Class getCla();

    /**
     * 执行方法
     * @param methodName
     * @param params
     * @return
     */
    public Object execute(String methodName, Map<String,Object> params) throws NoSuchMethodException {
        Method method=this.getMethodByName(methodName);
        try {
            Object[] objs=getArgumentsArray(params,method.getParameters());
            return method.invoke(this ,objs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 根据方法的参数配置返回参数列表
     * @param paramMap
     * @param parameters
     * @return
     */
    private static Object[] getArgumentsArray(Map<String, Object> paramMap, Parameter[] parameters) {
        Object[] argsArray = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object paramValue = paramMap.getOrDefault(parameter.getName(), null);
            argsArray[i] = paramValue;
        }
        return argsArray;
    }

    /**
     * 根据方法名获取方法
     * @param methodName
     * @return
     * @throws NoSuchMethodException
     */
    private Method getMethodByName(String methodName) throws NoSuchMethodException {
        Method[] methods=this.getCla().getMethods();
        for(Method method:methods){
            if(method.getName().equals(methodName)){
                return method;
            }
        }
        return this.getCla().getMethod(methodName);
    }

    /**
     * 获取方法
     * @return
     * @throws NoSuchMethodException
     */
    public List<Functions> getFunctions() throws NoSuchMethodException {
        Class cla = this.getCla();
        Method[] methods=cla.getMethods();
        List<Functions> functions=new ArrayList<>();
        for(Method method: methods) {
            Annotation[] as=method.getAnnotations();
            FunctionAnnotation methodFun = method.getAnnotation(FunctionAnnotation.class);
            if(methodFun==null){
                continue;
            }
            String description = methodFun.describe();
            Parameter[] params = method.getParameters();
            Parameters parameters = this.getParameters(params);
            Functions function = Functions.builder()
                    .name(this.getFunctionName() + "_" + method.getName())
                    .description(description)
                    .parameters(parameters)
                    .build();
            functions.add(function);
        }
        return functions;
    }

    /**
     * 获取参数
     *
     * @param parameters
     * @return
     */
    private Parameters getParameters(Parameter[] parameters) {
        JSONObject params = new JSONObject();
        List<String> requireds = new ArrayList<>();
        for (Parameter parameter : parameters) {
            FunctionAnnotation paramFun = parameter.getAnnotation(FunctionAnnotation.class);

            JSONObject param = new JSONObject();
            String type = this.convertParameterToJsonSchemaType(parameter);
            param.putOpt("type", type);

            if (paramFun != null) {
                if (paramFun.required()) {
                    requireds.add(parameter.getName());
                }
                if (paramFun.enums().length > 0) {
                    param.putOpt("enum", Arrays.asList(paramFun.enums()));
                }
                param.putOpt("description", paramFun.describe());
            }
            params.putOpt(parameter.getName(), param);
        }
        Parameters result = Parameters.builder()
                .type("object")
                .properties(params)
                .required(requireds)
                .build();

        return result;
    }

    /**
     * 将JAVA的参数类型转换为JSON Schema的类型
     * @param parameter
     * @return
     */
    public String convertParameterToJsonSchemaType(Parameter parameter) {
        String parameterTypeName = parameter.getType().getSimpleName().toLowerCase();
        String jsonSchemaType = null;
        switch (parameterTypeName) {
            case "string":
                jsonSchemaType = "string";
                break;
            case "boolean":
                jsonSchemaType = "boolean";
                break;
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
                jsonSchemaType = "number";
                break;
            default:
                if (parameterTypeName.endsWith("[]")) {
                    jsonSchemaType = "array";
                } else {
                    jsonSchemaType = "object";
                }
        }
        return jsonSchemaType;
    }
}
