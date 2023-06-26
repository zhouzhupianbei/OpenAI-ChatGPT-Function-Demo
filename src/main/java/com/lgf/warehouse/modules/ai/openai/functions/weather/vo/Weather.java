package com.lgf.warehouse.modules.ai.openai.functions.weather.vo;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;

/**
 * "location": {
 * "name": "Luoyang",
 * "region": "Henan",
 * "country": "China",
 * },
 * "current": {
 * "temp_c": 31.3,
 * "condition": {
 * "text": "Sunny",
 * "icon": "//cdn.weatherapi.com/weather/64x64/day/113.png",
 * "code": 1000
 * }
 * }
 */
@Data
public class Weather {
    /**
     * 城市
     */
    private String city;
    /**
     * 地区
     */
    private String region;
    /**
     * 国家
     */
    private String country;
    /**
     * 日期
     */
    private Integer days;
    /**
     * 温度
     */
    private double temp;
    /**
     * 天气情况
     */
    private String condition;
    /**
     * 天气图标
     */
    private String conditionIcon;

    @Operation
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
