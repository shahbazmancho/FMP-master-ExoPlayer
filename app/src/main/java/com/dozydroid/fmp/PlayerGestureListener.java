package com.dozydroid.fmp;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dozydroid.fmp.activities.VideoPlayerActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

/**
 * Created by ShaBax on 1/2/2018.
 */

public class PlayerGestureListener extends SimpleOnGestureListener{

    Context context;
    SimpleExoPlayer simpleExoPlayer;
    SimpleExoPlayerView playerView;

    AudioManager audioManager;

    private FrameLayout videoCenterBoxgesture;


    LinearLayout fastForwardBox;
    TextView fastForwardTW;
    TextView fastForwardTargetTW;
    TextView fastForwardAllTW;

    LinearLayout app_video_volume_box;
    ImageView app_video_volume_icon;
    TextView app_video_volume;

    LinearLayout app_video_brightness_box;
    ImageView app_video_brightness_icon;
    TextView app_video_brightness;


    String TAG="PlayerGestureListener";

    public PlayerGestureListener(Context context,SimpleExoPlayer simpleExoPlayer, SimpleExoPlayerView playerView)
    {


        this.context=context;
        this.simpleExoPlayer=simpleExoPlayer;
        this.playerView=playerView;

        Log.d(TAG,"PlayerGestureListener");

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        TouchListenerVariable.maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        videoCenterBoxgesture= ((AppCompatActivity)context).findViewById(R.id.app_video_center_box);

        /// progress slider
        fastForwardBox= videoCenterBoxgesture.findViewById(R.id.app_video_fastForward_box);
        fastForwardTW= videoCenterBoxgesture.findViewById(R.id.app_video_fastForward);
        fastForwardTargetTW= videoCenterBoxgesture.findViewById(R.id.app_video_fastForward_target);
        fastForwardAllTW= videoCenterBoxgesture.findViewById(R.id.app_video_fastForward_all);

        //Volume

        app_video_volume_box=videoCenterBoxgesture.findViewById(R.id.app_video_volume_box);
        app_video_volume_icon=videoCenterBoxgesture.findViewById(R.id.app_video_volume_icon);
        app_video_volume=videoCenterBoxgesture.findViewById(R.id.app_video_volume);

        app_video_brightness_box=videoCenterBoxgesture.findViewById(R.id.app_video_brightness_box);
        app_video_brightness_icon=videoCenterBoxgesture.findViewById(R.id.app_video_brightness_icon);
        app_video_brightness=videoCenterBoxgesture.findViewById(R.id.app_video_brightness);


    }

    /**
     * Double click
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG,"onDoubleTap");
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG,"onDown");
        TouchListenerVariable.firstTouch = true;
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG,"onSingleTapUp");
       /* if (TouchListenerVariable.isShowing) {
            hide(false);
        } else {
            show(defaultTimeout);
        }*/
        return true;
    }

    /**
     *
     slide
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(TouchListenerVariable.toZoom)
        {
            Log.d(TAG,"toZoom");
            TouchListenerVariable.toZoom=false;
            return false;
        }
        //1. if not the active player,ignore
/*        boolean currentPlayer = playerView.isActivated();
        if (!currentPlayer) {
            Log.d(TAG,"!currentPlayer");
            //return true;
        }*/

        float oldX = e1.getX(), oldY = e1.getY();
        float deltaY = oldY - e2.getY();
        float deltaX = oldX - e2.getX();
        if (TouchListenerVariable.firstTouch) {
            TouchListenerVariable.toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
            TouchListenerVariable.volumeControl = oldX > playerView.getWidth() * 0.5f;
            TouchListenerVariable.firstTouch = false;

        }
        ExoPlayer player = playerView.getPlayer();
        if (TouchListenerVariable.toSeek)
        {
            Log.d(TAG,"toSeek");
            onProgressSlide(-deltaX / playerView.getWidth());
        }
        else
        {
            //if player in list controllerView,ignore
           /* if (videoView.inListView()) {
                return true;
            }*/
            float percent = deltaY / playerView.getHeight();
            if (TouchListenerVariable.volumeControl) {
                Log.d(TAG,"volumeControl");
                onVolumeSlide(percent);
            } else {
                onBrightnessSlide(percent);
            }
        }
        return true;
    }

    private void onBrightnessSlide(float percent) {
        Window window = ((Activity) context).getWindow();
        if (TouchListenerVariable.brightness < 0) {
            TouchListenerVariable.brightness = window.getAttributes().screenBrightness;
            if (TouchListenerVariable.brightness <= 0.00f) {
                TouchListenerVariable.brightness = 0.50f;
            } else if (TouchListenerVariable.brightness < 0.01f) {
                TouchListenerVariable.brightness = 0.01f;
            }
        }
        //Log.d(this.getClass().getSimpleName(), "brightness:" + TouchListenerVariable.brightness + ",percent:" + percent);

        videoCenterBoxgesture.setVisibility(View.VISIBLE);


        app_video_brightness_box.setVisibility(View.VISIBLE);

        WindowManager.LayoutParams lpa = window.getAttributes();
        lpa.screenBrightness = TouchListenerVariable.brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }

        app_video_brightness.setText(((int) (lpa.screenBrightness * 100)) + "%");
        window.setAttributes(lpa);
    }

    private void onVolumeSlide(float percent) {
        if (TouchListenerVariable.volume == -1) {
            TouchListenerVariable.volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (TouchListenerVariable.volume < 0)
                TouchListenerVariable.volume = 0;
        }

        videoCenterBoxgesture.setVisibility(View.VISIBLE);

        int index = (int) (percent * TouchListenerVariable.maxVolume) + TouchListenerVariable.volume;
        if (index > TouchListenerVariable.maxVolume)
            index = TouchListenerVariable.maxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        int i = (int) (index * 1.0 / TouchListenerVariable.maxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        // 显示


        if(i==0)
        {
            app_video_volume_icon.setImageResource(R.mipmap.ic_volume_off_white);
        }
        else {
            app_video_volume_icon.setImageResource( R.mipmap.ic_volume_up_white);
        }


        //$.id(R.id.app_video_brightness_box).gone();
        app_video_volume_box.setVisibility(View.VISIBLE);
        app_video_volume.setText(s);
    }

    private void onProgressSlide(float percent) {
        SimpleExoPlayer player = playerView.getPlayer();

        long position = player.getCurrentPosition();
        long duration = player.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);
        TouchListenerVariable.newPosition = delta + position;
        if (TouchListenerVariable.newPosition > duration) {
            TouchListenerVariable.newPosition = duration;
        } else if (TouchListenerVariable.newPosition <= 0) {
            TouchListenerVariable.newPosition = 0;
            delta = -position;
        }

        int showDelta = (int) delta / 1000;

        //player.seekTo(showDelta*1000);

        if (showDelta != 0) {

            videoCenterBoxgesture.setVisibility(View.VISIBLE);
            fastForwardBox.setVisibility(View.VISIBLE);
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            fastForwardTW.setText(text + "s");
            fastForwardTargetTW.setText(generateTime(TouchListenerVariable.newPosition) + "/");
            fastForwardAllTW.setText(generateTime(duration));
        }

    }
    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

}
