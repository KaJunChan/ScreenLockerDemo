package com.example.demo.screenlocker;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import eightbitlab.com.blurview.BlurView;

/**
 * Created by Administrator on 2016/6/24.
 */
public class LockedReceiver extends BroadcastReceiver {
    private static WindowManager windowManager;
    public static final String UNLOCKED = "android.action.unlocked";
    private static View mContainer;
    private Context mCtx;
    private RecyclerView mRecyclerView;
    private BlurView mBlurView;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mCtx = context;
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Intent it = new Intent(context, LockedActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
            keyguardLock.disableKeyguard();
        } else if (UNLOCKED.equals(action)) {

        } else if ("android.lock.restarted".equals(action)) {
            Intent it = new Intent(context, LockedActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        }
    }


    private void addLockedViewToWindowManager(Context context) {
        initContainer(context);
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        WindowManager.LayoutParams lp = generateLayoutParams(context);
        windowManager.addView(mContainer, lp);
    }

    private void initContainer(Context context) {
        if (mContainer == null) {
            mContainer = View.inflate(context, R.layout.lock_act, null);

            Button unlocked = (Button) mContainer.findViewById(R.id.unlockbtn);
            unlocked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    windowManager.removeView(mContainer);
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
