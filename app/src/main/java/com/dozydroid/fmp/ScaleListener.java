package com.dozydroid.fmp;

import android.content.Context;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

/**
 * Created by ShaBax on 1/2/2018.
 */




public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private float scaleFactor = 1f;
    View videoSurfaceView;
    Context context;
    String TAG="ScaleListener";

    public ScaleListener(Context context, View videoSurfaceView)
    {
        this.context=context;
        this.videoSurfaceView=videoSurfaceView;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        TouchListenerVariable.toZoom=true;
        Log.d(TAG,"onScaleBegin ");
        return true;

    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        TouchListenerVariable.toZoom=true;
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

        Log.d(TAG,"scaleFactor "+scaleFactor);


        int height2= (int) (videoSurfaceView.getHeight()*scaleFactor);
        int width2= (int) (videoSurfaceView.getWidth()*scaleFactor);
        Log.d(TAG,"height222 "+height2);
        //videoSurfaceView.setMinimumHeight(height2);
        //videoSurfaceView.setMinimumWidth(width2);

        videoSurfaceView.setScaleX(scaleFactor);
        videoSurfaceView.setScaleY(scaleFactor);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        TouchListenerVariable.toZoom=false;
        Log.d(TAG,"onScaleEnd ");

    }
}
