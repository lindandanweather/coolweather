package com.example.administrator.coolweather.util;

import android.text.TextUtils;

import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.County;
import com.example.administrator.coolweather.db.Province;
import com.example.administrator.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/5/9.
 */

public class Utility {
    /**
     * 解析并处理省数据
     * @param response
     * @return
     */
    public static boolean handlerProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allPronvinces = new JSONArray(response);//实例化JSONArray
                for(int i=0;i<allPronvinces.length();i++){//循环遍历出省数据保存到数据库
                    JSONObject provinceObject = allPronvinces.getJSONObject(i);//获取当前对象
                    Province province  = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();//保存省名称和id到数据库
                }
                return true;//保存成功，可在主线程中修改UI数据
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;//response为空，没有数据保存
    }
    /**
     * 解析并处理城市数据
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handlerCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);//实例化JSONArray
                for(int i=0;i<allCities.length();i++){//循环遍历出城市数据保存到数据库
                    JSONObject cityObject = allCities.getJSONObject(i);//获取当前对象
                    City city  = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();//保存城市名称和id到数据库
                }
                return true;//保存成功，可在主线程中修改UI数据
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;//response为空，没有数据保存
    }
    /**
     * 解析并处理县数据
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handlerCountyResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);//实例化JSONArray
                for(int i=0;i<allCounties.length();i++){//循环遍历出县数据保存到数据库
                    JSONObject countyObject = allCounties.getJSONObject(i);//获取当前对象
                    County county  = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCityId(provinceId);
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();//保存县名称和id到数据库
                }
                return true;//保存成功，可在主线程中修改UI数据
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;//response为空，没有数据保存
    }

    /**
     *
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);//获取响应数据到jsonObject
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);//Forecast声明多个json字段命名
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
