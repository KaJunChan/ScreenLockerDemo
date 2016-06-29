package com.example.demo.screenlocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InitalReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            context.startService(new Intent(context, LockedService.class));
            context.startService(new Intent(context, HandleNotificationService.class));
        }
    }
}  