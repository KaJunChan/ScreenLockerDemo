package com.example.demo.screenlocker.helper;

import android.app.Activity;
import android.view.LayoutInflater;

import com.example.demo.screenlocker.R;
import com.example.demo.screenlocker.widget.BaseSwipeLayout;

/**
 * Created by Administrator on 2016/2/20 0020.
 */
public class SwipeHelper {

    private Activity mActivity;
    private BaseSwipeLayout mBaseSwipeLayout;

    public SwipeHelper(Activity activity) {
        this.mActivity = activity;
    }

    public void onActivityCreate() {
        mBaseSwipeLayout = (BaseSwipeLayout) LayoutInflater.from(mActivity)
                .inflate(R.layout.swipe_layout, null);
        mBaseSwipeLayout.setOnDragListener(new BaseSwipeLayout.OnDragListener() {
            @Override
            public void complete() {
                mActivity.finish();
            }
        });
    }

    public void onPostCreate() {
        mBaseSwipeLayout.attachToActivity(mActivity);
    }

    public void setSwipeEdge(int edgeFlag) {
        mBaseSwipeLayout.setSwipeEdge(edgeFlag);
    }

}