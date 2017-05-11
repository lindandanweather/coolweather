package com.example.administrator.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.administrator.coolweather.gson.Weather;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();//更新天气
        updateBingPic();//更新背景图片
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);//获取系统闹钟服务

        int anHour = 8*60*60*1000;//8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;//设定系统时钟

        Intent i =new Intent(this,AutoUpdateService.class);//启动自己（重启）
        PendingIntent pi = PendingIntent.getService(this,0,i,0);//延迟intent

        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);//设置闹钟8小时后重启：刷新数据
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            //如果本地有数据,取出weather，取出天气ID和天气信息请求地址
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=/" + weatherId + "&key=5e3ea9dfc344442ea7d67a2b69509f86";

            //请求新信息
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();//取出返回信息
                    Weather weather = Utility.handleWeatherResponse(responseText);//解析数据成weather对象
                    if(weather != null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);//新信息保存到本地
                        editor.apply();
                    }
                }
            });
        }

    }

    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";//背景图请求地址
        //请求新图片
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();

            }
        });


    }
}
