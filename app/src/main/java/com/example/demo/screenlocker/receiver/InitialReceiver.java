package com.example.demo.screenlocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.demo.screenlocker.service.HandleNotificationService;
import com.example.demo.screenlocker.service.LockedService;

public class InitialReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            context.startService(new Intent(context, LockedService.class));
            context.startService(new Intent(context, HandleNotificationService.class));
        }
    }
}  