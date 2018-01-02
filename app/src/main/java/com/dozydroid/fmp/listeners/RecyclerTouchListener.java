package com.dozydroid.fmp.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by MIRSAAB on 10/12/2017.
 */

public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int i);
    }

    GestureDetector mGestureDetector;

    public RecyclerTouchListener(Context c, OnItemClickListener mListener) {
        this.mListener = mListener;
        mGestureDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View mChildView = rv.findChildViewUnder(e.getX(), e.getY());
        if ((mChildView != null && mListener != null && mGestureDetector.onTouchEvent(e))) {
            mListener.onItemClick(mChildView, rv.getChildPosition(mChildView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
