package com.example.demo.screenlocker.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.demo.screenlocker.service.HandleNotificationService;
import com.example.demo.screenlocker.service.LockedService;
import com.example.demo.screenlocker.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, LockedService.class));
        startService(new Intent(this, HandleNotificationService.class));
    }

    public void onStartNotificationService(View view) {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 100);
    }

    public void onCancelGustureLock(View view) {
        getSharedPreferences(GustureLockSetupActivity.LOCK, MODE_PRIVATE).edit().clear().commit();
        Toast.makeText(MainActivity.this, "已取消图案解锁", Toast.LENGTH_SHORT).show();
    }

    public void onSetUpGustureLock(View view) {
        Intent it = new Intent(this, GustureLockSetupActivity.class);
        startActivity(it);
    }
}
