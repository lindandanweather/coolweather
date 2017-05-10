package com.example.administrator.coolweather;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by Administrator on 2017/5/10.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext(){
        return context;
    }
}
