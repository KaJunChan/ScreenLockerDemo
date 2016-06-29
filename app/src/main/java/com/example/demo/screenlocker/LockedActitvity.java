package com.example.demo.screenlocker;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.recyclerview.itemanimator.SlideScaleInOutRightItemAnimator;

public class LockedActitvity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private PowerManager.WakeLock wakeLock;
    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    private PackageManager pm;
    private NotifyAdapter mAdapter;

    private NotificationManager nm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);

        //解锁系统锁屏
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
        keyguardLock.disableKeyguard();

        setContentView(R.layout.lock_act);
        EventBus.getDefault().register(this);

        initView();

    }


    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NotifyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(10));
        mRecyclerView.setItemAnimator(new SlideScaleInOutRightItemAnimator(mRecyclerView));

        final ImageView background = (ImageView) findViewById(R.id.back);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.back2);
        background.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (Build.VERSION.SDK_INT > 16) {
                            blur(bitmap, background, 20);
                        } else {
                            blurForLowVersion(bitmap, background,20);
                        }
                        return true;
                    }
                });
        pm = getPackageManager();
        nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
    }


    @Override
    protected void onResume() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wakeLock.acquire();
        super.onResume();
    }

    public void onUnLocked(View view) {
        finish();
    }


    private int flag = 0;

    public void onTestNotify(View view) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(LockedActitvity.this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("测试")
                .setTicker("测试标题")
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
                    Intent it = new Intent(LockedActitvity.this, LockedService.class);
                    it.setAction("android.restartLocked");
                    startService(it);
                }
            }
        }, 0);
    }

    @Override
    protected void onPause() {
        wakeLock.release();
        super.onPause();
    }

    private List<NotifyData> mData = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object object) {
        if (object instanceof NotifyData) {
            NotifyData notifyData = (NotifyData) object;
            mData.add(notifyData);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
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

    private class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(LockedActitvity.this, R.layout.view_notification, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NotifyData notifyData = mData.get(position);
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(notifyData.packageName, PackageManager.GET_META_DATA);
                Drawable applogo = pm.getApplicationIcon(appInfo);
                holder.icon.setImageDrawable(applogo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                holder.icon.setImageResource(notifyData.notification.icon);
            }
            holder.content.setText(notifyData.notification.tickerText);

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView icon;
            public TextView content;

            public ViewHolder(View itemView) {
                super(itemView);
                icon = (ImageView) itemView.findViewById(R.id.notify_icon);
                content = (TextView) itemView.findViewById(R.id.notify_text);
            }
        }
    }

    //毛玻璃效果（高级模糊效果）
    private void blurForLowVersion(Bitmap bkg, View view, float radius) {
        long startMs = System.currentTimeMillis();
        float scaleFactor = 8;
        Bitmap overlay = Bitmap.createBitmap(
                (int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop()
                / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
    }

    private void blur(Bitmap bkg, View view, float radius) {
        Bitmap overlay = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(bkg, -view.getLeft(), -view.getTop(), null);
        RenderScript rs = RenderScript.create(this);
        Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
        blur.setInput(overlayAlloc);
        blur.setRadius(radius);
        blur.forEach(overlayAlloc);
        overlayAlloc.copyTo(overlay);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
        rs.destroy();
    }
}
