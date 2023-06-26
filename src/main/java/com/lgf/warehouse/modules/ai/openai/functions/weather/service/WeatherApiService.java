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
