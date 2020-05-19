package com.example.honyarexample;

import android.app.Application;

import com.honyar.SDKHonyarSupport;
import com.qw.soul.permission.SoulPermission;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //no necessary
        SDKHonyarSupport.getInstance().init(this);
        SoulPermission.init(this);
    }
}
