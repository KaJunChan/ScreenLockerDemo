package com.example.demo.screenlocker.activity;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.demo.screenlocker.modeldemo.NotifyData;
import com.example.demo.screenlocker.R;

import java.util.List;

public class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.ViewHolder> {

        private Context context;
        private  List<NotifyData> mData;
        private PackageManager pm;

        public NotifyAdapter(Context context,List<NotifyData> datas){
            this.context=context;
            this.mData=datas;
            pm=this.context.getPackageManager();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView view = (CardView) View.inflate(context, R.layout.view_notification, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NotifyData notifyData = mData.get(position);

            Bundle extras = notifyData.notification.extras;
            CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(notifyData.packageName, PackageManager.GET_META_DATA);
                Drawable appLogo = pm.getApplicationIcon(appInfo);
                holder.icon.setImageDrawable(appLogo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                holder.icon.setImageResource(notifyData.notification.icon);
            }
            holder.content.setText(notificationText);
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
                content = (TextView) itemView.findViewById(R.id.notify_content);
            }
        }
    }