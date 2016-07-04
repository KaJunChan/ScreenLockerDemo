package com.example.demo.screenlocker.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.screenlocker.modeldemo.NotifyData;
import com.example.demo.screenlocker.R;
import com.example.demo.screenlocker.widget.BaseSwipeLayout;
import com.example.demo.screenlocker.widget.LockPatternView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class LockedActivity extends AppCompatActivity implements LockPatternView.OnPatternListener {

    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    private BlurView mBlurView;
    private ImageView mWallPaper;
    private BaseSwipeLayout mSwipeLayout;
    private LinearLayout mNotificationCenter;

    private String[] mImages = {"http://img2.guang.j.cn/thumb/g1/M01/7E/99/wKggKVbUEG3gtFXsAAUkxVsK3wQ28.jpeg_1080x1920x50.jpeg",
            "http://pic1.ipadown.com/imgs/43/oqukm0dgl3t.jpg",
            "http://attachments.gfan.com/forum/201508/28/100733md4yolxr0nxksgiv.jpg"};

    private static final int REFRESH_NOTIFICATION_LIST = 0x101;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int what = message.what;
            switch (what) {
                case REFRESH_NOTIFICATION_LIST:
                    if (message.obj instanceof NotifyData) {
                        addNotification((NotifyData) message.obj);
                    }
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
        setContentView(R.layout.activity_locked);
        EventBus.getDefault().register(this);
        nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
        initView();
        getWapper();
    }

    private void initView() {
        mWallPaper = (ImageView) findViewById(R.id.wallpaper);
        mSwipeLayout = (BaseSwipeLayout) findViewById(R.id.swipe);
        mSwipeLayout.setOnDragListener(() -> {
            if (isGustureLocked()) {
                LockedActivity.this.finish();
            } else {
                bottomDialog();
            }
        });

        mNotificationCenter = (LinearLayout) findViewById(R.id.notificationCenter);

        mBlurView = (BlurView) findViewById(R.id.blurview);

        final View decorView = getWindow().getDecorView();
        final View rootView = decorView.findViewById(android.R.id.content);
        final Drawable windowBackground = decorView.getBackground();
        mBlurView.setupWith(rootView).windowBackground(windowBackground).blurAlgorithm(new RenderScriptBlur(this, true)).blurRadius(20f);
    }

    public void onUnLocked(View view) {
        finish();
    }

    private int flag = 0;

    public void onTestNotify(View view) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(LockedActivity.this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("测试标题")
                .setTicker("测试内容")
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

    private void getWapper() {
        Random random = new Random();
        int r = random.nextInt(3);
        String rImage = mImages[r];
        Picasso.with(this).load(rImage).config(Bitmap.Config.RGB_565).fit().error(R.drawable.wraper1).into(mWallPaper);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSwipeLayout.autoSettleBack();
        if(mDialog!=null&& mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private List<NotifyData> mData = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(Object object) {
        if (object instanceof NotifyData) {
            NotifyData notifyData = (NotifyData) object;
            mData.add(notifyData);
            Message msg = mHandler.obtainMessage();
            msg.what = REFRESH_NOTIFICATION_LIST;
            msg.obj = notifyData;
            mHandler.sendMessage(msg);
        }
    }

    private void addNotification(NotifyData notifyData) {
        RemoteViews remoteViews=notifyData.notification.contentView;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Dp2Px(this, 240f), Dp2Px(this, 128f));
        View view=null;
        if(remoteViews!=null){
            view=remoteViews.apply(this,mNotificationCenter);
        }else{
            view=View.inflate(this, R.layout.view_notification, null);
            ImageView icon = (ImageView) view.findViewById(R.id.notify_icon);
            TextView contentTv = (TextView) view.findViewById(R.id.notify_content);
            TextView titleTv = (TextView) view.findViewById(R.id.notification_title);
            Bundle extras = notifyData.notification.extras;
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            PackageManager packageManager = getPackageManager();
            CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(notifyData.packageName, PackageManager.GET_META_DATA);
                Drawable appLogo = packageManager.getApplicationIcon(appInfo);
                icon.setImageDrawable(appLogo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                icon.setImageResource(notifyData.notification.icon);
            }
            contentTv.setText(notificationText);
            titleTv.setText(title);

        }
        mNotificationCenter.addView(view, params);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private String mPatternString;
    private AlertDialog mDialog;

    private boolean isGustureLocked() {
        SharedPreferences preferences = getSharedPreferences(GustureLockSetupActivity.LOCK, MODE_PRIVATE);
        mPatternString = preferences.getString(GustureLockSetupActivity.LOCK_KEY, null);
        return TextUtils.isEmpty(mPatternString);
    }

    private LockPatternView mLockPatternView;

    private void bottomDialog() {
        View view = View.inflate(this, R.layout.view_gusture_locked, null);
        mLockPatternView = (LockPatternView) view.findViewById(R.id.lock_pattern);
        mLockPatternView.setOnPatternListener(this);
        mDialog = new AlertDialog.Builder(this).setView(view).create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    public void onPatternStart() {

    }

    @Override
    public void onPatternCleared() {

    }

    @Override
    public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

    }

    @Override
    public void onPatternDetected(List<LockPatternView.Cell> pattern) {
        if (mLockPatternView != null) {
            List<LockPatternView.Cell> lockPattern = LockPatternView.stringToPattern(mPatternString);
            if (pattern.equals(lockPattern)) {
                finish();
            } else {
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                Toast.makeText(this, R.string.lockpattern_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
