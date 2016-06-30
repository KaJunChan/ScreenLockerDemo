package com.example.demo.screenlocker;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by Administrator on 2016/6/30.
 */
public class NotificationMonitor extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}



