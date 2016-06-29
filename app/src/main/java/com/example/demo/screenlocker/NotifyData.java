package com.example.demo.screenlocker;

import android.app.Notification;

/**
 * Created by Administrator on 2016/6/28.
 */
public class NotifyData {

    public Notification notification;
    public String packageName;

    public NotifyData(Notification n,String name){
        this.notification=n;
        this.packageName=name;
    }
}
