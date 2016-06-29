package com.example.demo.screenlocker;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Administrator on 2016/6/24.
 */
public class LockedReceiver extends BroadcastReceiver {
    private WindowManager windowManager;
    public static final String UNLOCKED = "android.action.unlocked";

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();

        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
              Intent it=new Intent(context,LockedActitvity.class);
              it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(it);
        } else if (UNLOCKED.equals(action)) {

        }else if ("android.lock.restarted".equals(action)){
            Intent it=new Intent(context,LockedActitvity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        }
    }


    private void addLockedViewToWindowManager(Context context){
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();
        View mContainer=View.inflate(context,R.layout.lock_act,null);
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams lp = generateLayoutParams(context);
        windowManager.addView(mContainer, lp);
    }

    private WindowManager.LayoutParams generateLayoutParams(Context context) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        lp.x = 0;
        lp.y = 0;
        lp.format = PixelFormat.TRANSLUCENT;
        return lp;
    }
}
