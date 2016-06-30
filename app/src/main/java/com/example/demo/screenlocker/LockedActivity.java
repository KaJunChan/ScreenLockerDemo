package com.example.demo.screenlocker;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class LockedActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private PowerManager.WakeLock wakeLock;
    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    private NotifyAdapter mAdapter;
    private BlurView mBlurView;

    private static final int REFRESH_NOTIFICATION_LIST = 0x101;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int what = message.what;
            switch (what) {
                case REFRESH_NOTIFICATION_LIST:
                    mAdapter.notifyDataSetChanged();
                    break;
            }

            return false;
        }
    });

    private NotificationManager nm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
        setContentView(R.layout.lock_act);
        EventBus.getDefault().register(this);
        nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
        initView();

    }


    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NotifyAdapter(LockedActivity.this,mData);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(20));
        mBlurView= (BlurView) findViewById(R.id.blurview);

        final View decorView = getWindow().getDecorView();
        final View rootView = decorView.findViewById(android.R.id.content);
        final Drawable windowBackground = decorView.getBackground();
        mBlurView.setupWith(rootView)
                .windowBackground(windowBackground)
                .blurAlgorithm(new RenderScriptBlur(this, true))
                .blurRadius(3f);
        Log.i("info", "create view");
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onUnLocked(View view) {
        finish();
    }


    private int flag = 0;

    public void onTestNotify(View view) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(LockedActivity.this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("测试")
                .setTicker("测试标题")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("ScreenLocker")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT);
        nm.notify(flag++, builder.build());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //解锁直接finish
        if (isFinishing()) return;
        //锁屏时，按Home返回时，再次唤醒LockedActivity
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!isAppOnForeground()) {
                    System.out.println("onStop()");
                    Intent it = new Intent(LockedActivity.this, LockedService.class);
                    it.setAction("android.restartLocked");
                    startService(it);
                }
            }
        }, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private List<NotifyData> mData = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object object) {
        if (object instanceof NotifyData) {
            NotifyData notifyData = (NotifyData) object;
            mData.add(notifyData);
            mHandler.sendEmptyMessage(REFRESH_NOTIFICATION_LIST);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        Log.i("info", "onDestory");
        super.onDestroy();
    }

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }
}
