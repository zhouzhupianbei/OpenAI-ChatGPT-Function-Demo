# 介绍

这是一个OpenAI Function 接入的Demo，通过Lucene+OpenAI实现个人知识库，但是目前只有服务端，没有页面之类的东西，并且实现的非常简单，所以只能当做一个Demo使用。

感谢[https://github.com/Grt1228/chatgpt-java](https://github.com/Grt1228/chatgpt-java)项目，不过由于我Maven配置问题，拉不到1.0.14版本的包，我直接把这个项目源代码拷贝到了我的项目里。

这个项目更新的非常及时，提供了OpenAI最新API的接入，很好用的SDK。

# 启动服务
## 准备工作
1、 OpenAI Token，到OpenAI官网申请，这里不再赘述

2、 我在服务里写了一个查询天气的Demo，如果有兴趣的话，可以去申请一个天气查询KEY，申请地址：[https://www.weatherapi.com/](https://www.weatherapi.com/) 免费的，够用一阵了。

3、 修改配置信息

```yaml
server:
  port: 9000 #端口

spring:
  servlet:
    multipart: # 文件上传限制
      max-file-size: 10MB 
      max-request-size: 10MB

openai:
  model: "gpt-3.5-turbo-0613" # 模型，暂时没什么用
  tokens: "" # OpenAI Token，多个使用逗号隔开，注意是英文逗号，不过多个token我没试过

base:
  proxy: # 代理信息，如果需要的话，国内肯定得用，不会了百度或者私信我
    url: 127.0.0.1
    port: 7890
  file:
    save-path: /out/files # 文件保存路径
functions:
  weather: # 天气的配置
    api:
      url: http://api.weatherapi.com/v1/current.json
      key:  # 天气查询的key
  lucene:
    index:
      path: # 索引保存路径 
    timeout: 10s
    fragment-size: 200
    default-page-size: 10
```

## 启动

正常的SpringBoot项目的启动方式。

# 修改

## 删除一个Function

比如不想用天气的函数，可以到com.lgf.warehouse.modules.ai.openai.functions.FunctionFactory中删除。

```java

    @Autowired
    private WeatherApiService weatherApiService;
    @Autowired
    private LuceneSearchService luceneSearchService;
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
```
默认是注册了两个服务：天气服务（WeatherApiService）和Lucene搜索服务（LuceneSearchService），如果不想用天气服务，可以删除这个注册。

系统使用的是SpringBoot的自动注入，所以只要删除了这个注册，就不会再使用这个服务了。

## 添加一个Function

比如我想添加一个查询百度的功能，那么需要做如下几步：

1、 添加一个Service，继承AbsFunctionService，比如我这里添加一个BaiduSearchService

```java
package com.lgf.warehouse.modules.ai.openai.functions.weather.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lgf.warehouse.modules.ai.openai.functions.AbsFunctionService;
import com.lgf.warehouse.modules.ai.openai.functions.FunctionAnnotation;
import com.lgf.warehouse.modules.ai.openai.functions.weather.vo.Weather;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherApiService  extends AbsFunctionService {

    @Value("${functions.weather.api.url}")
    private String api;
    @Value("${functions.weather.api.key}")
    private String key;


    @FunctionAnnotation(describe = "获取天气预报")
    public Weather getWeatherByCity(@FunctionAnnotation(describe = "城市名称，如果是中文需要转换为拼音，比如洛阳，需要转换为LuoYang",required = true) String city,
                              @FunctionAnnotation(describe = "要查询的是哪一天的天气，今天是1，明天是2，后天是3，最多查询3天的天气，如果超出3天，则返回1",required = true,enums = {"1","2","3"}) Integer days) {
        //调用天气接口
        Map<String,Object> params=new HashMap<>();
        params.put("key",this.key);
        params.put("q",city);
        params.put("days",days);
        String result= HttpUtil.get(this.api,params);
        if(JSONUtil.isTypeJSONObject(result)){
            JSONObject response=JSONUtil.parseObj(result);
//            {"location":{"name":"Luoyang","region":"Henan","country":"China","lat":34.68,"lon":112.45,"tz_id":"Asia/Shanghai","localtime_epoch":1687575565,"localtime":"2023-06-24 10:59"},"current":{"last_updated_epoch":1687574700,"last_updated":"2023-06-24 10:45","temp_c":30.9,"temp_f":87.6,"is_day":1,"condition":{"text":"Sunny","icon":"//cdn.weatherapi.com/weather/64x64/day/113.png","code":1000},"wind_mph":4.0,"wind_kph":6.5,"wind_degree":150,"wind_dir":"SSE","pressure_mb":1009.0,"pressure_in":29.8,"precip_mm":0.0,"precip_in":0.0,"humidity":35,"cloud":9,"feelslike_c":30.6,"feelslike_f":87.1,"vis_km":10.0,"vis_miles":6.0,"uv":8.0,"gust_mph":4.7,"gust_kph":7.6}}

            if(response.get("current")!=null){
                JSONObject location=response.getJSONObject("location");
                JSONObject current=response.getJSONObject("current");
                JSONObject condition=current.getJSONObject("condition");
                // 解析天气接口返回的数据
                Weather weather= new Weather();
                weather.setCity(location.getStr("name"));
                weather.setRegion(location.getStr("region"));
                weather.setCountry(location.getStr("country"));

                weather.setDays(days);

                weather.setCondition(condition.getStr("text"));
                weather.setConditionIcon(condition.getStr("icon"));

                weather.setTemp(current.getDouble("temp_c"));
                return weather;
            }
        }
        return null;
    }

    @Override
    public String getFunctionName() {
        return "WeatherApiService";
    }

    @Override
    public Class getCla() {
        return this.getClass();
    }
}

```

当然，我懒得再写一个百度的服务了，复制天气服务来讲解。

在AbsFunctionService中定义了getFunctionName()和getCal()的抽象方法，需要在子类中实现，返回函数的名称和实现类的Class对象。

在这个Service中编写函数的方法，方法需要使用@FunctionAnnotation注解，这个注解有以下几个属性：

- describe：函数的描述，比如“获取天气预报”
- required：是否必须，如果是必须的，那么在调用的时候，如果没有传递这个参数，那么会抛出异常
- enums：如果是枚举类型，那么可以使用这个属性，比如天气预报的天数，只能是1、2、3，那么可以使用这个属性，如果传递的值不是这三个值，那么会抛出异常

```java
@FunctionAnnotation(describe = "获取天气预报")
    public Weather getWeatherByCity(@FunctionAnnotation(describe = "城市名称，如果是中文需要转换为拼音，比如洛阳，需要转换为LuoYang",required = true) String city,
                              @FunctionAnnotation(describe = "要查询的是哪一天的天气，今天是1，明天是2，后天是3，最多查询3天的天气，如果超出3天，则返回1",required = true,enums = {"1","2","3"}) Integer days) {
```
注意：方法必须使用@FunctionAnnotation注解，否则不会被识别为函数。参数没有强制要求，但是如果不写注解的describe属性，那么在调用的时候，OpenAI也不知道参数含义。

2、 在FunctionFactory中注册这个Service

```java
        this.registerFunction(this.weatherApiService);
```

# 其他功能

目前系统接入了Lucene，可以导入本地文件，然后进行全文搜索，将搜索结果给OpenAI进行处理总结后返回给用户。

具体的接口可以使用Swagger调用体验。

    com.lgf.warehouse.modules.ai.openai.controller.LuceneSearchController
    com.lgf.warehouse.modules.ai.openai.controller.LuceneImportController

这个文档不少内容是用Copilot写的，真不错啊（除了要花钱）