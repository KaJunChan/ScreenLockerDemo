package com.example.demo.screenlocker.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.demo.screenlocker.R;
import com.example.demo.screenlocker.activity.LockedActivity;

/**
 * Created by Administrator on 2016/6/24.
 */
public class LockedReceiver extends BroadcastReceiver {
    private static WindowManager mWindowManager;
    public static final String UNLOCKED = "android.action.unlocked";
    private static View mContainer;

    @Override
    public void onReceive(Context context, Intent intent) {

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();

        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Intent it = new Intent(context, LockedActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
//            addLockedViewToWindowManager(context);
        } else if ("android.lock.restarted".equals(action)) {
            Intent it = new Intent(context, LockedActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        }
    }


    private void addLockedViewToWindowManager(Context context) {
        initContainer(context);
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        WindowManager.LayoutParams lp = generateLayoutParams(context);
        mWindowManager.addView(mContainer, lp);
    }

    private void initContainer(Context context) {
        if (mContainer == null) {
            mContainer = View.inflate(context, R.layout.activity_locked, null);

            Button unlocked = (Button) mContainer.findViewById(R.id.unlockbtn);
            unlocked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mWindowManager.removeView(mContainer);
                }
            });
        }
    }

    private WindowManager.LayoutParams generateLayoutParams(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
